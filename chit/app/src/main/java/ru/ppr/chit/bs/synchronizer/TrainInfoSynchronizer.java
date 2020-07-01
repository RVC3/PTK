package ru.ppr.chit.bs.synchronizer;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.Completable;
import ru.ppr.chit.api.entity.ErrorEntity;
import ru.ppr.chit.api.mapper.TrainInfoMapper;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManagerStub;
import ru.ppr.chit.bs.synchronizer.base.Notifier;
import ru.ppr.chit.bs.synchronizer.base.SynchronizeException;
import ru.ppr.chit.bs.synchronizer.base.Synchronizer;
import ru.ppr.chit.bs.synchronizer.event.SyncInfoEvent;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.tripservice.TripServiceManager;
import ru.ppr.core.exceptions.UserException;
import ru.ppr.logger.Logger;

/**
 * Класс, синхронизирующий информацию о поезде
 *
 * @author m.sidorov
 */
public class TrainInfoSynchronizer implements Synchronizer<TrainInfo>, Notifier<String> {

    private static final String TAG = Logger.makeLogTag(TrainInfoSynchronizer.class);

    private final SynchronizerInformer synchronizerInformer;
    private final ApiManager apiManager;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final TripServiceManager tripServiceManager;
    private final BackupManagerStub backupManager = new BackupManagerStub();

    private TrainInfo loadedTrain;

    @Inject
    TrainInfoSynchronizer(ApiManager apiManager,
                          TripServiceInfoStorage tripServiceInfoStorage,
                          TripServiceManager tripServiceManager,
                          SynchronizerInformer synchronizerInformer) {
        this.apiManager = apiManager;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.tripServiceManager = tripServiceManager;
        this.synchronizerInformer = synchronizerInformer;
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_TRAININFO;
    }

    @Override
    public String getTitle() {
        return "Информация о поезде";
    }

    @Override
    public BackupManager getBackupManager() {
        return backupManager;
    }

    @Override
    public void notify(String message) {
        synchronizerInformer.notify(new SyncInfoEvent(getType(), message));
    }

    @Override
    @Nullable
    public TrainInfo getLoadedData() {
        return loadedTrain;
    }

    @Override
    public boolean hasLoadedData(){
        return getLoadedData() != null;
    }

    @Override
    public Completable load() {
        loadedTrain = null;

        return Completable
                .fromAction(() -> notify(getTitle() + ": загрузка данных"))
                .andThen(apiManager.api().getCurrentTrainInfo())
                .flatMapCompletable(response -> {
                    TrainInfo bsTrainInfo;
                    if (response.getError() == null) {
                        bsTrainInfo = TrainInfoMapper.INSTANCE.entityToModel(response.getTrainInfo());
                    } else if (response.getError().getCode().equals(ErrorEntity.Code.TRAIN_THREAD_CODE_NOT_SET)) {
                        bsTrainInfo = null;
                    } else {
                        return Completable.error(new SynchronizeException(String.format("%s: ошибка обработки запроса [%s]", getTitle(), response.getError().getDescription())));
                    }
                    // проверяем, что поезд можно синхронизировать
                    checkTrain(bsTrainInfo);
                    // запоминаем загруженный поезд
                    loadedTrain = bsTrainInfo;

                    return Completable.complete();
                });
    }

    @Override
    public Completable apply() {
        return Completable
                .fromAction(() -> {
                    if (hasLoadedData()) {
                        notify(getTitle() + ": сохранение данных");
                        tripServiceInfoStorage.updateTrainInfo(getLoadedData());
                    }
                })
                .onErrorResumeNext(throwable -> Completable.error(UserException.wrap(throwable, "Ошибка сохранения нити поезда")));
    }

    // Проверяет что можно синхронизировать нить поезда (в режиме поездки поезд должен совпадать)
    private void checkTrain(TrainInfo bsTrainInfo) throws SynchronizeException {
        // Проверяем совпадение нити поезда, только если режим "в поездке"
        if (tripServiceManager.isTripServiceStarted()) {
            TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
            String currentTrainThreadId = trainInfo != null && trainInfo.getTrainThreadId() != null ? trainInfo.getTrainThreadId() : "";

            String bsTrainThreadId = bsTrainInfo != null && bsTrainInfo.getTrainThreadId() != null ? bsTrainInfo.getTrainThreadId() : "";

            if (!currentTrainThreadId.equals(bsTrainThreadId)) {
                String bsTrainNumber = bsTrainInfo != null ? bsTrainInfo.getTrainNumber() : "не задан";
                throw new SynchronizeException(String.format("Поезд [%s] базовой станции не совпадает с текущим поездом терминала.\n" +
                        "Для выполнения синхронизации завершите поездку.", bsTrainNumber));
            }
        }
    }

}
