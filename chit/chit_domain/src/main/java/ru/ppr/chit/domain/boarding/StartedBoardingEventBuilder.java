package ru.ppr.chit.domain.boarding;

import android.support.annotation.NonNull;

import java.util.UUID;

import javax.inject.Inject;

import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.domain.tripservice.TripServiceStatusChecker;
import ru.ppr.core.exceptions.UserCriticalException;

/**
 * Билдер события {@link BoardingEvent} в статусе {@link BoardingEvent.Status#STARTED}.
 *
 * @author Aleksandr Brazhkin
 */
public class StartedBoardingEventBuilder {

    private final EventBuilder eventBuilder;
    private final BoardingEventRepository boardingEventRepository;
    private final TripServiceEventRepository tripServiceEventRepository;
    private final TripServiceStatusChecker tripServiceStatusChecker;
    private final BoardingStatusChecker boardingStatusChecker;
    private final TripServiceInfoStorage tripServiceInfoStorage;

    @Inject
    StartedBoardingEventBuilder(EventBuilder eventBuilder,
                                BoardingEventRepository boardingEventRepository,
                                TripServiceEventRepository tripServiceEventRepository,
                                TripServiceStatusChecker tripServiceStatusChecker,
                                BoardingStatusChecker boardingStatusChecker,
                                TripServiceInfoStorage tripServiceInfoStorage) {
        this.eventBuilder = eventBuilder;
        this.boardingEventRepository = boardingEventRepository;
        this.tripServiceEventRepository = tripServiceEventRepository;
        this.tripServiceStatusChecker = tripServiceStatusChecker;
        this.boardingStatusChecker = boardingStatusChecker;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
    }

    @NonNull
    public BoardingEvent build() throws Exception {

        // Проверяем статус последнего события посадки
        BoardingEvent prevBoardingEvent = boardingEventRepository.loadLast();
        if (boardingStatusChecker.isStarted(prevBoardingEvent)) {
            throw new UserCriticalException("Посадка не может быть начата повторно.");
        }

        // Создаем базовое событие
        Event event = eventBuilder.build();

        // Заполняем собственные поля посадки
        BoardingEvent boardingEvent = new BoardingEvent();
        boardingEvent.setBoardingUuid(UUID.randomUUID().toString());
        boardingEvent.setStatus(BoardingEvent.Status.STARTED);
        boardingEvent.setStartTime(event.getCreatedAt());

        // Заполняем базовое событие
        boardingEvent.setEvent(event);

        // Заполняем информацию о станции
        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
        if (controlStation == null) {
            throw new UserCriticalException("Посадка не может быть начата, не задана станция контроля.");
        }
        boardingEvent.setStationCode(controlStation.getCode());

        // Заполняем информацию об обслуживании
        TripServiceEvent lastTripServiceEvent = tripServiceEventRepository.loadLast();
        if (!tripServiceStatusChecker.isStarted(lastTripServiceEvent)) {
            throw new UserCriticalException("Посадка не может быть начата без начала поездки.");
        }
        boardingEvent.setTripServiceEvent(lastTripServiceEvent);

        return boardingEvent;
    }
}
