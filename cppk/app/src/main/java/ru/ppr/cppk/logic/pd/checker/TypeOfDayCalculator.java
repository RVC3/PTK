package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TypeOfDay;
import ru.ppr.nsi.repository.CalendarRepository;
import ru.ppr.nsi.repository.RegionCalendarRepository;
import ru.ppr.nsi.repository.StationRepository;

/**
 * Калькулятор типа дня.
 *
 * @author Grigoriy Kashka
 */
public class TypeOfDayCalculator {

    private final StationRepository stationRepository;
    private final RegionCalendarRepository regionCalendarRepository;
    private final CalendarRepository calendarRepository;
    private final PrivateSettings privateSettings;

    @Inject
    TypeOfDayCalculator(StationRepository stationRepository,
                        RegionCalendarRepository regionCalendarRepository,
                        CalendarRepository calendarRepository,
                        PrivateSettings privateSettings) {
        this.stationRepository = stationRepository;
        this.regionCalendarRepository = regionCalendarRepository;
        this.calendarRepository = calendarRepository;
        this.privateSettings = privateSettings;
    }

    @NonNull
    public TypeOfDay getTypeOfDay(@NonNull Date date, int nsiVersion) {
        long currentStationCode = privateSettings.getCurrentStationCode();
        Station currentStation = stationRepository.load(currentStationCode, nsiVersion);
        Integer currentRegionCode = (currentStation == null) ? null : currentStation.getRegionCode();

        TypeOfDay dayType = TypeOfDay.UNKNOWN;

        if (currentRegionCode != null) {
            dayType = regionCalendarRepository.getTypeOfDateFromRegionCalendar(date, currentRegionCode, nsiVersion);
        }

        if (dayType != TypeOfDay.UNKNOWN) {
            // Тип дня определен по региональному кадендарю
            return dayType;
        }

        // Если тип дня не определен по региональному календарю - ищем по обычному
        // http://agile.srvdev.ru/browse/CPPKPP-36483
        dayType = calendarRepository.getTypeOfDateFromCalendarTable(date, nsiVersion);

        if (dayType != TypeOfDay.UNKNOWN) {
            // Тип дня определен по обычному кадендарю
            return dayType;
        }

        // Опираясь на код с кассы: там никогда не придёт неизвестное значение,
        // если ни в одной таблице нет данного дня - пытаемся определить доступными средствами
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            dayType = TypeOfDay.SATURDAY;
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            dayType = TypeOfDay.SUNDAY;
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            dayType = TypeOfDay.PRE_HOLIDAY;
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            dayType = TypeOfDay.POST_HOLIDAY;
        } else {
            dayType = TypeOfDay.WORKING_DAY;
        }

        return dayType;
    }
}
