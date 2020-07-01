package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.controlstation.ControlStationManager;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.utils.ObjectUtils;

/**
 * Валидатор станции отправления ПД.
 *
 * @author Max Sidorov
 */
class DestinationStationChecker {

    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final ControlStationManager controlStationManager;

    @Inject
    DestinationStationChecker(TripServiceInfoStorage tripServiceInfoStorage,
                              ControlStationManager controlStationManager) {
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.controlStationManager = controlStationManager;
    }

    /**
     * Выполняет проверку станции назначения ПД.
     *
     * @return {@code true} если проверка пройдена успешно, {@code false} - иначе
     */
    boolean checkStation(@Nullable Station destinationStation) {
        if (destinationStation == null) {
            // Если станция назначения неизвестна, считаем её невалидной
            return false;
        }
        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
        if (controlStation == null) {
            throw new IllegalStateException("Station could not be checked without control station");
        }

        // Если станция назначения после станции контроля, считаем проверку валидной
        return controlStationManager.isStationAfterCurrent(destinationStation.getCode());
    }
}
