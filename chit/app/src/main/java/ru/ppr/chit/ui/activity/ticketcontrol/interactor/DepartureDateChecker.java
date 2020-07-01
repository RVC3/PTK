package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.logger.Logger;

/**
 * Валидатор даты действия ПД.
 *
 * @author Aleksandr Brazhkin
 */
class DepartureDateChecker {

    private static final String TAG = Logger.makeLogTag(DepartureDateChecker.class);

    private final TripServiceInfoStorage tripServiceInfoStorage;

    @Inject
    DepartureDateChecker(TripServiceInfoStorage tripServiceInfoStorage) {
        this.tripServiceInfoStorage = tripServiceInfoStorage;
    }

    /**
     * Выполняет проверку даты действия ПД.
     *
     * @return {@code true} если проверка пройдена успешно, {@code false} - иначе
     */
    Result checkDate(@Nullable Date departureDate, long depStationCode) {
        Logger.trace(TAG, "checkDate(departureDate='" + departureDate + "', depStationCode=" + depStationCode + ") START");
        if (departureDate == null) {
            // На всякий случай
            // Если дата поездки неизвестна, считаем ее невалидной
            Logger.trace(TAG, "checkDate() departureDate == null, return: " + Result.NOT_VALID);
            return Result.NOT_VALID;
        }
        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
        if (controlStation == null) {
            Logger.error(TAG, "checkDate() Date could not be checked without control station");
            throw new IllegalStateException("Date could not be checked without control station");
        }

        Logger.trace(TAG, "checkDate() controlStation=" + controlStation);

        // Время отправления поезда по данным нити поезда, полученной с Базовой станции или
        // текущее время терминала посадки в случае отсутствия связи с Базовой станцией или
        // отсутствия данных нити поезда;
        Date controlDate = controlStation.getDepartureDate();
        if (controlDate == null) {
            controlDate = new Date();
        }
        // ТО (см. controlDate)
        Calendar controlCalendar = Calendar.getInstance();
        controlCalendar.setTime(controlDate);
        // ТПД (время отправления поезда по ПД)
        Calendar departureCalendar = Calendar.getInstance();
        departureCalendar.setTime(departureDate);

        Logger.trace(TAG, "checkDate() controlCalendar=" + controlCalendar.getTime());
        Logger.trace(TAG, "checkDate() departureCalendar=" + departureCalendar.getTime());

        // Если верно: ТО - 1час < ТПД < ТО + 1час, то ПД валиден, посадка разрешена.
        if (betweenShift(departureCalendar, controlCalendar, 1)) {
            return Result.FULLY_VALID;
        }
        // Если верно: ТО - 24часа < ТПД < ТО + 24часа, то ПД предположительно валиден,
        // посадка разрешена по усмотрению проводника. При этом должно быть выдано
        // сообщение: «ВНИМАНИЕ. Проверьте дату и время отправления в ПД».
        if (betweenShift(departureCalendar, controlCalendar, 24)) {
            // Если станция посадки совпадает со станцией контроля, то предупреждаем про расхождение по времени посадки
            // Иначе расхождение по времени не учитываем и считаем что время в порядке
            if (controlStation.getCode() == depStationCode) {
                return Result.PROBABLY_VALID;
            } else {
                return Result.FULLY_VALID;
            }
        }
        // Если верно: ТПД ≤ ТО - 24часа, то ПД не валиден, посадка запрещена.
        // Если верно: ТПД ≥ ТО + 24часа, то ПД не валиден, посадка запрещена.
        // по сути если ТПД не находится между ТО +- 24 часа, то как раз полаются эти условия
        return Result.NOT_VALID;
    }

    /**
     * @return true, если departureCalendar находится строго между controlCalendar +- hours
     */
    private boolean betweenShift(@NonNull Calendar departureCalendar, @NonNull Calendar controlCalendar, int hours) {
        Date backup = controlCalendar.getTime();
        controlCalendar.add(Calendar.HOUR_OF_DAY, -hours);
        boolean before = departureCalendar.before(controlCalendar) || departureCalendar.equals(controlCalendar);
        controlCalendar.setTime(backup);
        controlCalendar.add(Calendar.HOUR_OF_DAY, hours);
        boolean after = departureCalendar.after(controlCalendar) || departureCalendar.equals(controlCalendar);
        controlCalendar.setTime(backup);
        boolean res = !before && !after;
        Logger.trace(TAG, "betweenShift(" +
                "departureCalendar='" + departureCalendar.getTime() + "', " +
                "controlCalendar='" + controlCalendar.getTime() + "', " +
                "hours=" + hours + ") " +
                "return: " + res);
        return res;
    }

    enum Result {

        /**
         * ПД валиден, посадка разрешена
         */
        FULLY_VALID,
        /**
         * ПД предположительно валиден, посадка разрешена по усмотрению проводника.
         * При этом должно быть выдано сообщение: «ВНИМАНИЕ. Проверьте дату и время отправления в ПД».
         */
        PROBABLY_VALID,
        /**
         * ПД не валиден, посадка запрещена
         */
        NOT_VALID

    }

}
