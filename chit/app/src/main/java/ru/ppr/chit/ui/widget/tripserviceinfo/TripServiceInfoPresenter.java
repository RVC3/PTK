package ru.ppr.chit.ui.widget.tripserviceinfo;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.ppr.chit.domain.boarding.BoardingManager;
import ru.ppr.chit.domain.controlstation.ControlStationManager;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.domain.repository.local.UserRepository;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.tripservice.TripServiceManager;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;

/**
 * @author Dmitry Nevolin
 */
class TripServiceInfoPresenter extends BaseMvpViewStatePresenter<TripServiceInfoView, TripServiceInfoViewState> {

    private boolean initialized;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final TripServiceManager tripServiceManager;
    private final UiThread uiThread;
    private final NsiVersionProvider nsiVersionProvider;
    private final StationRepository stationRepository;
    private final BoardingManager boardingManager;
    private final ControlStationManager controlStationManager;
    private final UserRepository userRepository;

    @Inject
    TripServiceInfoPresenter(TripServiceInfoViewState tripServiceInfoViewState,
                             TripServiceInfoStorage tripServiceInfoStorage,
                             TripServiceManager tripServiceManager,
                             UiThread uiThread,
                             NsiVersionProvider nsiVersionProvider,
                             StationRepository stationRepository,
                             BoardingManager boardingManager,
                             ControlStationManager controlStationManager,
                             UserRepository userRepository) {
        super(tripServiceInfoViewState);
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.tripServiceManager = tripServiceManager;
        this.uiThread = uiThread;
        this.nsiVersionProvider = nsiVersionProvider;
        this.stationRepository = stationRepository;
        this.boardingManager = boardingManager;
        this.controlStationManager = controlStationManager;
        this.userRepository = userRepository;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Disposable trainNumberDisposable = tripServiceInfoStorage.getTrainInfoPublisher()
                .observeOn(AppSchedulers.background())
                .subscribe(trainInfoResult -> {
                    TrainInfo trainInfo = trainInfoResult.get();
                    uiThread.post(() -> {
                        view.setTrainNumber(trainInfo == null ? null : trainInfo.getTrainNumber());
                        view.setTrainDepartureDate(trainInfo == null ? null : trainInfo.getDepartureDate());
                    });
                });
        Disposable userDisposable = tripServiceInfoStorage.getUserPublisher()
                .observeOn(AppSchedulers.background())
                .subscribe(userResult -> {
                    User user = loadUser();
                    uiThread.post(() -> view.setUserName(user == null ? null : user.getName()));
                });
        Disposable stationsDisposable = tripServiceInfoStorage.getControlStationPublisher()
                .observeOn(AppSchedulers.background())
                .subscribe(controlStationResult -> {
                    Station controlStation = loadControlStation(controlStationResult.get());
                    Station nextStation = controlStationManager.getNextStation();
                    uiThread.post(() -> {
                        view.setControlStationName(controlStation == null ? null : controlStation.getName());
                        view.setNextStationName(nextStation == null ? null : nextStation.getName());
                    });
                });
        Disposable boardingDisposable = boardingManager.statusChanges()
                .observeOn(AppSchedulers.background())
                .subscribe(status -> {
                    ControlStation controlStation = tripServiceInfoStorage.getControlStation();
                    TripServiceInfoView.BoardingStatus boardingStatus = prepareBoardingStatusForView(status, controlStation);
                    uiThread.post(() -> view.setBoardingStatus(boardingStatus));
                });
        disposables.addAll(trainNumberDisposable, userDisposable, stationsDisposable, boardingDisposable);
    }

    private User loadUser() {
        TripServiceEvent lastTripService = tripServiceManager.getLastTripService();
        if (lastTripService != null && tripServiceManager.isTripServiceStarted()) {
            return lastTripService.getUser(userRepository);
        } else {
            return null;
        }
    }

    private Station loadControlStation(ControlStation controlStation) {
        Station station = null;
        if (controlStation != null) {
            station = stationRepository.load(controlStation.getCode(), nsiVersionProvider.getCurrentNsiVersion());
        }
        return station;
    }

    private TripServiceInfoView.BoardingStatus prepareBoardingStatusForView(BoardingEvent.Status status, ControlStation controlStation) {
        TripServiceInfoView.BoardingStatus boardingStatus = TripServiceInfoView.BoardingStatus.UNKNOWN;
        // Если станция контроля не установлена, то события посадки быть не может, соответственно статус не актуален
        if (controlStation != null) {
            switch (status) {
                case STARTED:
                    boardingStatus = TripServiceInfoView.BoardingStatus.STARTED;
                    break;
                case ENDED:
                    boardingStatus = TripServiceInfoView.BoardingStatus.ENDED;
                    break;
            }
        }
        return boardingStatus;
    }

    @Override
    public void destroy() {
        disposables.clear();
        super.destroy();
    }

}
