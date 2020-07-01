package ru.ppr.core.manager.eds;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.manager.factory.EdsCheckerFactory;
import ru.ppr.edssft.LicType;
import ru.ppr.edssft.SftEdsChecker;
import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.edssft.model.GetStateResult;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.edssft.model.VerifySignResult;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Менеджер ЭЦП.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class EdsManager {

    private static final String TAG = Logger.makeLogTag(EdsManager.class);

    private static long EDS_THREAD_ID;
    public static final ExecutorService EDS_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable r) {
            final Thread thread = new Thread(r, "eds");
            EDS_THREAD_ID = thread.getId();
            return thread;
        }
    });

    private final EdsCheckerFactory edsCheckerFactory;
    private final KeyRevokedChecker keyRevokedChecker;
    private SftEdsChecker edsChecker;
    private EdsConfig edsConfig;

    @Inject
    public EdsManager(@NonNull EdsCheckerFactory edsCheckerFactory, @NonNull KeyRevokedChecker keyRevokedChecker) {
        this.edsCheckerFactory = edsCheckerFactory;
        this.keyRevokedChecker = keyRevokedChecker;
    }

    /**
     * Возвращает текущую конфигурацию для работы менеджера.
     * {@link NonNull} так как предполагается, что поле будет заполнено при запуске приложения
     *
     * @return Конфигурация sft
     */
    @NonNull
    public EdsConfig getCurrentEdsConfig() {
        return edsConfig;
    }

    /**
     * Метод для получения используемого типа ЭЦП.
     *
     * @return тип ЭЦП.
     */
    @NonNull
    public EdsType getCurrentEdsType() {
        return edsConfig.edsType();
    }

    /**
     * Метод для получения используемых настроек для ЭЦП.
     *
     * @return используемые настройки для ЭЦП.
     */
    @NonNull
    public EdsDirs getEdsDirs() {
        return edsConfig.edsDirs();
    }

    public void updateConfig(@NonNull final EdsConfig edsConfig) {
        Logger.trace(TAG, "updateConfig: start, edsConfig = " + edsConfig);

        try {
            if (edsChecker != null) {
                this.edsChecker.close();
            }
            this.edsChecker = edsCheckerFactory.getEdsChecker(edsConfig);
            this.edsConfig = edsConfig;
            Logger.trace(TAG, "updateConfig: end");
        } catch (Exception e) {
            Logger.error(TAG, e);
            throw new IllegalStateException("updateConfig: error", e);
        }
    }

    public boolean open() {
        Logger.trace(TAG, "open: start");
        final boolean result = createSdkDirsIfNeed() && edsChecker.open();
        Logger.trace(TAG, "open: end, result = " + String.valueOf(result));

        return result;
    }

    public boolean close() {
        Logger.trace(TAG, "close: start");
        final boolean result = edsChecker.close();
        Logger.trace(TAG, "close: end, result = " + String.valueOf(result));

        return result;
    }

    /**
     * Выполняет попытку подписи данных.
     *
     * @return
     */
    public SignDataResult pingEds() {
        checkOnEdsThread();
        return signData(new byte[16], new Date());
    }

    /**
     * Дергает сфт.
     */
    public boolean pullEdsChecker() {
        checkOnEdsThread();

        if (close()) {
            final GetStateResult getStateResult = getState();
            Logger.trace(TAG, "pullEdsChecker: getStateResult = " + getStateResult);

            if (close()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Подписывает данные.
     *
     * @param data         Данные
     * @param signDateTime Время подписи
     * @return Результат подписи данных
     */
    @NonNull
    public SignDataResult signData(byte[] data, Date signDateTime) {
        checkOnEdsThread();
        return edsChecker.signData(data, signDateTime);
    }

    /**
     * Получает информацию по номеру ключа подписи
     *
     * @param edsKeyNumber Номер ключа подписи
     * @return Информация по ключу подписи
     */
    @NonNull
    public GetKeyInfoResult getKeyInfo(long edsKeyNumber) {
        checkOnEdsThread();
        return edsChecker.getKeyInfo(edsKeyNumber);
    }

    /**
     * Проверяет подпись
     *
     * @param data         Подписанные для проверки
     * @param signature    Подпись
     * @param edsKeyNumber Номер ключа ЭЦП
     * @return Результат проверки подписи
     */
    @NonNull
    public CheckSignResult verifySign(byte[] data, byte[] signature, long edsKeyNumber) {
        checkOnEdsThread();

        CheckSignResult checkSignResult = new CheckSignResult();

        VerifySignResult verifySignResult = edsChecker.verifySign(data, signature, edsKeyNumber);
        if (verifySignResult.isSuccessful()) {
            GetKeyInfoResult getKeyInfoResult = edsChecker.getKeyInfo(edsKeyNumber);
            if (getKeyInfoResult.isSuccessful()) {
                checkSignResult.setDeviceId(getKeyInfoResult.getDeviceId());
                checkSignResult.setDateOfRevocation(getKeyInfoResult.getDateOfRevocation());
                if (verifySignResult.isSignValid()) {
                    if (keyRevokedChecker.isRevoked(getKeyInfoResult.getDateOfRevocation())) {
                        checkSignResult.setState(CheckSignResultState.KEY_REVOKED);
                    } else {
                        checkSignResult.setState(CheckSignResultState.VALID);
                    }
                } else {
                    checkSignResult.setState(CheckSignResultState.INVALID);
                }
            } else {
                checkSignResult.setState(CheckSignResultState.INVALID);
            }
        } else {
            checkSignResult.setState(CheckSignResultState.INVALID);
        }
        Logger.info(TAG, "verifySign: checkSignResult=" + checkSignResult);
        return checkSignResult;
    }

    /**
     * Проверяет на возможность проверять подпись.
     * Как оказалось если указанного при проверке подписи ключа у СФТ не оказалось, то она возвращает ошибку,
     * т.е. просто проверять любые данные и смотреть чтобы метод выполнялся без ошибок нельзя.
     * Поэтому будем просто запрашивать state
     *
     * @return true если метод выполняется без ошибок
     */
    public boolean checkVerifySignPossibility() {
        checkOnEdsThread();
        GetStateResult getStateResult = edsChecker.getState();
        boolean possibility = getStateResult.isSuccessful() &&
                (getStateResult.getState() == SftEdsChecker.SFT_STATE_ALL_LICENSES || getStateResult.getState() == SftEdsChecker.SFT_STATE_ONLY_CHECK_LICENSE);
        Logger.info(TAG, "checkVerifySignPossibility: getStateResult=" + getStateResult + " return " + possibility);
        return possibility;
    }

    @NonNull
    public GetStateResult getState() {
        checkOnEdsThread();
        if (!createSdkDirsIfNeed()) {
            Logger.error(TAG, "getState: createSdkDirsIfNeed failed");
            return new GetStateResult();
        }
        GetStateResult getStateResult = edsChecker.getState();
        if (getStateResult.isSuccessful()) {
            if (createUtilDirsIfNeed()) {
                File edsUtilDstDir = getEdsDirs().getEdsUtilDstDir();
                Logger.trace(TAG, "getState: Clearing edsUtilDstDir");
                if (FileUtils2.clearDir(edsUtilDstDir, null)) {
                    EnumSet<LicType> requiredLicences = getCurrentEdsConfig().getLicTypes();
                    @SftEdsChecker.State int state = getStateResult.getState();
                    if (state == SftEdsChecker.SFT_STATE_ONLY_CHECK_LICENSE) {
                        if (requiredLicences.contains(LicType.SELL)) {
                            createLicRequest(EnumSet.of(LicType.SELL));
                        }
                    } else if (state == SftEdsChecker.SFT_STATE_ONLY_SELL_LICENSE) {
                        if (requiredLicences.contains(LicType.CHECK)) {
                            createLicRequest(EnumSet.of(LicType.CHECK));
                        }
                    } else if (state == SftEdsChecker.SFT_STATE_NO_LICENSES) {
                        createLicRequest(requiredLicences);
                    }
                } else {
                    Logger.error(TAG, "getState: Could not clear edsUtilDstDir: " + edsUtilDstDir.getAbsolutePath());
                }
            } else {
                Logger.error(TAG, "getState: createUtilDirsIfNeed failed");
            }
        }
        return getStateResult;
    }

    /**
     * Создает запросы лицензий для указанных типов.
     *
     * @param licTypes Типы лицензий
     */
    public void createLicRequest(@NonNull final EnumSet<LicType> licTypes) {
        checkOnEdsThread();
        if (createUtilDirsIfNeed()) {
            File edsUtilDstDir = getEdsDirs().getEdsUtilDstDir();
            Logger.trace(TAG, "createLicRequest: Clearing edsUtilDstDir");
            if (FileUtils2.clearDir(edsUtilDstDir, null)) {
                // Говорят, на кассе всё плохо. Она не умеет работать с одним файлом запроса на оба типа лицензий.
                // Поэтоу создаем разные запросы.
                if (licTypes.contains(LicType.CHECK)) {
                    edsChecker.createLicRequest(EnumSet.of(LicType.CHECK));
                }
                if (licTypes.contains(LicType.SELL)) {
                    edsChecker.createLicRequest(EnumSet.of(LicType.SELL));
                }
            } else {
                Logger.error(TAG, "createLicRequest: Could not clear edsUtilDstDir: " + edsUtilDstDir.getAbsolutePath());
            }
        } else {
            Logger.error(TAG, "createLicRequest: createUtilDirsIfNeed failed");
        }
    }

    public boolean takeLicenses(@NonNull final File srcDir) {
        checkOnEdsThread();
        if (createUtilDirsIfNeed()) {
            // Скопируем файлы в папку, предназначенную для работы с утилитой
            File edsUtilSrcDir = getEdsDirs().getEdsUtilSrcDir();
            Logger.trace(TAG, "takeLicenses: Clearing edsUtilSrcDir");
            if (FileUtils2.clearDir(edsUtilSrcDir, null)) {
                try {
                    if (FileUtils2.copyDir(srcDir, edsUtilSrcDir, null)) {
                        return edsChecker.takeLicenses();
                    } else {
                        Logger.error(TAG, "takeLicenses: Could not copy files to edsUtilSrcDir: " + edsUtilSrcDir.getAbsolutePath());
                    }
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            } else {
                Logger.error(TAG, "takeLicenses: Could not clear edsUtilSrcDir: " + edsUtilSrcDir.getAbsolutePath());
            }
        } else {
            Logger.error(TAG, "takeLicenses: createUtilDirsIfNeed failed");
        }
        return false;
    }

    /**
     * Вспомогательный метод для создания необходимых каталогов, если они не существуют.
     *
     * @return результат выполнения.
     */
    private boolean createUtilDirsIfNeed() {
        return createDirsIfNotExists(
                getEdsDirs().getEdsUtilSrcDir(),
                getEdsDirs().getEdsUtilDstDir()
        );
    }

    /**
     * Вспомогательный метод для создания необходимых каталогов, если они не существуют.
     *
     * @return результат выполнения.
     */
    private boolean createSdkDirsIfNeed() {
        return createDirsIfNotExists(
                getEdsDirs().getEdsWorkingDir(),
                getEdsDirs().getEdsTransportDir(),
                getEdsDirs().getEdsTransportInDir(),
                getEdsDirs().getEdsTransportOutDir()
        );
    }

    /**
     * Метод для создания каталогов.
     * Если указанные каталоги не существуют, то будет предпринята попытка их создания.
     * Если хотя бы один каталог не будет создан, то будет возвращен неуспешный результат выполнения.
     *
     * @param paths пути каталогов
     * @return при успешном выполнении результат {@code true}, иначе {@code false}.
     */
    private boolean createDirsIfNotExists(@NonNull final File... paths) {
        boolean result = true;

        for (File path : paths) {
            if (!path.exists()) {
                result = path.mkdirs();

                if (!result) {
                    Logger.error(TAG, "createDirsIfNotExists: can't create dir: " + path.getAbsolutePath());
                    break;
                }
            }
        }

        return result;
    }


    /**
     * Метод для проверки выполнения операций в соответствующем потоке.
     */
    private void checkOnEdsThread() {
        final Thread thread = Thread.currentThread();

        if (Thread.currentThread().getId() != EDS_THREAD_ID) {
            throw new RuntimeException("Method must be executed ONLY IN THE EDS THREAD! Current thread - " + thread);
        }
    }

}