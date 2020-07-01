package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.dialog;

import android.support.annotation.NonNull;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParams;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.ui.activity.transfersalestart.model.TransferSaleData;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import rx.Completable;

/**
 * @author Dmitry Nevolin
 */

public class TripOpeningClosurePresenter extends BaseMvpViewStatePresenter<TripStartView, TripValidityStartView> {

    private static final String TAG = Logger.makeLogTag(TripOpeningClosurePresenter.class);

    private final NsiVersionManager nsiVersionManager;
    private final UiThread uiThread;
    private final PdSaleEnv pdSaleEnv;
    private final PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder;
    private final TransferSaleData transferSaleData;
    /**
     * Версия НСИ для оформления ПД
     */
    private int nsiVersion;
    /**
     * Дата начала процесса оформления ПД
     */
    private Date timestamp;
    /**
     * Стацнии отправления в выпадающем списке
     */
    private List<Station> departureStations = Collections.emptyList();
    /**
     * Кешированный список стацний отправления в выпадающем списке без фильтра по введенным символам.
     * Используется для быстрого обновления выпадающего списка напрямую из UI-потока при отмене выбора станции.
     */
    private List<Station> departureStationsWithoutFilter = null;


    @Inject
    TripOpeningClosurePresenter(@NonNull TripValidityStartView tripValidityStartView,
                               @NonNull NsiVersionManager nsiVersionManager,
                               @NonNull UiThread uiThread,
                               @NonNull PdSaleEnvFactory pdSaleEnvFactory,
                               @NonNull PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder,
                               @NonNull TransferSaleData transferSaleData) {
        super(tripValidityStartView);
        this.nsiVersionManager = nsiVersionManager;
        this.uiThread = uiThread;
        this.pdSaleEnv = pdSaleEnvFactory.pdSaleEnvForSinglePd();
        this.pdSaleRestrictionsParamsBuilder = pdSaleRestrictionsParamsBuilder;
        this.transferSaleData = transferSaleData;
    }


    @Override
    protected void onInitialize2() {
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showLoadingDialog);

                    timestamp = new Date();
                    nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

                    PdSaleRestrictionsParams pdSaleRestrictionsParams = pdSaleRestrictionsParamsBuilder.create(timestamp, nsiVersion);
                    pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParams);
                    // Устанавливаем список станций и выбранные станции
                    initializeStations();
                    uiThread.post(view::hideLoadingDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    private void initializeStations() {
        Logger.trace(TAG, "initializeStations");
        // Обновляем станции в выпадающих списках
        updateDepartureStations("");
    }

    List<Station> onDepartureStationTextChanged(String text) {
        Logger.trace(TAG, "onDepartureStationTextChanged, text = " + text);
        updateDepartureStations(text);
        return departureStations;
    }


    void onDepartureStationSelected(int position) {
        Logger.trace(TAG, "onDepartureStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showLoadingDialog);
                    setDepartureStation(departureStations.get(position));
                    updateContinueBtnEnable();
                    uiThread.post(view::hideLoadingDialog);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onDepartureStationEditCanceled() {
        Station station = transferSaleData.getDepartureStation();
        view.setDepartureStationName(station == null ? "" : station.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDepartureStations("");
    }

    Integer getIdSelectStation(){
      return (transferSaleData.getDepartureStation() == null)? null : transferSaleData.getDepartureStation().getCode();
    }

    void onDestinationStationEditCanceled() {
        Station station = transferSaleData.getDestinationStation();
        view.setDestinationStationName(station == null ? "" : station.getName());
        // Вернем полный список станций
    }


    private List<Station> findDepartureStations(@NonNull String filter) {
        String likeQuery = filter.toUpperCase(Locale.getDefault());
        return pdSaleEnv.depStationsLoader().loadAllStations(
                null,
                null,
                likeQuery);
    }


    private void updateDepartureStations(@NonNull String filter) {
        String likeQuery = filter.toUpperCase(Locale.getDefault());

        if ("".equals(filter) && departureStationsWithoutFilter != null) {
            // Используем кешированный список
            departureStations = departureStationsWithoutFilter;
        } else {
            departureStations = findDepartureStations(likeQuery);

            if ("".equals(filter)) {
                // Кешируем список станций
                departureStationsWithoutFilter = departureStations;
            }
        }

        uiThread.post(() -> view.setDepartureStations(departureStations));
    }


    private void setDepartureStation(Station station) {
        // Устнавливаем выбранную станцию
        transferSaleData.setDepartureStation(station);
        uiThread.post(() -> view.setDepartureStationName(transferSaleData.getDepartureStation() == null ? "" : transferSaleData.getDepartureStation().getName()));
        // Возвращаем полный список станций отправления
        updateDepartureStations("");
        // Сбрасываем выбранную станцию назначения
        setDestinationStation(null);
    }

    private void setDestinationStation(Station station) {
        // Устнавливаем выбранную станцию
        transferSaleData.setDestinationStation(station);
        uiThread.post(() -> view.setDestinationStationName(transferSaleData.getDestinationStation() == null ? "" : transferSaleData.getDestinationStation().getName()));
        // Обновляем видимость кнопки "Продолжить"
        updateContinueBtnEnable();
    }

    private void updateContinueBtnEnable() {
        uiThread.post(() -> view.setContinueBtnEnable(transferSaleData.getDestinationStation() == null));
    }
}
