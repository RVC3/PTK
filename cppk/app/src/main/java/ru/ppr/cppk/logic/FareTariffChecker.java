package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.StationToTariffZoneRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;

/**
 * Проверяет наличие тарифа доплаты.
 *
 * @author Dmitry Nevolin
 */
public class FareTariffChecker {

    private final NsiDaoSession nsiDaoSession;
    private final CommonSettings commonSettings;
    private final NsiVersionManager nsiVersionManager;
    private final TariffRepository tariffRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final StationRepository stationRepository;
    private final TrainCategoryRepository trainCategoryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final StationToTariffZoneRepository stationToTariffZoneRepository;

    @Inject
    FareTariffChecker(NsiDaoSession nsiDaoSession,
                      CommonSettings commonSettings,
                      NsiVersionManager nsiVersionManager,
                      TariffRepository tariffRepository,
                      TariffPlanRepository tariffPlanRepository,
                      StationRepository stationRepository,
                      TrainCategoryRepository trainCategoryRepository,
                      TicketTypeRepository ticketTypeRepository,
                      StationToTariffZoneRepository stationToTariffZoneRepository) {
        this.nsiDaoSession = nsiDaoSession;
        this.commonSettings = commonSettings;
        this.nsiVersionManager = nsiVersionManager;
        this.tariffRepository = tariffRepository;
        this.tariffPlanRepository = tariffPlanRepository;
        this.stationRepository = stationRepository;
        this.trainCategoryRepository = trainCategoryRepository;
        this.ticketTypeRepository = ticketTypeRepository;

        this.stationToTariffZoneRepository = stationToTariffZoneRepository;
    }

    // В будущем метод нуждается в небольшом рефакторинге, т.к. был просто перенесен из pdHandler
    public boolean check(@Nullable Tariff parentTariff, @NonNull TicketWayType wayType) {
        if (parentTariff == null) {
            return false;
        }

        int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

        // Тип билета для доплаты
        // Устанавливем "Разовый полный" или "Разовый детский"
        // http://agile.srvdev.ru/browse/CPPKPP-33774
        long parentTicketTypeCode = parentTariff.getTicketType(nsiDaoSession).getCode();
        long ticketTypeCode;
        if (parentTicketTypeCode == TicketType.Code.SINGLE_CHILD) {
            ticketTypeCode = TicketType.Code.SINGLE_CHILD;
        } else {
            ticketTypeCode = TicketType.Code.SINGLE_FULL;
        }
        TicketType ticketType = ticketTypeRepository.load((int) ticketTypeCode, nsiVersion);

        // Категорию поезда хардкодим, на PrivateSettings.getTrainCategoryPrefix() завязываться нельзя,
        // потому что в режиме мобильной кассы всегда устновлена категория 6000, а доплату при этом оформлять можно
        TrainCategory trainCategory = trainCategoryRepository.load(TrainCategory.CATEGORY_CODE_7, nsiVersion);

        // Определяем допустимые тарифные планы
        List<TariffPlan> allowedTariffPlans = tariffPlanRepository.getTariffPlans(trainCategory.code, true, nsiVersionManager.getCurrentNsiVersionId());
        List<Long> allowedTariffPlanCodes = new ArrayList<>();
        for (TariffPlan tariffPlan : allowedTariffPlans) {
            allowedTariffPlanCodes.add((long) tariffPlan.getCode());
        }

        int parentTicketCategoryCode = parentTariff.getTicketType(nsiDaoSession).getTicketCategoryCode();
        TicketCategoryChecker ticketCategoryChecker = new TicketCategoryChecker();

        List<Long> departureStationCodes;
        List<Long> destinationStationCodes;

        // http://agile.srvdev.ru/browse/CPPKPP-41822
        // Алгоритм расчета теперь одинаковый для разовых ПД и сезонных абонементов.

        // Фильтруем станции по маршруту для родительского тарифа
        // http://agile.srvdev.ru/browse/CPPKPP-33184
        List<Long> stationCodesInParentRoute = stationRepository.getStationCodesBetweenStations(
                Collections.singletonList((long) parentTariff.getStationDepartureCode()),
                Collections.singletonList((long) parentTariff.getStationDestinationCode()),
                //http://agile.srvdev.ru/browse/CPPKPP-34537
                null,
                nsiVersion
        );
        // Фильтруем станции по маршруту
        // http://agile.srvdev.ru/browse/CPPKPP-30337
        // Ищем варианты среди станций от родительского маршрута
        // http://agile.srvdev.ru/browse/CPPKPP-33184
        List<Long> stationCodesInRoute = stationRepository.getStationCodesBetweenStations(
                stationCodesInParentRoute,
                stationCodesInParentRoute,
                allowedTariffPlanCodes,
                nsiVersion
        );

        // http://agile.srvdev.ru/browse/CPPKPP-39418
        // Дополняем список станциями Большой Москвы
        Set<Long> updatedStationCodes = new HashSet<>(stationCodesInRoute);

        Long departureTariffZoneCode = parentTariff.getDepartureTariffZoneCode();
        if (departureTariffZoneCode != null) {
            List<Long> stationCodesForTariffZone = stationToTariffZoneRepository.getStationCodesForTariffZone(departureTariffZoneCode, nsiVersion);
            updatedStationCodes.addAll(stationCodesForTariffZone);
        }
        Long destinationTariffZoneCode = parentTariff.getDestinationTariffZoneCode();
        if (destinationTariffZoneCode != null) {
            List<Long> stationCodesForTariffZone = stationToTariffZoneRepository.getStationCodesForTariffZone(destinationTariffZoneCode, nsiVersion);
            updatedStationCodes.addAll(stationCodesForTariffZone);
        }

        if (commonSettings.getAllowedStationsCodes() != null) {
            // Фильтруем станции, оставим только те, по которым можно продать билет
            // https://aj.srvdev.ru/browse/CPPKPP-27005
            HashSet<Long> stationCodesAllowedBySettings = new HashSet<>();
            for (long stationCode : commonSettings.getAllowedStationsCodes()) {
                stationCodesAllowedBySettings.add(stationCode);
            }
            // Оставляем только пересечение множеств
            updatedStationCodes.retainAll(stationCodesAllowedBySettings);
        }
        ArrayList<Long> updatedStationCodesList = new ArrayList<>(updatedStationCodes);
        departureStationCodes = updatedStationCodesList;
        destinationStationCodes = updatedStationCodesList;


        // Ищем первый попавшийся тариф "Туда" и парный ему тариф "Обратно"
        Pair<Tariff, Tariff> tariffsThereAndBack = tariffRepository.loadDirectTariffs(
                departureStationCodes,
                destinationStationCodes,
                Collections.singletonList((long) ticketType.getCode()),
                allowedTariffPlans,
                nsiVersion);

        if (tariffsThereAndBack == null) {
            // Нет тарифа доплаты
            return false;
        }

        if (TicketWayType.TwoWay.equals(wayType) && tariffsThereAndBack.first == null && tariffsThereAndBack.second == null) {
            // Раньше было так:
            // http://agile.srvdev.ru/browse/CPPKPP-30337
            // При контроле ПД "Туда-Обратно" не позволяем оформлять доплату, если нет тарифа доплаты и туда, и обратно.

            // Теперь
            // http://agile.srvdev.ru/browse/CPPKPP-42163
            // На доплате может быть только один из тарифов (либо "и туда, и обратно", либо только "Туда", либо только "Обратно")
            // Не даем оформить доплату только в случае, если нет ни того, ни другого тарифа
            return false;
        }

        return true;
    }

}
