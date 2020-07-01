package ru.ppr.cppk.logic.transfer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.model.TransferStationRoute;
import ru.ppr.nsi.repository.StationRepository;

/**
 * Класс лоадер станций трансфера (автобуса).
 *
 * @author Dmitry Nevolin
 */
public class TransferStationsLoader {

    private final StationRepository stationRepository;

    @Inject
    TransferStationsLoader(@NonNull StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    /**
     * Ищет все автобусные станции от куда можно куда-то поехать
     *
     * @param versionId версия НСИ
     * @return все автобусные станции отправления
     */
    public List<Station> loadTransferDepartureStations(int versionId, @Nullable String likeQuery) {
        List<Long> departureStationCodes = stationRepository.loadTransferStations(versionId);
        List<Station> list = stationRepository.loadAll(departureStationCodes, versionId);
        return filter(list, likeQuery);
    }

    /**
     * Ищет все автобусные станции назначения для конкретной станции отправления
     *
     * @param departureStationCode код станции отправления
     * @param versionId            версия НСИ
     * @return все автобусные станции назначения для конкретной станции отправления
     */
    public List<Station> loadTransferDestinationStations(long departureStationCode, int versionId, @Nullable String likeQuery) {
        String routeCode = null;
        Boolean back = null;
        // Т.к. маршруты для автобусов двухсторонние, без разницы в каком нарпавлении подгружать список
        List<TransferStationRoute> transferRoutes = stationRepository.loadTransferRoutes(false, versionId);
        // Подгружаем станцию отправления
        Station departureStation = stationRepository.load(departureStationCode, versionId);

        for (TransferStationRoute route : transferRoutes) {
            if (route.getDepStationCode().equals(Long.valueOf(departureStation.getCode()))) {
                routeCode = route.getRouteCode();
                back = false;

                break;
            } else if (route.getDestStationCode().equals(Long.valueOf(departureStation.getCode()))) {
                routeCode = route.getRouteCode();
                back = true;

                break;
            }
        }

        if (routeCode == null) {
            return Collections.emptyList();
        }

        List<Long> destinationStationCodes = stationRepository.loadDestinationTransferStations(routeCode, departureStationCode, back, versionId);
        List<Station> list = stationRepository.loadAll(destinationStationCodes, versionId);

        return filter(list, likeQuery);
    }

    private List<Station> filter(@NonNull List<Station> stations, @Nullable String likeQuery) {
        if (likeQuery == null || likeQuery.isEmpty()) {
            return stations;
        }
        List<Station> filteredStations = new ArrayList<>();
        String modifiedQuery = likeQuery.toLowerCase();
        for (Station station : stations) {
            if (station.getName().toLowerCase().contains(modifiedQuery)) {
                filteredStations.add(station);
            }
        }
        return filteredStations;
    }


}
