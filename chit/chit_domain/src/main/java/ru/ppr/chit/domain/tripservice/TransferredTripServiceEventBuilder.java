package ru.ppr.chit.domain.tripservice;

import javax.inject.Inject;

import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.repository.local.TrainInfoRepository;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.core.exceptions.UserCriticalException;

/**
 * Билдер события {@link TripServiceEvent} в статусе {@link TripServiceEvent.Status#TRANSFERRED}.
 *
 * @author Aleksandr Brazhkin
 */
public class TransferredTripServiceEventBuilder {

    private final EventBuilder eventBuilder;
    private final TripServiceEventRepository tripServiceEventRepository;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final TripServiceStatusChecker tripServiceStatusChecker;
    private final TrainInfoRepository trainInfoRepository;

    @Inject
    TransferredTripServiceEventBuilder(EventBuilder eventBuilder,
                                       TripServiceEventRepository tripServiceEventRepository,
                                       TripServiceInfoStorage tripServiceInfoStorage,
                                       TripServiceStatusChecker tripServiceStatusChecker,
                                       TrainInfoRepository trainInfoRepository) {
        this.eventBuilder = eventBuilder;
        this.tripServiceEventRepository = tripServiceEventRepository;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.tripServiceStatusChecker = tripServiceStatusChecker;
        this.trainInfoRepository = trainInfoRepository;
    }

    public TripServiceEvent build() throws Exception {
        // Проверяем статус последнего события обслуживания
        TripServiceEvent prevTripServiceEvent = tripServiceEventRepository.loadLast();
        if (prevTripServiceEvent == null || !tripServiceStatusChecker.isStarted(prevTripServiceEvent)) {
            throw new UserCriticalException("Поездка не может быть продолжена, так как она не была начата.");
        }
        // Проверяем наличие пользователя
        User user = tripServiceInfoStorage.getUser();
        if (user == null) {
            throw new UserCriticalException("Поездка не может быть продолжена, пока не задан пользователь.");
        }
        TrainInfo trainInfo = prevTripServiceEvent.getTrainInfo(trainInfoRepository);
        // Создаем базовое событие
        Event event = eventBuilder.build();
        // Заполняем собственные поля обслуживания
        TripServiceEvent tripServiceEvent = new TripServiceEvent();
        tripServiceEvent.setTripUuid(prevTripServiceEvent.getTripUuid());
        tripServiceEvent.setStatus(TripServiceEvent.Status.TRANSFERRED);
        tripServiceEvent.setStartTime(prevTripServiceEvent.getStartTime());
        tripServiceEvent.setUser(user);
        tripServiceEvent.setTrainInfo(trainInfo);
        // Заполняем базовое событие
        tripServiceEvent.setEvent(event);

        return tripServiceEvent;
    }
}
