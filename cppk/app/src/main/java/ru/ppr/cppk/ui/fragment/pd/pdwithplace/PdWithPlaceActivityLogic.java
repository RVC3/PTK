package ru.ppr.cppk.ui.fragment.pd.pdwithplace;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdForDays;
import ru.ppr.core.dataCarrier.pd.base.PdWithPlace;
import ru.ppr.core.dataCarrier.pd.v10.PdV10;
import ru.ppr.core.dataCarrier.pd.v9.PdV9;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.controlbarcodestorage.PdControlBarcodeData;
import ru.ppr.cppk.helpers.controlbarcodestorage.PdControlBarcodeDataStorage;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardDataStorage;
import ru.ppr.cppk.ui.fragment.pd.pdwithplace.model.PdWithPlaceViewModel;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketType;

/**
 * Временное решение по выносу логики из {@link PdWithPlaceFragment}
 *
 * @author Dmitry Vinogradov
 */
@Deprecated
public class PdWithPlaceActivityLogic {

    private static final String TAG = Logger.makeLogTag(PdWithPlaceActivityLogic.class);

    @Inject
    PdControlCardDataStorage pdControlCardDataStorage;
    @Inject
    NsiDaoSession nsiDaoSession;
    @Inject
    PdControlBarcodeDataStorage pdControlBarcodeDataStorage;

    private final Activity activity;

    public PdWithPlaceActivityLogic(Activity activity,
                                    PdWithPlaceFragment pdWithPlaceFragment) {

        Dagger.appComponent().inject(this);
        this.activity = activity;
        pdWithPlaceFragment.setPdViewModel(createTicketPdViewModel());

    }

    private PdWithPlaceViewModel createTicketPdViewModel() {

        PdControlCardData pdControlCardData = pdControlCardDataStorage.getLastCardData();
        PdControlBarcodeData pdControlBarcodeData = pdControlBarcodeDataStorage.getLastBarcodeData();

        Pd pd = null;
        if (pdControlBarcodeData != null) {
            pd = pdControlBarcodeData.getPd();
        } else if (pdControlCardData != null) {
            List<Pd> pdList = pdControlCardData.getPdList();
            pd = pdList.get(0);
        }

        PdWithPlace pdWithPlace = null;
        if (pd instanceof PdV9) {
            pdWithPlace = (PdV9) pd;
        } else if (pd instanceof PdV10) {
            pdWithPlace = (PdV10) pd;
        }

        if (pdWithPlace == null) {
            Logger.info(TAG, "Не удалось получить ПД с местом");
            return null;
        }

        int nsiVersion = Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId();
        PdWithPlaceViewModel viewModel = new PdWithPlaceViewModel();

        int orderNumber = pdWithPlace.getOrderNumber();
        int trainNumber = pdWithPlace.getTrainNumber();
        String trainLetter = trimNeedlessSymbols(pdWithPlace.getTrainLetter());
        int placeNumber = pdWithPlace.getPlaceNumber();
        String placeLetter = trimNeedlessSymbols(pdWithPlace.getPlaceLetter());
        int wagonNumber = pdWithPlace.getWagonNumber();
        int departureDayOffset = pdWithPlace.getDepartureDayOffset();
        int departureTime = pdWithPlace.getDepartureTime();
        int forDays = pdWithPlace instanceof PdForDays ? ((PdForDays) pdWithPlace).getForDays() : 1;
        String documentNumber = trimNeedlessSymbols(pdWithPlace.getDocumentNumber());
        long departureStationCode = pdWithPlace.getDepartureStationCode();
        long destinationStationCode = pdWithPlace.getDestinationStationCode();
        long ticketTypeCode = pdWithPlace.getTicketTypeCode();
        String lastName = trimNeedlessSymbols(pdWithPlace.getLastName());
        String firstNameInitial = trimNeedlessSymbols(pdWithPlace.getFirstNameInitial());
        String secondNameInitial = trimNeedlessSymbols(pdWithPlace.getSecondNameInitial());
        Date saleDate = pdWithPlace.getSaleDateTime();

        // Получаем наименование типа билета
        TicketType ticketType = nsiDaoSession.getTicketTypeDao().load((int) ticketTypeCode, nsiVersion);
        // Получаем станции отправления и назначения
        Station departureStation = nsiDaoSession.getStationDao().load(departureStationCode, nsiVersion);
        Station destinationStation = nsiDaoSession.getStationDao().load(destinationStationCode, nsiVersion);
        // Получаем дату и время отправления
        List<Date> departureDates = calc(saleDate, departureDayOffset, departureTime, forDays);
        Date departureDate = departureDates.get(0);
        // Получаем полное имя пассажира
        String passengerName = lastName;
        if (firstNameInitial != null && firstNameInitial.length() > 0) {
            passengerName = passengerName + " " + firstNameInitial + ".";
        }
        if (secondNameInitial != null && secondNameInitial.length() > 0) {
            passengerName = passengerName + " " + secondNameInitial + ".";
        }
        // Получаем номер документа
        String documentNumberString = "***" + documentNumber;
        // Получаем номер поезда
        String trainNumberString = Integer.toString(trainNumber);
        if (trainLetter != null && trainLetter.length() > 0) {
            trainNumberString = trainNumberString + trainLetter;
        }
        // Получаем номер вагона
        String wagonNumberString = Integer.toString(wagonNumber);
        // Получаем номер места
        String placeNumberString = Integer.toString(placeNumber);
        if (placeLetter != null && placeLetter.length() > 0) {
            placeNumberString = placeNumberString + placeLetter;
        }

        viewModel.setTitle(ticketType.toString());
        viewModel.setNumber(orderNumber);
        viewModel.setDepStationName(departureStation.getName());
        viewModel.setDestStationName(destinationStation.getName());
        viewModel.setDepartureDate(departureDate);
        viewModel.setPassengerName(passengerName);
        viewModel.setDocumentNumber(documentNumberString);
        viewModel.setTrainNumber(trainNumberString);
        viewModel.setWagonNumber(wagonNumberString);
        viewModel.setPlaceNumber(placeNumberString);

        return viewModel;
    }

    /**
     * Удаляет ненужные символы из строки, как правило, это пустые символы
     */
    private String trimNeedlessSymbols(String str) {
        if (str == null) {
            return null;
        }
        // Вырезаем ненужные символы
        str = str.replace("\u0000", "");
        str = str.replace("\\u0000", "");
        str = str.trim();
        return str;
    }

    /**
     * @param saleDateTime       Дата продажи ПД
     * @param departureDayOffset Дата отправления: количество дней с даты продажи. Значение 0: дата отправления соответствует дате продажи.
     * @param departureTime      Время отправления с точностью до минуты - количество минут с полуночи. Минимум: 0 - 00:00, максимум: 1439 - 23:59
     * @param forDays            Даты действия ПД. Каждому биту соответствует один из дней.
     *                           Нулевой бит - это дата первого отправления, он всегда = 1. остальные биты - последующие дни. Для разового ПД в единицу установлен только нулевой бит.
     * @return Даты отправления
     */
    @NonNull
    private List<Date> calc(Date saleDateTime, int departureDayOffset, int departureTime, int forDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Отталкиваемся от даты продажи
        calendar.setTime(saleDateTime);
        // Смещаем дату отправления на нужное количество дней
        calendar.add(Calendar.DAY_OF_MONTH, departureDayOffset);
        // Сбрасываем время
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // Устанавливаем нужное время в минутах
        calendar.add(Calendar.MINUTE, departureTime);
        return Collections.singletonList(calendar.getTime());
    }
}
