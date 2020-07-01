package ru.ppr.chit.ui.activity.main;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.ppr.chit.domain.boarding.BoardingManager;
import ru.ppr.chit.domain.controlstation.ControlStationManager;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.ticket.TicketManager;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.tripservice.TripServiceManager;
import ru.ppr.chit.domain.tripservice.TripServiceMode;
import ru.ppr.chit.domain.tripservice.TripServiceModeManager;
import ru.ppr.core.exceptions.UserException;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class MainPresenter extends BaseMvpViewStatePresenter<MainView, MainViewState> {

    private static final String TAG = Logger.makeLogTag(MainPresenter.class);

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final BoardingManager boardingManager;
    private final TripServiceManager tripServiceManager;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final ControlStationManager controlStationManager;
    private final TicketManager ticketManager;
    private final TripServiceModeManager tripServiceModeManager;
    //endregion
    //region Other
    private CompositeDisposable disposables = new CompositeDisposable();
    private Navigator navigator;
    /**
     * Блокирует многократное нажатие на кнопки начала/окончания поездки.
     */
    private boolean boardingStatusChangeRequested = false;
    /**
     * Блокирует многократное нажатие на кнопку завршения обслуживания.
     */
    private boolean endTripServiceRequested = false;
    //endregion

    @Inject
    MainPresenter(MainViewState mainViewState,
                  BoardingManager boardingManager,
                  TripServiceManager tripServiceManager,
                  TripServiceInfoStorage tripServiceInfoStorage,
                  ControlStationManager controlStationManager,
                  TicketManager ticketManager,
                  TripServiceModeManager tripServiceModeManager) {
        super(mainViewState);
        this.boardingManager = boardingManager;
        this.tripServiceManager = tripServiceManager;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.controlStationManager = controlStationManager;
        this.ticketManager = ticketManager;
        this.tripServiceModeManager = tripServiceModeManager;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        // За посадками имеет смысл следить только в онлайн режиме
        if (tripServiceModeManager.detectTripServiceMode() == TripServiceMode.ONLINE) {
            Disposable boardingStatusChanges = boardingManager
                    .statusChanges()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onBoardingStatusChanged);
            disposables.add(boardingStatusChanges);
        } else {
            // В оффлайн-режиме просто показываем кнопку завершения поездки
            view.setStartBoardingBtnVisible(false);
            view.setEndBoardingBtnVisible(false);
            view.setEndTripServiceBtnVisible(true);
        }
        Disposable ticketListChanges = ticketManager
                .getTicketListPublisher()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onTicketListChanged);
        disposables.add(ticketListChanges);
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void onReadBscBtnClicked() {
        Logger.trace(TAG, "onReadBscBtnClicked");
        navigator.navigateToReadBsc();
    }

    void onReadBarcodeBtnClicked() {
        Logger.trace(TAG, "onReadBarcodeBtnClicked");
        navigator.navigateToReadBarcode();
    }

    void onPassengerListBtnClicked() {
        Logger.trace(TAG, "onPassengerListBtnClicked");
        navigator.navigateToPassengerList();
    }

    void onMenuBtnClicked() {
        Logger.trace(TAG, "onMenuBtnClicked");
        navigator.navigateToMenu();
    }

    void onStartBoardingBtnClicked() {
        Logger.trace(TAG, "onStartBoardingBtnClicked: " + boardingStatusChangeRequested);
        if (!boardingStatusChangeRequested) {
            boardingStatusChangeRequested = true;
            try {
                boardingManager.startBoarding();
            } catch (Exception e){
                Logger.error(TAG, "onStartBoardingBtnClicked() error", e);
                view.showError(UserException.wrap(e).getMessage());
            }
       }
    }

    void onEndBoardingBtnClicked() {
        Logger.trace(TAG, "onEndBoardingBtnClicked: " + boardingStatusChangeRequested);
        if (!boardingStatusChangeRequested) {
            boardingStatusChangeRequested = true;
            try {
                boardingManager.endBoarding();
            } catch (Exception e){
                Logger.error(TAG, "onEndBoardingBtnClicked() error", e);
                view.showError(UserException.wrap(e).getMessage());
            }
        }
    }

    void onEndTripServiceBtnClicked() {
        Logger.trace(TAG, "onEndTripServiceBtnClicked: " + endTripServiceRequested);
        if (!endTripServiceRequested) {
            endTripServiceRequested = true;
            try {
                tripServiceManager.endTripService();
                navigator.navigateToWelcome();
            } catch (Exception e){
                Logger.error(TAG, "onEndTripServiceBtnClicked() error", e);
                view.showError(UserException.wrap(e).getMessage());
            }
        }
    }

    private void onBoardingStatusChanged(BoardingEvent.Status status) {
        Logger.trace(TAG, "onBoardingStatusChanged(status): " + status);
        switch (status) {
            case ENDED: {
                // Если следующая станция - последняя в нити поезда (конечная), значит там
                // сажать людей нельзя, показываем кнопку завершения обслуживания
                boolean isNextStationLast = controlStationManager.isNextStationLast();
                view.setStartBoardingBtnVisible(!isNextStationLast);
                view.setEndBoardingBtnVisible(false);
                view.setEndTripServiceBtnVisible(isNextStationLast);
                break;
            }
            case STARTED: {
                view.setStartBoardingBtnVisible(false);
                view.setEndBoardingBtnVisible(true);
                view.setEndTripServiceBtnVisible(false);
            }
        }
        boardingStatusChangeRequested = false;
    }

    private void onTicketListChanged(List<Ticket> ticketList) {
        Logger.trace(TAG, "onTicketListChanged(ticketList): " + ticketList.size());
        view.setPassengerListBtnEnabled(!ticketList.isEmpty());
    }

    @Override
    public void destroy() {
        disposables.clear();
        super.destroy();
    }

    interface Navigator {

        void navigateToReadBsc();

        void navigateToReadBarcode();

        void navigateToMenu();

        void navigateToPassengerList();

        void navigateToWelcome();

    }

}
