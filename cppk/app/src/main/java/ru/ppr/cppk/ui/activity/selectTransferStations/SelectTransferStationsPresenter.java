package ru.ppr.cppk.ui.activity.selectTransferStations;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.local.repository.PrivateSettingsRepository;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.transfer.TransferStationsLoader;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.repository.StationRepository;
import rx.Completable;


/**
 * @author Grigoriy Kashka
 */
class SelectTransferStationsPresenter extends BaseMvpViewStatePresenter<SelectTransferStationsView, SelectTransferStationsViewState> {

    private static final String TAG = Logger.makeLogTag(SelectTransferStationsPresenter.class);

    private final UiThread uiThread;
    private final PrivateSettings privateSettings;
    private final StationRepository stationRepository;
    private final NsiVersionManager nsiVersionManager;
    private final TransferStationsLoader transferStationsLoader;
    private final PrivateSettingsRepository privateSettingsRepository;
    private final Globals app;
    private Navigator navigator;

    /**
     * Станция отправления
     */
    private Station departureStation = null;
    /**
     * Станция назначения
     */
    private Station destinationStation = null;
    /**
     * Станции отправления в выпадающем списке
     */
    private List<Station> departureStations = Collections.emptyList();
    /**
     * Станции назначения в выпадающем списке
     */
    private List<Station> destinationStations = Collections.emptyList();
    /**
     * Кешированный список стацний отправления в выпадающем списке без фильтра по введенным символам.
     * Используется для быстрого обновления выпадающего списка напрямую из UI-потока при отмене выбора станции.
     */
    private List<Station> departureStationsWithoutFilter = null;
    /**
     * Кешированный список стацний назначения в выпадающем списке без фильтра по введенным символам.
     * Используется для быстрого обновления выпадающего списка напрямую из UI-потока при отмене выбора станции.
     */
    private List<Station> destinationStationsWithoutFilter = null;

    @Inject
    SelectTransferStationsPresenter(SelectTransferStationsViewState viewState,
                                    PrivateSettings privateSettings,
                                    StationRepository stationRepository,
                                    UiThread uiThread,
                                    NsiVersionManager nsiVersionManager,
                                    TransferStationsLoader transferStationsLoader,
                                    PrivateSettingsRepository privateSettingsRepository,
                                    Globals app) {
        super(viewState);
        this.privateSettings = privateSettings;
        this.stationRepository = stationRepository;
        this.uiThread = uiThread;
        this.nsiVersionManager = nsiVersionManager;
        this.transferStationsLoader = transferStationsLoader;
        this.privateSettingsRepository = privateSettingsRepository;
        this.app = app;
    }

    public void setNavigator(SelectTransferStationsPresenter.Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    protected void onInitialize2() {
        // Устанавливаем текущие станции в поля ввода
        long[] ptkRouteStationCodes = privateSettings.getTransferRouteStationsCodes();
        Logger.trace(TAG, "routeDepStation code = " + ptkRouteStationCodes[0]);
        Logger.trace(TAG, "routeDestStation code = " + ptkRouteStationCodes[1]);
        Station depStation = stationRepository.load(ptkRouteStationCodes[0], nsiVersionManager.getCurrentNsiVersionId());
        Station destStation = stationRepository.load(ptkRouteStationCodes[1], nsiVersionManager.getCurrentNsiVersionId());
        if (depStation != null) {
            setDepartureStation(depStation);
        }
        if (destStation != null) {
            setDestinationStation(destStation);
        }
        // Обновляем список возможных станций отправления
        updateDepartureStations("");
    }

    List<Station> onDepartureStationTextChanged(String text) {
        Logger.trace(TAG, "onDepartureStationTextChanged, text = " + text);
        updateDepartureStations(text);
        return departureStations;
    }

    List<Station> onDestinationStationTextChanged(String text) {
        Logger.trace(TAG, "onDestinationStationTextChanged, text = " + text);
        updateDestinationStations(text);
        return destinationStations;
    }

    void onDepartureStationEditCanceled() {
        view.setDepartureStationName(departureStation == null ? "" : departureStation.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDepartureStations("");
    }

    void onDestinationStationEditCanceled() {
        view.setDestinationStationName(destinationStation == null ? "" : destinationStation.getName());
        // Вернем полный список станций
        // За счет кешированного списка без фильтра можем позолить себе сделать операцию в UI
        updateDestinationStations("");
    }

    void onDepartureStationSelected(int position) {
        Logger.trace(TAG, "onDepartureStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showProgress);
                    setDepartureStation(departureStations.get(position));
                    uiThread.post(view::hideProgress);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onDestinationStationSelected(int position) {
        Logger.trace(TAG, "onDestinationStationSelected, position = " + position);
        Completable
                .fromAction(() -> {
                    uiThread.post(view::showProgress);
                    setDestinationStation(destinationStations.get(position));
                    uiThread.post(view::hideProgress);
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe();
    }

    void onCloseBtnClicked() {
        Logger.trace(TAG, "onCloseBtnClicked");
        navigator.navigateBack();
    }

    private void updateDepartureStations(@NonNull String filter) {
        final String likeQuery = filter.toUpperCase(Locale.getDefault());

        if ("".equals(filter) && departureStationsWithoutFilter != null) {
            // Используем кешированный список
            departureStations = departureStationsWithoutFilter;
        } else {
            departureStations = transferStationsLoader.loadTransferDepartureStations(nsiVersionManager.getCurrentNsiVersionId(), likeQuery);
            if ("".equals(filter)) {
                // Кешируем список станций
                departureStationsWithoutFilter = departureStations;
            }
        }
        uiThread.post(() -> view.setDepartureStations(departureStations));
    }

    private void updateDestinationStations(@NonNull String filter) {
        final String likeQuery = filter.toUpperCase(Locale.getDefault());

        if (departureStation == null) {
            // Если не выбрана станция отправления,
            // очищаем список для выбора станции назначения
            destinationStations = Collections.emptyList();
        } else if ("".equals(filter) && destinationStationsWithoutFilter != null) {
            // Используем кешированный список
            destinationStations = destinationStationsWithoutFilter;
        } else {
            destinationStations = transferStationsLoader.loadTransferDestinationStations(departureStation.getCode(), nsiVersionManager.getCurrentNsiVersionId(), likeQuery);
            if ("".equals(filter)) {
                // Кешируем список станций
                destinationStationsWithoutFilter = destinationStations;
            }
        }
        uiThread.post(() -> view.setDestinationStations(destinationStations));
    }

    private void setDepartureStation(Station station) {
        // Устнавливаем выбранную станцию
        departureStation = station;
        uiThread.post(() -> view.setDepartureStationName(departureStation == null ? "" : departureStation.getName()));
        // Возвращаем полный список станций отправления
        updateDepartureStations("");
        // Очищаем кешированный список станций наначения
        destinationStationsWithoutFilter = null;
        // Обновляем список станций назначения
        updateDestinationStations("");
        // Сбрасываем выбранную станцию назначения
        setDestinationStation(null);
    }

    private void setDestinationStation(Station station) {
        // Устнавливаем выбранную станцию
        destinationStation = station;
        uiThread.post(() -> view.setDestinationStationName(destinationStation == null ? "" : destinationStation.getName()));
        // Возвращаем полный список станций назначения
        updateDestinationStations("");
        // Сохраняем маршрут
        saveRoute();
    }

    private void updateTransferDepartureDateTime() {
        Logger.trace(TAG, "updateTransferDepartureDateTime");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date transferDepartureDateTime = calendar.getTime();
        SharedPreferencesUtils.setTransferDepartureDateTime(app, transferDepartureDateTime);
    }

    private void saveRoute() {
        Logger.trace(TAG, "saveRoute");
        if (departureStation != null && destinationStation != null) {
            long[] codes = {(long) departureStation.getCode(), (long) destinationStation.getCode()};
            privateSettings.setTransferRouteStationsCodes(codes);
            privateSettingsRepository.savePrivateSettings(privateSettings);

            // http://agile.srvdev.ru/browse/CPPKPP-42649
            // Требование: При изменении станции отправления (нажатии на наименование станции)
            // поля «Дата отправления» и «Время отправления» заполняются текущими значениями даты и времени - по настройкам ПТК
            updateTransferDepartureDateTime();
        }
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface Navigator {
        void navigateBack();
    }

}
