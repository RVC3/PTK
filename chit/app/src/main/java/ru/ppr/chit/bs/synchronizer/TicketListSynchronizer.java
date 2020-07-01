package ru.ppr.chit.bs.synchronizer;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import ru.ppr.chit.api.entity.ErrorEntity;
import ru.ppr.chit.api.mapper.TicketMapper;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManager;
import ru.ppr.chit.bs.synchronizer.base.BackupManagerStub;
import ru.ppr.chit.bs.synchronizer.base.Notifier;
import ru.ppr.chit.bs.synchronizer.base.SynchronizeException;
import ru.ppr.chit.bs.synchronizer.base.Synchronizer;
import ru.ppr.chit.bs.synchronizer.event.SyncInfoEvent;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.ticket.TicketStoreInteractor;
import ru.ppr.core.exceptions.UserException;
import ru.ppr.logger.Logger;

/**
 * Синхронизирует список билетов
 *
 * @author m.sidorov
 */
public class TicketListSynchronizer implements Synchronizer<List<Ticket>>, Notifier<String> {

    private static final String TAG = Logger.makeLogTag(TicketListSynchronizer.class);

    private final SynchronizerInformer synchronizerInformer;
    private final ApiManager apiManager;
    private final TicketStoreInteractor ticketStoreInteractor;
    private final BackupManagerStub backupManager = new BackupManagerStub();

    private List<Ticket> ticketList;

    @Inject
    TicketListSynchronizer(ApiManager apiManager,
                           TicketStoreInteractor ticketStoreInteractor,
                           SynchronizerInformer synchronizerInformer) {
        this.apiManager = apiManager;
        this.ticketStoreInteractor = ticketStoreInteractor;
        this.synchronizerInformer = synchronizerInformer;
    }

    @Override
    public SynchronizeType getType() {
        return SynchronizeType.SYNC_TICKETS;
    }

    @Override
    public String getTitle() {
        return "Список билетов";
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
    public List<Ticket> getLoadedData() {
        return ticketList;
    }

    @Override
    public boolean hasLoadedData(){
        return getLoadedData() != null;
    }

    @Override
    public Completable load() {
        ticketList = null;
        return Completable
                .fromAction(() -> notify(getTitle() + ": загрузка данных"))
                .andThen(apiManager.api().getTicketList())
                .flatMapCompletable(response -> {
                    if (response.getError() == null) {
                        ticketList = TicketMapper.INSTANCE.entityListToModelList(response.getTickets());
                    } else if (response.getError().getCode().equals(ErrorEntity.Code.TRAIN_THREAD_CODE_NOT_SET)) {
                        ticketList = new ArrayList<Ticket>();
                    } else {
                        ticketList = null;
                        return Completable.error(new SynchronizeException(String.format("%s: ошибка обработки запроса [%s]", getTitle(), response.getError().getDescription())));
                    }
                    return Completable.complete();
                });
    }

    @Override
    public Completable apply() {
        return Completable
                .fromAction(() -> {
                    if (hasLoadedData()) {
                        notify(getTitle() + ": сохранение данных");
                        ticketStoreInteractor.storeAll(ticketList);
                    }
                })
                .onErrorResumeNext(throwable -> Completable.error(UserException.wrap(throwable, "Ошибка сохранения списка билетов")));
    }

}
