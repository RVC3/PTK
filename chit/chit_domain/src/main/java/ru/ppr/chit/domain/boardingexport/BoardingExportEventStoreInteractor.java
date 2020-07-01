package ru.ppr.chit.domain.boardingexport;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.BoardingExportEvent;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.repository.local.BoardingExportEventRepository;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * Выполняет сохранение событий выгрузки посадки
 *
 * @author Dmitry Nevolin
 */
public class BoardingExportEventStoreInteractor {

    private final EventRepository eventRepository;
    private final BoardingExportEventRepository boardingExportEventRepository;
    private final LocalDbTransaction localDbTransaction;

    @Inject
    BoardingExportEventStoreInteractor(EventRepository eventRepository,
                                       BoardingExportEventRepository boardingExportEventRepository,
                                       LocalDbTransaction localDbTransaction) {
        this.eventRepository = eventRepository;
        this.boardingExportEventRepository = boardingExportEventRepository;
        this.localDbTransaction = localDbTransaction;
    }

    public void store(BoardingExportEvent boardingExportEvent) {
        try {
            localDbTransaction.begin();
            // Добавляем в БД базовое событие
            Event event = boardingExportEvent.getEvent(eventRepository);
            eventRepository.insert(event);
            boardingExportEvent.setEventId(event.getId());
            // Добавляем в БД событие выгрузки контроля
            boardingExportEventRepository.insert(boardingExportEvent);
            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }

}
