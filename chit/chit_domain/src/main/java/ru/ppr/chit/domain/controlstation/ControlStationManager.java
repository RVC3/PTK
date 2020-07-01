package ru.ppr.chit.domain.controlstation;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.core.exceptions.UserCriticalException;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.model.local.StationInfo;
import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.domain.repository.local.StationInfoRepository;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ObjectUtils;

/**
 * Менеджер станции контроля.
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class ControlStationManager {

    private static final String TAG = Logger.makeLogTag(ControlStationManager.class);

    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final StationInfoRepository stationInfoRepository;
    private final StationRepository stationRepository;
    private final NsiVersionProvider nsiVersionProvider;

    @Inject
    ControlStationManager(TripServiceInfoStorage tripServiceInfoStorage,
                          StationInfoRepository stationInfoRepository,
                          StationRepository stationRepository,
                          NsiVersionProvider nsiVersionProvider) {
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.stationInfoRepository = stationInfoRepository;
        this.stationRepository = stationRepository;
        this.nsiVersionProvider = nsiVersionProvider;
    }

    /**
     * Задаёт новую станцию контроля на основе текущей и информации о нити поезда
     * и сохраняет ее в базу данных
     *
     * @return возвращает новую станцию контроля
     */
    public void setNextControlStation() throws Exception {
        Logger.trace(TAG, "setNextControlStation");
        TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
        if (trainInfo == null){
            throw new UserCriticalException("Невозможно установить станцию контроля, так как не задана нить поезда");
        }

        List<StationInfo> stationInfoList = trainInfo.getStations(stationInfoRepository);
        if (stationInfoList == null || stationInfoList.isEmpty()) {
            throw new UserCriticalException("Невозможно установить станцию контроля, не заданы станции маршрута поезда");
        }

        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
        // Если текущей стнации контроля нет на маршруте, то считаем что она не задана
        if (controlStation != null && !isStationExists(stationInfoList, controlStation.getCode())){
            controlStation = null;
        }
        // Если не задана станция контроля, задаём первую из нити
        if (controlStation == null ) {
            StationInfo stationInfo = stationInfoList.get(0);
            controlStation = new ControlStation();
            controlStation.setCode(stationInfo.getCode());
            controlStation.setDepartureDate(stationInfo.getDepartureDate());
            tripServiceInfoStorage.updateControlStation(controlStation);
            return;
        } else {
            for (int i = 0; i < stationInfoList.size(); i++) {
                StationInfo stationInfo = stationInfoList.get(i);
                // Находим текущую станцию в нити поезд
                if (ObjectUtils.equals(controlStation.getCode(), stationInfo.getCode())) {
                    // Если следующая станция доступна, то заменяем текущую на неё
                    if (i + 1 < stationInfoList.size()) {
                        StationInfo nextStationInfo = stationInfoList.get(i + 1);
                        controlStation.setCode(nextStationInfo.getCode());
                        controlStation.setDepartureDate(nextStationInfo.getDepartureDate());
                        tripServiceInfoStorage.updateControlStation(controlStation);
                        return;
                    } else {
                        // Если текущая станцию контроля последняя, то ругаемся
                        throw new UserCriticalException("Невозможно установить станцию контроля, текущая станция последняя на маршруте");
                    }
                }
            }
        }

        throw new UserCriticalException("Невозможно установить станцию контроля");
    }

    /**
     * @return следующую станцию после станции контроля
     */
    @Nullable
    public Station getNextStation() {
        Logger.trace(TAG, "getNextStation");
        Station nextStation = null;
        TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
        if (trainInfo != null) {
            List<StationInfo> stationInfoList = trainInfo.getStations(stationInfoRepository);
            if (stationInfoList != null && !stationInfoList.isEmpty()) {
                ControlStation controlStation = tripServiceInfoStorage.getControlStation();
                if (controlStation != null) {
                    for (int i = 0; i < stationInfoList.size(); i++) {
                        StationInfo stationInfo = stationInfoList.get(i);
                        // Находим текущую станцию в нити поезд
                        if (ObjectUtils.equals(controlStation.getCode(), stationInfo.getCode())) {
                            // Если следующая станция доступна, то возвращаем её
                            if (i + 1 < stationInfoList.size()) {
                                StationInfo nextStationInfo = stationInfoList.get(i + 1);
                                nextStation = stationRepository.load(nextStationInfo.getCode(), nsiVersionProvider.getCurrentNsiVersion());
                            }
                            break;
                        }
                    }
                }
            }
        }
        return nextStation;
    }

    /**
     * @return признак того, что следующая станция - последняя в нити поезда (конечная)
     */
    public boolean isNextStationLast() {
        Station station = getNextStation();
        if (station != null) {
            // На всякий случай оставим проверки, хотя фактически если
            // getNextStation вернула не null значит всё хорошо
            TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
            if (trainInfo != null) {
                List<StationInfo> stationInfoList = trainInfo.getStations(stationInfoRepository);
                if (stationInfoList != null && !stationInfoList.isEmpty()) {
                    boolean isNextStationLast = ObjectUtils.equals(station.getCode(), stationInfoList.get(stationInfoList.size() - 1).getCode());
                    Logger.trace(TAG, "isNextStationLast: " + isNextStationLast);
                    return isNextStationLast;
                }
            }
        }
        Logger.trace(TAG, "isNextStationLast: " + false);
        return false;
    }

    /**
     * @return признак того, что станция находится ДО текущей станции контроля
     */
    public boolean isStationBeforeCurrent(@Nullable Long stationCode) {
        if (stationCode != null) {
            Station station = stationRepository.load(stationCode, nsiVersionProvider.getCurrentNsiVersion());
            if (station != null) {
                TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
                if (trainInfo != null) {
                    List<StationInfo> stationInfoList = trainInfo.getStations(stationInfoRepository);
                    if (stationInfoList != null && !stationInfoList.isEmpty()) {
                        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
                        if (controlStation != null) {
                            // Проверяем, что станция контроля есть в списке станций
                            if (!isStationExists(stationInfoList, controlStation.getCode())){
                                Logger.trace(TAG, String.format("controlStation [%d] is not present in the train stations", controlStation.getCode()));
                                return false;
                            }

                            // Обходим все станции, пока не встретим проверяемую станцию или станцию контроля
                            for (StationInfo stationInfo : stationInfoList) {
                                if (ObjectUtils.equals(controlStation.getCode(), stationInfo.getCode())) {
                                    Logger.trace(TAG, "isStationBeforeCurrent (" + stationCode + "): " + false);
                                    return false;
                                }
                                if (ObjectUtils.equals(stationCode, stationInfo.getCode())) {
                                    Logger.trace(TAG, "isStationBeforeCurrent (" + stationCode + "): " + true);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        Logger.trace(TAG, "isStationBeforeCurrent (" + stationCode + "): " + false);
        return false;
    }

    /**
     * @return признак того, что станция находится ПОСЛЕ текущей станции контроля
     */
    public boolean isStationAfterCurrent(@Nullable Long stationCode) {
        if (stationCode != null) {
            Station station = stationRepository.load(stationCode, nsiVersionProvider.getCurrentNsiVersion());
            if (station != null) {
                TrainInfo trainInfo = tripServiceInfoStorage.getTrainInfo();
                if (trainInfo != null) {
                    List<StationInfo> stationInfoList = trainInfo.getStations(stationInfoRepository);
                    if (stationInfoList != null && !stationInfoList.isEmpty()) {
                        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
                        if (controlStation != null) {
                            List<StationInfo> reverseStationList = new ArrayList<>(stationInfoList);
                            Collections.reverse(reverseStationList);
                            // Проверяем, что станция контроля есть в списке станций
                            if (!isStationExists(reverseStationList, controlStation.getCode())){
                                Logger.trace(TAG, String.format("controlStation [%d] is not present in the train stations", controlStation.getCode()));
                                return false;
                            }

                            // Обходим все станции в обратном порядке и проверяем, что искомая станция встретится раньше станции контроля
                            for (StationInfo stationInfo : reverseStationList) {
                                if (ObjectUtils.equals(controlStation.getCode(), stationInfo.getCode())) {
                                    Logger.trace(TAG, "isStationAfterCurrent (" + stationCode + "): " + false);
                                    return false;
                                }
                                if (ObjectUtils.equals(stationCode, stationInfo.getCode())) {
                                    Logger.trace(TAG, "isStationAfterCurrent (" + stationCode + "): " + true);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        Logger.trace(TAG, "isStationBeforeCurrent (" + stationCode + "): " + false);
        return false;
    }

    private boolean isStationExists(List<StationInfo> stationInfoList, Long stationCode){
        for (StationInfo stationInfo : stationInfoList) {
            if (stationInfo.getCode().equals(stationCode)){
                return true;
            }
        }
        return false;
    }

}
