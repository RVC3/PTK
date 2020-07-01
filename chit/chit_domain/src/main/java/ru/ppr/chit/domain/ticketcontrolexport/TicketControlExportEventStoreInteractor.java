package ru.ppr.chit.domain.ticketcontrolexport;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.TicketControlExportEvent;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.TicketControlExportEventRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * @author Dmitry Nevolin
 */
public class TicketControlExportEventStoreInteractor {

    private final EventRepository eventRepository;
    private final TicketControlExportEventRepository ticketControlExportEventRepository;
    private final LocalDbTransaction localDbTransaction;

    @Inject
    TicketControlExportEventStoreInteractor(EventRepository eventRepository,
                                            TicketControlExportEventRepository ticketControlExportEventRepository,
                                            LocalDbTransaction localDbTransaction) {
        this.eventRepository = eventRepository;
        this.ticketControlExportEventRepository = ticketControlExportEventRepository;
        this.localDbTransaction = localDbTransaction;
    }

    public void store(TicketControlExportEvent ticketControlExportEvent) {
        try {
            localDbTransaction.begin();
            // Добавляем в БД базовое событие
            Event event = ticketControlExportEvent.getEvent(eventRepository);
            eventRepository.insert(event);
            ticketControlExportEvent.setEventId(event.getId());
            // Добавляем в БД событие выгрузки контроля
            ticketControlExportEventRepository.insert(ticketControlExportEvent);
            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }

    public void storeAll(List<TicketControlExportEvent> ticketControlExportEventList) {
        try {
            localDbTransaction.begin();
            // Добавляем в БД базовые события
            for (TicketControlExportEvent ticketControlExportEvent : ticketControlExportEventList) {
                Event event = ticketControlExportEvent.getEvent(eventRepository);
                eventRepository.insert(event);
                ticketControlExportEvent.setEventId(event.getId());
            }
            // Добавляем в БД события выгрузки контроля
            ticketControlExportEventRepository.insertAll(ticketControlExportEventList);
            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }

}
