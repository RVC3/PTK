package ru.ppr.chit.bs.synchronizer;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.api.entity.ErrorEntity;
import ru.ppr.chit.api.mapper.TicketBoardingMapper;
import ru.ppr.chit.api.request.GetBoardingListRequest;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManagerStub;
import ru.ppr.chit.bs.synchronizer.base.Notifier;
import ru.ppr.chit.bs.synchronizer.base.SynchronizeException;
import ru.ppr.chit.bs.synchronizer.base.Synchronizer;
import ru.ppr.chit.bs.synchronizer.event.SyncInfoEvent;
import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.logger.Logger;

/**
 * Класс, синхронизирующий данные о посадках
 *
 * @author m.sidorov
 */
public class TicketBoardingSynchronizer implements Synchronizer<List<TicketBoarding>>, Notifier<String> {

    private static final String TAG = Logger.makeLogTag(TicketBoardingSynchronizer.class);

    private final SynchronizerInformer synchronizerInformer;
    private final ApiManager apiManager;
    private final BackupManagerStub backupManager = new BackupManagerStub();

    private List<TicketBoarding> ticketBoardingList;

    @Inject
    TicketBoardingSynchronizer(ApiManager apiManager,
                               SynchronizerInformer synchronizerInformer) {
        this.apiManager = apiManager;
        this.synchronizerInformer = synchronizerInformer;
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_BOARDING;
    }

    @Override
    public String getTitle() {
        return "Список посадок";
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
    public List<TicketBoarding> getLoadedData() {
        return ticketBoardingList;
    }

    @Override
    public boolean hasLoadedData(){
        return getLoadedData() != null;
    }

    @Override
    public Completable load() {
        ticketBoardingList = null;

        return Single
                .fromCallable(() -> {
                    notify(getTitle() + ": загрузка данных");
                    GetBoardingListRequest request = new GetBoardingListRequest();
                    // В будущем из-за текущих датаконтрактов нечего передавать, оставляем 0
                    request.setMinId(0);
                    return request;
                })
                .flatMap(apiManager.api()::getBoardingList)
                .flatMapCompletable(response -> {
                    if (response.getError() == null) {
                        ticketBoardingList = TicketBoardingMapper.INSTANCE.entityListToModelList(response.getTicketBoardingList());
                    } else if (response.getError().getCode().equals(ErrorEntity.Code.TRAIN_THREAD_CODE_NOT_SET)) {
                        ticketBoardingList = new ArrayList<TicketBoarding>();
                    } else {
                        ticketBoardingList = null;
                        return Completable.error(new SynchronizeException(String.format("%s: ошибка обработки запроса [%s]", getTitle(), response.getError().getDescription())));
                    }
                    return Completable.complete();
                });
    }

    @Override
    public Completable apply() {
        // store ticketBoardingList not implemented
        return Completable.complete();
    }

}
