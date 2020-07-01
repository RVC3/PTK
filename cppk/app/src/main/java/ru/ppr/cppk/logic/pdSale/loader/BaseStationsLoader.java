package ru.ppr.cppk.logic.pdSale.loader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ru.ppr.cppk.data.summary.RecentStationsStatistics;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictions;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.utils.CollectionUtils;

/**
 * Базовый класс для лоадеров станций при оформлении ПД.
 *
 * @author Aleksandr Brazhkin
 */
abstract class BaseStationsLoader extends BaseLoader {

    private static final int STATION_CODES_PART_SIZE = 500;

    private final RecentStationsStatistics recentStationsStatistics;
    private final StationRepository stationRepository;
    private List<Station> directStationsCache = null;
    private List<Station> transitStationsCache = null;
    private List<Station> allStationsCache = null;

    BaseStationsLoader(PdSaleRestrictions pdSaleRestrictions,
                       RecentStationsStatistics recentStationsStatistics,
                       StationRepository stationRepository) {
        super(pdSaleRestrictions);
        this.recentStationsStatistics = recentStationsStatistics;
        this.stationRepository = stationRepository;

        getPdSaleRestrictions()
                .restrictionsChanges()
                .subscribe(aBoolean -> {
                    directStationsCache = null;
                    transitStationsCache = null;
                    allStationsCache = null;
                });
    }

    RecentStationsStatistics getRecentStationsStatistics() {
        return recentStationsStatistics;
    }

    /**
     * Возвращает список станций, до/от которых есть прямые тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param likeQuery       Поисковый запрос
     * @return Список станций
     */
    public List<Station> loadDirectStations(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery) {
        boolean isCacheable = fromStationCode == null && toStationCode == null;
        if (isCacheable) {
            if (directStationsCache == null) {
                Set<Long> stationCodes = loadDirectStationsInternal(null, null, null);
                directStationsCache = loadStationsByIds(stationCodes);
            }
            // Фильтруем на стороне Java
            List<Station> filteredStations = filter(directStationsCache, likeQuery);
            sort(filteredStations, likeQuery);
            return filteredStations;
        } else {
            // Фильтруем на стороне SQLite
            Set<Long> stationCodes = loadDirectStationsInternal(fromStationCode, toStationCode, likeQuery);
            List<Station> loadedStations = loadStationsByIds(stationCodes);
            sort(loadedStations, likeQuery);
            return loadedStations;
        }
    }

    /**
     * Возвращает список станций, до/от которых есть транзитные тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param likeQuery       Поисковый запрос
     * @return Список станций
     */
    public List<Station> loadTransitStations(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery) {
        boolean isCacheable = fromStationCode == null && toStationCode == null;
        if (isCacheable) {
            if (transitStationsCache == null) {
                Set<Long> stationCodes = loadTransitStationsInternal(null, null, null);
                transitStationsCache = loadStationsByIds(stationCodes);
            }
            // Фильтруем на стороне Java
            List<Station> filteredStations = filter(transitStationsCache, likeQuery);
            sort(filteredStations, likeQuery);
            return filteredStations;
        } else {
            // Фильтруем на стороне SQLite
            Set<Long> stationCodes = loadTransitStationsInternal(fromStationCode, toStationCode, likeQuery);
            List<Station> loadedStations = loadStationsByIds(stationCodes);
            sort(loadedStations, likeQuery);
            return loadedStations;
        }
    }

    /**
     * Возвращает список станций, до/от которых есть прямые или транзитные тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param likeQuery       Поисковый запрос
     * @return Список станций
     */
    public List<Station> loadAllStations(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery) {
        boolean isCacheable = fromStationCode == null && toStationCode == null;
        if (isCacheable) {
            if (allStationsCache == null) {
                Set<Long> directStationCodes = loadDirectStationsInternal(null, null, null);
                Set<Long> transitStationCodes = loadTransitStationsInternal(null, null, null);
                Set<Long> stationCodes = CollectionUtils.fullOuterJoin(directStationCodes, transitStationCodes);
                allStationsCache = loadStationsByIds(stationCodes);
            }
            // Фильтруем на стороне Java
            List<Station> filteredStations = filter(allStationsCache, likeQuery);
            sort(filteredStations, likeQuery);
            return filteredStations;
        } else {
            // Фильтруем на стороне SQLite
            Set<Long> directStationCodes = loadDirectStationsInternal(fromStationCode, toStationCode, likeQuery);
            Set<Long> transitStationCodes = loadTransitStationsInternal(fromStationCode, toStationCode, likeQuery);
            Set<Long> stationCodes = CollectionUtils.fullOuterJoin(directStationCodes, transitStationCodes);
            List<Station> loadedStations = loadStationsByIds(stationCodes);
            sort(loadedStations, likeQuery);
            return loadedStations;
        }
    }

    protected abstract List<Integer> getRecentStationCodes();

    protected abstract Set<Long> loadDirectStationsInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery);

    protected abstract Set<Long> loadTransitStationsInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery);

    private List<Station> loadStationsByIds(Set<Long> stationCodes) {
        List<Station> stations = new ArrayList<>();

        if (stationCodes.size() > STATION_CODES_PART_SIZE) {
            List<Long> partialStationCodes = new ArrayList<>();

            for (Long code : stationCodes) {
                partialStationCodes.add(code);

                if (partialStationCodes.size() == STATION_CODES_PART_SIZE) {
                    stations.addAll(stationRepository.loadAll(partialStationCodes, getVersionId()));
                    partialStationCodes.clear();
                }
            }

            if (!partialStationCodes.isEmpty()) {
                stations.addAll(stationRepository.loadAll(partialStationCodes, getVersionId()));
            }
        } else {
            stations = stationRepository.loadAll(stationCodes, getVersionId());
        }

        return stations;
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

    /**
     * Выполняет сортировку станций.
     * Порядок сортировки:
     * - Недавние станции
     * - Позиция подстроки поиска
     * - Наименование станции
     *
     * @param stations  Список станций
     * @param likeQuery Поисковый запрос
     */
    private void sort(List<Station> stations, @Nullable String likeQuery) {
        String modifedLikeQuery = likeQuery == null || likeQuery.isEmpty() ? null : likeQuery.toLowerCase();
        List<Integer> recentStationCodes = getRecentStationCodes();
        Collections.sort(stations, (s1, s2) -> {
            String name1 = s1.getName().toLowerCase();
            String name2 = s2.getName().toLowerCase();
            int indexOfS1 = recentStationCodes.indexOf(s1.getCode());
            int indexOfS2 = recentStationCodes.indexOf(s2.getCode());
            if (indexOfS1 == indexOfS2) {
                int indexOfLike1 = 0;
                int indexOfLike2 = 0;
                if (modifedLikeQuery != null) {
                    indexOfLike1 = name1.indexOf(modifedLikeQuery);
                    indexOfLike2 = name2.indexOf(modifedLikeQuery);
                }
                if (indexOfLike1 == indexOfLike2) {
                    return name1.compareTo(name2);
                }
                return indexOfLike1 - indexOfLike2;
            }
            return indexOfS2 - indexOfS1;
        });
    }

    Set<Long> filterByName(@Nullable Set<Long> stationCodes, @Nullable String likeQuery) {
        if (likeQuery != null && !likeQuery.isEmpty()) {
            return stationRepository.filterStationCodesByName(stationCodes, likeQuery, getVersionId());
        } else {
            return stationCodes;
        }
    }

    /**
     * Класс, описывающий тип тарифа.
     * Тип тарифа - Локальное понятие, набор данных, которые должны
     * совпадать для всех тарифов транзитного маршрута.
     */
    static class TariffType {
        /**
         * Код типа ПД
         */
        private final long ticketTypeCode;
        /**
         * Код тарифного плана
         */
        private final long tariffPlanCode;

        TariffType(long ticketTypeCode, long tariffPlanCode) {
            this.ticketTypeCode = ticketTypeCode;
            this.tariffPlanCode = tariffPlanCode;
        }

        public long getTicketTypeCode() {
            return ticketTypeCode;
        }

        public long getTariffPlanCode() {
            return tariffPlanCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TariffType that = (TariffType) o;

            if (ticketTypeCode != that.ticketTypeCode) return false;
            return tariffPlanCode == that.tariffPlanCode;
        }

        @Override
        public int hashCode() {
            int result = (int) (ticketTypeCode ^ (ticketTypeCode >>> 32));
            result = 31 * result + (int) (tariffPlanCode ^ (tariffPlanCode >>> 32));
            return result;
        }
    }


}
