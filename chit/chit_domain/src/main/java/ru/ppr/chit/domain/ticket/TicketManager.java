package ru.ppr.chit.domain.ticket;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.repository.local.TicketRepository;
import ru.ppr.logger.Logger;

/**
 * Менеджер билетов.
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class TicketManager {

    private static final String TAG = Logger.makeLogTag(TicketManager.class);

    private final Object lock = new Object();
    private final TicketRepository ticketRepository;
    private final BehaviorSubject<List<Ticket>> ticketListPublisher = BehaviorSubject.create();
    private List<Ticket> cachedTicketList;

    @Inject
    TicketManager(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public void init() {
        ticketListPublisher.onNext(getTicketList());
    }

    public List<Ticket> getTicketList() {
        Logger.trace(TAG, "getTicketList");
        List<Ticket> local = cachedTicketList;
        if (local == null) {
            synchronized (lock) {
                if (cachedTicketList == null) {
                    cachedTicketList = ticketRepository.loadAll();
                }
                local = cachedTicketList;
            }
        }
        return local;
    }

    public Observable<List<Ticket>> getTicketListPublisher() {
        return ticketListPublisher;
    }

    private void clearTicketList() {
        Logger.trace(TAG, "clearTicketList");
        cachedTicketList = null;
    }

    public void clearCache() {
        Logger.trace(TAG, "clearCache");
        synchronized (lock) {
            clearTicketList();
        }
        ticketListPublisher.onNext(getTicketList());
    }

}
