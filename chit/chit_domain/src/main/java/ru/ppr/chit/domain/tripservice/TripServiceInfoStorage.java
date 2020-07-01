package ru.ppr.chit.domain.tripservice;

import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;
import ru.ppr.chit.domain.model.local.CarInfo;
import ru.ppr.chit.domain.model.local.CarScheme;
import ru.ppr.chit.domain.model.local.CarSchemeElement;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.model.local.StationInfo;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.repository.local.CarInfoRepository;
import ru.ppr.chit.domain.repository.local.CarSchemeElementRepository;
import ru.ppr.chit.domain.repository.local.CarSchemeRepository;
import ru.ppr.chit.domain.repository.local.ControlStationRepository;
import ru.ppr.chit.domain.repository.local.StationInfoRepository;
import ru.ppr.chit.domain.repository.local.TrainInfoRepository;
import ru.ppr.chit.domain.repository.local.UserRepository;
import ru.ppr.utils.Ref;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class TripServiceInfoStorage {

    private final Object lock = new Object();
    private final BehaviorSubject<Ref<TrainInfo>> trainInfoPublisher = BehaviorSubject.create();
    private final BehaviorSubject<Ref<User>> userPublisher = BehaviorSubject.create();
    private final BehaviorSubject<Ref<ControlStation>> controlStationPublisher = BehaviorSubject.create();
    private final TrainInfoRepository trainInfoRepository;
    private final StationInfoRepository stationInfoRepository;
    private final CarInfoRepository carInfoRepository;
    private final CarSchemeRepository carSchemeRepository;
    private final CarSchemeElementRepository carSchemeElementRepository;
    private final LocalDbTransaction localDbTransaction;
    private final ControlStationRepository controlStationRepository;
    private final UserRepository userRepository;
    private TrainInfo cachedTrainInfo;
    private User cachedUser;
    private ControlStation cachedControlStation;

    @Inject
    TripServiceInfoStorage(TrainInfoRepository trainInfoRepository,
                           StationInfoRepository stationInfoRepository,
                           CarInfoRepository carInfoRepository,
                           CarSchemeRepository carSchemeRepository,
                           CarSchemeElementRepository carSchemeElementRepository,
                           LocalDbTransaction localDbTransaction,
                           ControlStationRepository controlStationRepository,
                           UserRepository userRepository) {

        this.trainInfoRepository = trainInfoRepository;
        this.stationInfoRepository = stationInfoRepository;
        this.carInfoRepository = carInfoRepository;
        this.carSchemeRepository = carSchemeRepository;
        this.carSchemeElementRepository = carSchemeElementRepository;
        this.localDbTransaction = localDbTransaction;
        this.controlStationRepository = controlStationRepository;
        this.userRepository = userRepository;
    }

    public void init() {
        trainInfoPublisher.onNext(new Ref<>(getTrainInfo()));
        userPublisher.onNext(new Ref<>(getUser()));
        controlStationPublisher.onNext(new Ref<>(getControlStation()));
    }

    @Nullable
    public TrainInfo getTrainInfo() {
        TrainInfo local = cachedTrainInfo;
        if (local == null) {
            synchronized (lock) {
                if (cachedTrainInfo == null) {
                    cachedTrainInfo = trainInfoRepository.loadLastNotLegacy();
                }
                local = cachedTrainInfo;
            }
        }
        return local;
    }

    @Nullable
    public User getUser() {
        User local = cachedUser;
        if (local == null) {
            synchronized (lock) {
                if (cachedUser == null) {
                    cachedUser = userRepository.loadLast();
                }
                local = cachedUser;
            }
        }
        return local;
    }

    @Nullable
    public ControlStation getControlStation() {
        ControlStation local = cachedControlStation;
        if (local == null) {
            synchronized (lock) {
                if (cachedControlStation == null) {
                    cachedControlStation = controlStationRepository.loadFirst();
                }
                local = cachedControlStation;
            }
        }
        return local;
    }

    public Observable<Ref<TrainInfo>> getTrainInfoPublisher() {
        return trainInfoPublisher;
    }

    public Observable<Ref<User>> getUserPublisher() {
        return userPublisher;
    }

    public Observable<Ref<ControlStation>> getControlStationPublisher() {
        return controlStationPublisher;
    }

    public void updateTrainInfo(TrainInfo trainInfo) {
        synchronized (lock) {
            updateTrainInfoInternal(trainInfo);
            cachedTrainInfo = trainInfo;
        }
        trainInfoPublisher.onNext(new Ref<>(cachedTrainInfo));
    }

    public void updateUser(User user) {
        synchronized (lock) {
            userRepository.insert(user);
            cachedUser = user;
        }
        userPublisher.onNext(new Ref<>(cachedUser));
    }

    public void updateControlStation(ControlStation controlStation) {
        synchronized (lock) {
            updateControlStationInternal(controlStation);
            cachedControlStation = controlStation;
        }
        controlStationPublisher.onNext(new Ref<>(cachedControlStation));
    }

    public void clearControlStation() {
        synchronized (lock) {
            ControlStation controlStation = getControlStation();
            if (controlStation != null) {
                controlStationRepository.delete(controlStation);
                clearControlStationCache();
            }
        }
        controlStationPublisher.onNext(new Ref<>(getControlStation()));
    }

    public void clearCache() {
        synchronized (lock) {
            clearTrainInfoCache();
            clearUserCache();
            clearControlStationCache();
        }
        trainInfoPublisher.onNext(new Ref<>(getTrainInfo()));
        userPublisher.onNext(new Ref<>(getUser()));
        controlStationPublisher.onNext(new Ref<>(getControlStation()));
    }

    private void clearTrainInfoCache() {
        cachedTrainInfo = null;
    }

    private void clearUserCache() {
        cachedUser = null;
    }

    public void clearControlStationCache() {
        cachedControlStation = null;
    }

    private void updateTrainInfoInternal(TrainInfo trainInfo) {
        try {
            localDbTransaction.begin();
            trainInfoRepository.insert(trainInfo);
            List<StationInfo> stations = trainInfo.getStations(stationInfoRepository);
            if (stations != null) {
                stationInfoRepository.insertAll(stations, trainInfo.getId());
            }
            List<CarInfo> cars = trainInfo.getCars(carInfoRepository);
            if (cars != null) {
                for (CarInfo carInfo : cars) {
                    Long schemeId = null;
                    CarScheme scheme = carInfo.getScheme(carSchemeRepository);
                    if (scheme != null) {
                        schemeId = carSchemeRepository.insert(scheme);
                        List<CarSchemeElement> elements = scheme.getElements(carSchemeElementRepository);
                        if (elements != null) {
                            carSchemeElementRepository.insertAll(elements, schemeId);
                        }
                    }
                    carInfo.setSchemeId(schemeId);
                }
                carInfoRepository.insertAll(cars, trainInfo.getId());
            }
            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }

    private void updateControlStationInternal(ControlStation controlStation) {
        ControlStation loaded = getControlStation();
        if (loaded != null) {
            // текущая станция контроля должна быть только одна
            controlStation.setId(loaded.getId());
            controlStationRepository.update(controlStation);
        } else {
            controlStationRepository.insert(controlStation);
        }
    }

}
