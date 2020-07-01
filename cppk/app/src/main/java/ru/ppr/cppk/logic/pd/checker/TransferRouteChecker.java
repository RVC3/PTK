package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.StationOnRoutes;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.model.TransferStationRoute;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.StationsOnRouteRepository;

/**
 * Чекер маршрута для ПД-трансфера.
 *
 * @author Aleksandr Brazhkin
 */
public class TransferRouteChecker {

    private static final String TAG = Logger.makeLogTag(TransferRouteChecker.class);

    private final NsiDaoSession nsiDaoSession;
    private final StationRepository stationRepository;
    private final PrivateSettings privateSettings;
    private final StationsOnRouteRepository stationsOnRouteRepository;
    private final NsiVersionManager nsiVersionManager;

    @Inject
    public TransferRouteChecker(@NonNull NsiDaoSession nsiDaoSession,
                                @NonNull StationRepository stationRepository,
                                @NonNull PrivateSettings privateSettings,
                                @NonNull StationsOnRouteRepository stationsOnRouteRepository,
                                @NonNull NsiVersionManager nsiVersionManager) {
        this.nsiDaoSession = nsiDaoSession;
        this.stationRepository = stationRepository;
        this.privateSettings = privateSettings;
        this.stationsOnRouteRepository = stationsOnRouteRepository;
        this.nsiVersionManager = nsiVersionManager;
    }

    /**
     * Вернет true, если станции трансфера заданы и маршрут между ними существует
     *
     * @return
     */
    public boolean checkTransferRouteStationValid() {
        TransferStationRoute transferStationRoute = stationRepository.loadTransferRouteForStations(
                privateSettings.getTransferRouteStationsCodes()[0],
                privateSettings.getTransferRouteStationsCodes()[1],
                nsiVersionManager.getCurrentNsiVersionId());
        return transferStationRoute != null;
    }

    /**
     * Проверяет маршрут для ПД-трансфера.
     *
     * @param pd         ПД
     * @param tariff     Тариф
     * @param nsiVersion Версия НСИ
     * @return {@code true} если проверка пройдена успешно, {@code false} иначе
     */
    public boolean checkRouteFoTransferPd(PD pd, Tariff tariff, int nsiVersion) {
        Station readDepartureStation = tariff.getStationDeparture(nsiDaoSession);
        Station readDestinationStation = tariff.getStationDestination(nsiDaoSession);

        String workingRouteCode = null;
        boolean isWorkingDirectionBack = false;
        Station workingDepartureStation = getWorkingDepartureStation(nsiVersion);
        Station workingDestinationStation = getWorkingDestinationStation(nsiVersion);
        // Т.к. маршруты для автобусов двухсторонние, без разницы в каком нарпавлении подгружать список
        List<TransferStationRoute> transferRoutes = stationRepository.loadTransferRoutes(false, nsiVersion);

        for (TransferStationRoute route : transferRoutes) {
            if (route.getDepStationCode().equals(Long.valueOf(workingDepartureStation.getCode()))) {
                workingRouteCode = route.getRouteCode();
                isWorkingDirectionBack = false;

                break;
            } else if (route.getDestStationCode().equals(Long.valueOf(workingDepartureStation.getCode()))) {
                workingRouteCode = route.getRouteCode();
                isWorkingDirectionBack = true;

                break;
            }
        }

        if (workingRouteCode == null) {
            throw new IllegalStateException("не найден маршрут для выбранной из настроек станции, не должно такого быть!");
        }

        boolean isReadDepartureStationOnRoute = false;
        boolean isReadDestinationStationOnRoute = false;
        StationOnRoutes readDepartureStationOnRoutes = null;
        StationOnRoutes readDestinationStationOnRoutes = null;
        List<StationOnRoutes> stationsOnRoutes = stationsOnRouteRepository.loadStationsOnRoutesByRouteCode(workingRouteCode, nsiVersion);

        for (StationOnRoutes stationOnRoutes : stationsOnRoutes) {
            if (readDepartureStation.getCode() == stationOnRoutes.getStationCode()) {
                isReadDepartureStationOnRoute = true;
                readDepartureStationOnRoutes = stationOnRoutes;
            }

            if (readDestinationStation.getCode() == stationOnRoutes.getStationCode()) {
                isReadDestinationStationOnRoute = true;
                readDestinationStationOnRoutes = stationOnRoutes;
            }
        }

        //http://agile.srvdev.ru/browse/CPPKPP-36927
        //оставляем функционал в рассчите что он скоро понадобится
        boolean isNeedControlDirection = false;

        if (isReadDepartureStationOnRoute && isReadDestinationStationOnRoute) {
            if (pd.wayType != TicketWayType.TwoWay && isNeedControlDirection) {
                boolean isReadDirectionBack = readDepartureStationOnRoutes.getStationNumber() > readDestinationStationOnRoutes.getStationNumber();

                if (isWorkingDirectionBack != isReadDirectionBack) {
                    Logger.trace(TAG, "Не совпадает напрвление движения движения автобуса и направление считанных станций");
                    Logger.trace(TAG, "isWorkingDirectionBack = " + isWorkingDirectionBack + ", isReadDirectionBack = " + isReadDirectionBack);
                    addLogStationsInvalidDetails(workingDepartureStation, workingDestinationStation, readDepartureStation, readDestinationStation);
                    return false;
                }
            }
        } else {
            Logger.trace(TAG, "Одна или обе из считанных станций не лежат на маршруте работы автобуса");
            Logger.trace(TAG, "isReadDepartureStationOnRoute = " + isReadDepartureStationOnRoute + ", isReadDestinationStationOnRoute = " + isReadDestinationStationOnRoute);
            addLogStationsInvalidDetails(workingDepartureStation, workingDestinationStation, readDepartureStation, readDestinationStation);
            return false;
        }

        return true;
    }

    @NonNull
    private Station getWorkingDepartureStation(int versionNsi) {
        return stationRepository.load(privateSettings.getTransferRouteStationsCodes()[0], versionNsi);
    }

    @NonNull
    private Station getWorkingDestinationStation(int versionNsi) {
        return stationRepository.load(privateSettings.getTransferRouteStationsCodes()[1], versionNsi);
    }

    private void addLogStationsInvalidDetails(@NonNull Station workingDepartureStation,
                                              @NonNull Station workingDestinationStation,
                                              @NonNull Station readDepartureStation,
                                              @NonNull Station readDestinationStation) {
        Logger.trace(TAG, "workingDepartureStation = " + workingDepartureStation.getCode() +
                ", workingDestinationStation = " + workingDestinationStation.getCode() +
                ", readDepartureStation = " + readDepartureStation.getCode() +
                ", readDestinationStation = " + readDestinationStation.getCode());
    }
}
