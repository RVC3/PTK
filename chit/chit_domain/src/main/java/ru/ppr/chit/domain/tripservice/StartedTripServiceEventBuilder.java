package ru.ppr.chit.domain.tripservice;

import java.util.UUID;

import javax.inject.Inject;

import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.core.exceptions.UserCriticalException;

/**
 * Билдер события {@link TripServiceEvent} в статусе {@link TripServiceEvent.Status#STARTED}.
 *
 * @author Aleksandr Brazhkin
 */
public class StartedTripServiceEventBuilder {

    private final EventBuilder eventBuilder;
    private final TripServiceEventRepository tripServiceEventRepository;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final TripServiceStatusChecker tripServiceStatusChecker;

    @Inject
    StartedTripServiceEventBuilder(EventBuilder eventBuilder,
                                   TripServiceEventRepository tripServiceEventRepository,
                                   TripServiceInfoStorage tripServiceInfoStorage,
                                   TripServiceStatusChecker tripServiceStatusChecker) {
        this.eventBuilder = eventBuilder;
        this.tripServiceEventRepository = tripServiceEventRepository;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.tripServiceStatusChecker = tripServiceStatusChecker;
    }

    public TripServiceEvent build() throws Exception {
        // Проверяем статус последнего события обслуживания
        TripServiceEvent prevTripServiceEvent = tripServiceEventRepository.loadLast();
        if (tripServiceStatusChecker.isStarted(prevTripServiceEvent)) {
            throw new UserCriticalException("Поездка не может быть начата, пока не заврешена старая поездка.");
        }
        // Проверяем наличие пользователя
        User user = tripServiceInfoStorage.getUser();
        if (user == null) {
            throw new UserCriticalException("Поездка не может быть начата, пока не задан пользователь.");
        }
        // Если актуальной информации о поезде нет, значит создаём пустую для удобства
        TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
        if (trainInfo == null) {
            trainInfo = new TrainInfo();
        }
        // Создаем базовое событие
        Event event = eventBuilder.build();
        // Заполняем собственные поля обслуживания
        TripServiceEvent tripServiceEvent = new TripServiceEvent();
        tripServiceEvent.setTripUuid(UUID.randomUUID().toString());
        tripServiceEvent.setStatus(TripServiceEvent.Status.STARTED);
        tripServiceEvent.setStartTime(event.getCreatedAt());
        tripServiceEvent.setUser(user);
        tripServiceEvent.setTrainInfo(trainInfo);
        // Заполняем базовое событие
        tripServiceEvent.setEvent(event);

        return tripServiceEvent;
    }
}
