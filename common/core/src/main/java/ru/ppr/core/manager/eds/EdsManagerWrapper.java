package ru.ppr.core.manager.eds;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Future;

import javax.inject.Inject;

import ru.ppr.core.domain.model.EdsType;
import ru.ppr.edssft.SftEdsChecker;
import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.edssft.model.GetStateResult;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;

/**
 * Обертка над {@link EdsManager}.
 * Содержит синхронные блокирующие обращения к ЭЦП из любого потока.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsManagerWrapper {

    private static final String TAG = Logger.makeLogTag(EdsManagerWrapper.class);

    private final EdsManager edsManager;

    @Inject
    public EdsManagerWrapper(EdsManager edsManager) {
        this.edsManager = edsManager;
    }

    /**
     * Метод для получения используемых настроек для ЭЦП.
     *
     * @return используемые настройки для ЭЦП.
     */
    @NonNull
    public EdsDirs getEdsDirs() {
        return edsManager.getEdsDirs();
    }

    @NonNull
    public EdsType getCurrentEdsType() {
        return edsManager.getCurrentEdsType();
    }

    @NonNull
    public CheckSignResult verifySignBlocking(byte[] data, byte[] signature, long edsKeyNumber) {
        Future<CheckSignResult> future = EdsManager.EDS_EXECUTOR
                .submit(() -> edsManager.verifySign(data, signature, edsKeyNumber));

        CheckSignResult checkSignResult;
        try {
            checkSignResult = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
            checkSignResult = new CheckSignResult();
            checkSignResult.setState(CheckSignResultState.INVALID);
            checkSignResult.setDescription("Future error");
        }
        return checkSignResult;
    }

    // Инициализирует edsChecker с новыми лицензиями
    public boolean pullEdsCheckerBlocking() {
        Future<Boolean> future = EdsManager.EDS_EXECUTOR
                .submit(edsManager::pullEdsChecker);

        boolean result = false;
        try {
            result = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
        return result;
    }

    public boolean closeBlocking() {
        Future<Boolean> future = EdsManager.EDS_EXECUTOR
                .submit(edsManager::close);

        boolean result = false;
        try {
            result = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
        return result;
    }

    public boolean takeLicensesBlocking(@NonNull final File srcDir) {
        Future<Boolean> future = EdsManager.EDS_EXECUTOR
                .submit(() -> edsManager.takeLicenses(srcDir));

        boolean result = false;
        try {
            result = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
        return result;
    }

    // Проверяет состояние лицензии и создает запрос в случае необходимости
    @NonNull
    public GetStateResult getStateBlocking() {
        Future<GetStateResult> future = EdsManager.EDS_EXECUTOR
                .submit(edsManager::getState);

        GetStateResult getStateResult;updateConfig:
        try {
            getStateResult = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
            getStateResult = new GetStateResult();
            getStateResult.setSuccessful(false);
            getStateResult.setState(SftEdsChecker.SFT_STATE_NO_LICENSES);
        }
        return getStateResult;
    }

    @NonNull
    public GetKeyInfoResult getKeyInfoBlocking(long edsKeyNumber) {
        Future<GetKeyInfoResult> future = EdsManager.EDS_EXECUTOR
                .submit(() -> edsManager.getKeyInfo(edsKeyNumber));

        GetKeyInfoResult getKeyInfoResult;
        try {
            getKeyInfoResult = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
            getKeyInfoResult = new GetKeyInfoResult();
            getKeyInfoResult.setSuccessful(false);
            getKeyInfoResult.setDescription("Future error");
        }
        return getKeyInfoResult;
    }

    @NonNull
    public SignDataResult signBlocking(byte[] data, Date signDateTime) {
        Future<SignDataResult> future = EdsManager.EDS_EXECUTOR
                .submit(() -> edsManager.signData(data, signDateTime));

        SignDataResult signDataResult;
        try {
            signDataResult = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
            signDataResult = new SignDataResult();
            signDataResult.setSuccessful(false);
            signDataResult.setDescription("Future error");
        }
        return signDataResult;
    }

    /**
     * Выполняет попытку подписи данных.
     *
     * @return
     */
    public SignDataResult pingEdsBlocking() {
        Future<SignDataResult> future = EdsManager.EDS_EXECUTOR
                .submit(() -> edsManager.pingEds());

        SignDataResult signDataResult;
        try {
            signDataResult = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
            signDataResult = new SignDataResult();
            signDataResult.setSuccessful(false);
            signDataResult.setDescription("Future error");
        }
        return signDataResult;
    }

    /**
     * Выполняет попытку проверки данных.
     *
     * @return - true в случае если есть возможность выполнять проверку подписи
     */
    public boolean checkVerifySignPossibility() {
        Future<Boolean> future = EdsManager.EDS_EXECUTOR
                .submit(edsManager::checkVerifySignPossibility);

        Boolean signDataResult;
        try {
            signDataResult = future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
            signDataResult = false;
        }
        return signDataResult;
    }

    /**
     * Выполняет замену конфига SFT
     *
     * @return
     */
    public void updateConfigBlocking(@NonNull final EdsConfig edsConfig) {
        Future<?> future = EdsManager.EDS_EXECUTOR
                .submit(() -> edsManager.updateConfig(edsConfig));
        try {
            future.get();
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
    }
}
