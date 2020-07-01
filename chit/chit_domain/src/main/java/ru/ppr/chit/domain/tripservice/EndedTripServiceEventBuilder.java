package ru.ppr.chit.domain.tripservice;

import javax.inject.Inject;

import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.repository.local.TrainInfoRepository;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.domain.repository.local.UserRepository;
import ru.ppr.core.exceptions.UserCriticalException;

/**
 * Билдер события {@link TripServiceEvent} в статусе {@link TripServiceEvent.Status#ENDED}.
 *
 * @author Aleksandr Brazhkin
 */
public class EndedTripServiceEventBuilder {

    private final EventBuilder eventBuilder;
    private final TripServiceEventRepository tripServiceEventRepository;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final UserRepository userRepository;
    private final TrainInfoRepository trainInfoRepository;
    private final TripServiceStatusChecker tripServiceStatusChecker;

    @Inject
    EndedTripServiceEventBuilder(EventBuilder eventBuilder,
                                 TripServiceEventRepository tripServiceEventRepository,
                                 TripServiceInfoStorage tripServiceInfoStorage,
                                 UserRepository userRepository,
                                 TrainInfoRepository trainInfoRepository,
                                 TripServiceStatusChecker tripServiceStatusChecker) {
        this.eventBuilder = eventBuilder;
        this.tripServiceEventRepository = tripServiceEventRepository;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.userRepository = userRepository;
        this.trainInfoRepository = trainInfoRepository;
        this.tripServiceStatusChecker = tripServiceStatusChecker;
    }

    public TripServiceEvent build() throws Exception {
        // Проверяем наличие события начала обслуживания
        TripServiceEvent prevTripServiceEvent = tripServiceEventRepository.loadLast();
        if (prevTripServiceEvent == null || !tripServiceStatusChecker.isStarted(prevTripServiceEvent)) {
            throw new UserCriticalException("Поездка не может быть завершена, так как она не была начата.");
        }
        // Проверяем наличие пользователя
        User user = tripServiceInfoStorage.getUser();
        if (user == null) {
            throw new UserCriticalException("Поездка не может быть завершена, пока не задан пользователь.");
        }
        // Проверяем, что обслуживание закрывается корректным пользователем
        if (!prevTripServiceEvent.getUser(userRepository).getId().equals(user.getId())) {
            throw new UserCriticalException("Поездка не может быть завершена другим пользователем.");
        }
        // Если есть информация о поезде, делаем её устаревшей
        TrainInfo trainInfo = prevTripServiceEvent.getTrainInfo(trainInfoRepository);
        if (trainInfo != null) {
            trainInfo.setLegacy(true);
        }
        // Создаем базовое событие
        Event event = eventBuilder.build();
        // Заполняем собственные поля обслуживания
        TripServiceEvent tripServiceEvent = new TripServiceEvent();
        tripServiceEvent.setTripUuid(prevTripServiceEvent.getTripUuid());
        tripServiceEvent.setStatus(TripServiceEvent.Status.ENDED);
        tripServiceEvent.setStartTime(prevTripServiceEvent.getStartTime());
        tripServiceEvent.setEndTime(event.getCreatedAt());
        tripServiceEvent.setUser(user);
        tripServiceEvent.setTrainInfo(trainInfo);
        // Заполняем базовое событие
        tripServiceEvent.setEvent(event);

        return tripServiceEvent;
    }
}
