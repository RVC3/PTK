package ru.ppr.chit.domain.tripservice;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.TrainInfoRepository;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * @author Aleksandr Brazhkin
 */
public class TripServiceEventStoreInteractor {

    private final LocalDbTransaction localDbTransaction;
    private final TripServiceEventRepository tripServiceEventRepository;
    private final EventRepository eventRepository;
    private final TrainInfoRepository trainInfoRepository;

    @Inject
    TripServiceEventStoreInteractor(LocalDbTransaction localDbTransaction,
                                    TripServiceEventRepository tripServiceEventRepository,
                                    EventRepository eventRepository,
                                    TrainInfoRepository trainInfoRepository) {

        this.localDbTransaction = localDbTransaction;
        this.tripServiceEventRepository = tripServiceEventRepository;
        this.eventRepository = eventRepository;
        this.trainInfoRepository = trainInfoRepository;
    }

    void store(TripServiceEvent tripServiceEvent) {
        try {
            localDbTransaction.begin();
            // Добавляем в БД базовое событие
            Event event = tripServiceEvent.getEvent(eventRepository);
            eventRepository.insert(event);
            tripServiceEvent.setEventId(event.getId());
            // Добавляем в БД информацию о поезде если необходимо
            TrainInfo trainInfo = tripServiceEvent.getTrainInfo(trainInfoRepository);
            if (trainInfo != null && trainInfo.getId() == null) {
                trainInfoRepository.insert(trainInfo);
            }
            // Добавляем в БД событие обслуживания поезда
            tripServiceEventRepository.insert(tripServiceEvent);

            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }
}