package ru.ppr.chit.bs.synchronizer;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.bs.synchronizer.base.BackupManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManagerStub;
import ru.ppr.chit.bs.synchronizer.base.Notifier;
import ru.ppr.chit.bs.synchronizer.base.SynchronizeException;
import ru.ppr.chit.bs.synchronizer.base.Synchronizer;
import ru.ppr.chit.bs.synchronizer.event.SyncInfoEvent;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.exceptions.UserException;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.edssft.SftEdsChecker;
import ru.ppr.edssft.model.GetStateResult;
import ru.ppr.logger.Logger;

/**
 * Класс синхронизации SFT
 * По сути вырожденный класс, так как он просто объединяет в себе два класса синхронизации (лицензии, sft ключи)
 *
 * @author Dmitry Nevolin
 */
public class SftSynchronizer implements Synchronizer, Notifier<String> {

    private static final String TAG = Logger.makeLogTag(SftSynchronizer.class);

    private final EdsManagerWrapper edsManager;
    private final SftLicenseSynchronizer sftLicenseSynchronizer;
    private final SftDataSynchronizer sftDataSynchronizer;
    private final SynchronizerInformer synchronizerInformer;
    private final BackupManagerStub backupManager = new BackupManagerStub();

    private Boolean successSftSync;

    @Inject
    SftSynchronizer(EdsManagerWrapper edsManager,
                    SynchronizerInformer synchronizerInformer,
                    SftLicenseSynchronizer sftLicenseSynchronizer,
                    SftDataSynchronizer sftDataSynchronizer) {
        this.edsManager = edsManager;
        this.synchronizerInformer = synchronizerInformer;
        this.sftLicenseSynchronizer = sftLicenseSynchronizer;
        this.sftDataSynchronizer = sftDataSynchronizer;
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_SFT;
    }

    @Override
    public String getTitle() {
        return "Синхронизация sft";
    }

    @Override
    public BackupManager getBackupManager() {
        return backupManager;
    }

    @Override
    public void notify(String message) {
        synchronizerInformer.notify(new SyncInfoEvent(getType(), message));
    }

    // Возвращает, прошла ли синхронизация sft успешно
    @Override
    @Nullable
    public Boolean getLoadedData() {
        return successSftSync;
    }

    @Override
    public boolean hasLoadedData() {
        return getLoadedData() != null;
    }

    // По сути этот метод последовательно выполняет синхронизацию лицензии и SFT ключей load + apply
    @Override
    public Completable load() {
        successSftSync = false;
        if (edsManager.getCurrentEdsType() == EdsType.SFT) {
            Logger.info(TAG, "Eds type is SFT, call sftSynchronizer.start()");
            return syncLicense()
                    .andThen(syncSftData())
                    .doOnComplete(() -> {
                        successSftSync = true;
                    })
                    .onErrorResumeNext(throwable -> Completable.error(UserException.wrap(throwable, "Ошибка синхронизации SFT")));
        } else {
            Logger.info(TAG, "Eds type is STUB, skip call sftSynchronizer.start()");
            return Completable.complete();
        }
    }

    // Для синхронизации SFT ключей этот метод простая пустышка
    @Override
    public Completable apply() {
        return Completable.complete();
    }

    // вполняет проверку и синхронизацию лимцензии через sftLicenseSynchronizer
    private Completable syncLicense() {
        return Single
                // Проверяем наличие лицензии
                .fromCallable(() -> checkLicenseState(false))
                .flatMapCompletable(hasLicense -> {
                    if (hasLicense) {
                        // Если лицензия есть, то завершаем цепочку
                        return Completable.complete();
                    }
                    // синхронизируем лицензии
                    return sftLicenseSynchronizer
                            .load()
                            .andThen(sftLicenseSynchronizer.apply())
                            .andThen(Completable.fromAction(() -> checkLicenseState(true)));
                })
                .onErrorResumeNext(throwable -> Completable.error(UserException.wrap(throwable, "Ошибка обработки запроса лицензий")));
    }

    // вполняет проверку и синхронизацию SFT ключей через sftDataSynchronizer
    private Completable syncSftData() {
        // Синхронизируем SFT ключи
        return sftDataSynchronizer
                .load()
                .andThen(sftDataSynchronizer.apply())
                .andThen(Completable.fromAction(() -> {
                    // проверяем возможность подписи
                    if (!checkSftCheck()) {
                        throw new SynchronizeException("Ошибка проверки sft [недостаточно ключей], попробуйте повторить операцию позже");
                    }
                }))
                // Оборачиваем ошибку (если она не SynchronizeException) в абстрактную "Ошибка обработки запроса"
                .onErrorResumeNext(throwable -> Completable.error(UserException.wrap(throwable, "Ошибка обработки запроса sft ключей")));

    }

    // Проверяет наличие необходимых лицензий
    // throwException - определяет будет ли ф-ия возбуждать исключение
    private boolean checkLicenseState(boolean throwException) throws SynchronizeException {
        notify("Проверка лицензии");
        // проверяем состояние лицензии (при этом может быть созданы файлы для запроса недостающих частей лицензии)
        GetStateResult getStateResult = edsManager.getStateBlocking();
        Logger.info(TAG, "getStateResult = " + getStateResult.toString());

        if (!getStateResult.isSuccessful()) {
            if (throwException) {
                throw new SynchronizeException("Проверка лицензии: внутренняя ошибка получения состояния sft");
            }
            notify("Внутренняя ошибка получения состояния sft");
            return false;
        }

        boolean hasLicense = getStateResult.getState() == SftEdsChecker.SFT_STATE_ONLY_CHECK_LICENSE || getStateResult.getState() == SftEdsChecker.SFT_STATE_ALL_LICENSES;
        if (hasLicense) {
            notify("Проверка лицензии: успешно");
            return hasLicense;
        } else {
            if (throwException) {
                throw new SynchronizeException("Ошибка проверки лицензии [недостаточно ключей], попробуйте повторить операцию позже");
            }
            notify("Проверка лицензии: нет лицензии");
            return hasLicense;
        }
    }

    // проверка возможности ппроверки подписи
    private boolean checkSftCheck() {
        notify("Проверка ключей sft");
        // выполняем попытку проверки подписи
        boolean res = edsManager.checkVerifySignPossibility();
        Logger.info(TAG, "checkVerifySignPossibility = " + res);
        notify(res ? "Проверка ключей sft: успешно" : "Проверка ключей sft: ошибка");
        return res;
    }

}
