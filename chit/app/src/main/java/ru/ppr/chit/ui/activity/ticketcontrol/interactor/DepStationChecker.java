package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.chit.domain.controlstation.ControlStationManager;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.utils.ObjectUtils;

/**
 * Валидатор станции отправления ПД.
 *
 * @author Aleksandr Brazhkin
 */
class DepStationChecker {

    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final ControlStationManager controlStationManager;

    @Inject
    DepStationChecker(TripServiceInfoStorage tripServiceInfoStorage,
                      ControlStationManager controlStationManager) {
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.controlStationManager = controlStationManager;
    }

    /**
     * Выполняет проверку станции отправления ПД.
     *
     * @return {@code true} если проверка пройдена успешно, {@code false} - иначе
     */
    boolean checkStation(@Nullable Station depStation) {
        if (depStation == null) {
            // На всякий случай
            // Если станция отправления неизвестна, считаем её невалидной
            return false;
        }
        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
        if (controlStation == null) {
            throw new IllegalStateException("Station could not be checked without control station");
        }

        // Сравниваем код текущей станции контроля и код станции отправления в билете
        // Если они совпадают, считаем проверку успешной
        //
        // Станция так же может находиться ДО текущей станции контроля
        // см. http://agile.srvdev.ru/browse/CPPKPP-40110
        return ObjectUtils.equals(depStation.getCode(), controlStation.getCode()) || controlStationManager.isStationBeforeCurrent(depStation.getCode());
    }
}
