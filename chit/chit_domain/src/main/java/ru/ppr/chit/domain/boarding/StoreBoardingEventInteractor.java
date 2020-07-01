package ru.ppr.chit.domain.boarding;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * @author Aleksandr Brazhkin
 */
public class StoreBoardingEventInteractor {

    private final LocalDbTransaction localDbTransaction;
    private final BoardingEventRepository boardingEventRepository;
    private final EventRepository eventRepository;

    @Inject
    StoreBoardingEventInteractor(LocalDbTransaction localDbTransaction,
                                 BoardingEventRepository boardingEventRepository,
                                 EventRepository eventRepository) {

        this.localDbTransaction = localDbTransaction;
        this.boardingEventRepository = boardingEventRepository;
        this.eventRepository = eventRepository;
    }

    void store(BoardingEvent boardingEvent) {
        try {
            localDbTransaction.begin();

            // Добавляем в БД базовое событие
            Event event = boardingEvent.getEvent(eventRepository);
            eventRepository.insert(event);
            boardingEvent.setEventId(event.getId());

            // Добавляем в БД событие посадки на поезд
            boardingEventRepository.insert(boardingEvent);

            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }
}
