package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.model.local.TrainInfo;

/**
 * Валидатор номера поезда ПД.
 *
 * @author Aleksandr Brazhkin
 */
class TrainNumberChecker {

    private final TripServiceInfoStorage tripServiceInfoStorage;

    @Inject
    TrainNumberChecker(TripServiceInfoStorage tripServiceInfoStorage) {
        this.tripServiceInfoStorage = tripServiceInfoStorage;
    }

    /**
     * Выполняет проверку номера поезда ПД.
     *
     * @return {@code true} если проверка пройдена успешно, {@code false} - иначе
     */
    boolean checkTrainNumber(@Nullable String trainNumber) {
        if (trainNumber == null) {
            // На всякий случай
            // Если номер поезда неизвестен, считаем его невалидной
            return false;
        }
        TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
        // Если у нас нет информации о поезде, то считаем что билет валиден
        return trainInfo == null || trainInfo.getTrainNumber() == null || trainInfo.getTrainNumber().equals(trainNumber);
    }

}
