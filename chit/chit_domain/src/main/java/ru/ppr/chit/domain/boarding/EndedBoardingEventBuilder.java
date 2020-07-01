package ru.ppr.chit.domain.boarding;

import android.support.annotation.NonNull;

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
 * Билдер события {@link BoardingEvent} в статусе {@link BoardingEvent.Status#ENDED}.
 *
 * @author Aleksandr Brazhkin
 */
public class EndedBoardingEventBuilder {

    private final EventBuilder eventBuilder;
    private final BoardingEventRepository boardingEventRepository;
    private final TripServiceEventRepository tripServiceEventRepository;
    private final TripServiceStatusChecker tripServiceStatusChecker;
    private final BoardingStatusChecker boardingStatusChecker;
    private final TripServiceInfoStorage tripServiceInfoStorage;

    @Inject
    EndedBoardingEventBuilder(EventBuilder eventBuilder,
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
        if (!boardingStatusChecker.isStarted(prevBoardingEvent)) {
            throw new UserCriticalException("Посадка не может быть завершена, так как она не была начата.");
        }

        // Создаем базовое событие
        Event event = eventBuilder.build();

        // Заполняем собственные поля посадки
        BoardingEvent boardingEvent = new BoardingEvent();
        boardingEvent.setBoardingUuid(prevBoardingEvent.getBoardingUuid());
        boardingEvent.setStatus(BoardingEvent.Status.ENDED);
        boardingEvent.setStartTime(prevBoardingEvent.getStartTime());
        boardingEvent.setEndTime(event.getCreatedAt());

        // Заполняем базовое событие
        boardingEvent.setEvent(event);

        // Заполняем информацию о станции
        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
        if (controlStation == null) {
            throw new UserCriticalException("Посадка на может быть заверешна, так как не указана станция контроля.");
        }
        if (!controlStation.getCode().equals(prevBoardingEvent.getStationCode())) {
            throw new UserCriticalException("Посадка на может быть заверешна, так как была начата на другой станции.");
        }
        boardingEvent.setStationCode(controlStation.getCode());

        // Заполняем информацию об обслуживании
        TripServiceEvent lastTripServiceEvent = tripServiceEventRepository.loadLast();
        if (!tripServiceStatusChecker.isStarted(lastTripServiceEvent)) {
            throw new UserCriticalException("Посадка не может быть завершена, так как не была начата поездка.");
        }
        if (lastTripServiceEvent == null) {
            throw new IllegalStateException("Trip service event should not be null");
        }
        if (!lastTripServiceEvent.getId().equals(prevBoardingEvent.getTripServiceEventId())) {
            throw new IllegalStateException("Boarding could not be ended in different trip service");
        }
        boardingEvent.setTripServiceEvent(lastTripServiceEvent);

        return boardingEvent;
    }
}
