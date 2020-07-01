package ru.ppr.cppk.logic.pdSale;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ppr.cppk.helpers.TicketTypeChecker;
import ru.ppr.cppk.helpers.TicketTypeValidityTimeChecker;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.TicketTypesToDeviceTypesChecker;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TicketTypesValidityTimes;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.StationToTariffZoneRepository;
import ru.ppr.nsi.repository.StationTransferRouteRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.nsi.repository.TicketTypesValidityTimesRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;
import ru.ppr.nsi.repository.TrainTicketTypeForTransferRegistrationRepository;
import ru.ppr.utils.CollectionUtils;
import rx.subjects.PublishSubject;

/**
 * Ограничения на оформление ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleRestrictions {

    private static final String TAG = Logger.makeLogTag(PdSaleRestrictions.class);

    private final NsiDaoSession nsiDaoSession;
    private final PdSaleEnvType pdSaleEnvType;
    private final TicketTypesValidityTimesRepository ticketTypesValidityTimesRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final StationRepository stationRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final StationToTariffZoneRepository stationToTariffZoneRepository;
    private final TariffRepository tariffRepository;
    private final TrainCategoryRepository trainCategoryRepository;
    private final StationTransferRouteRepository stationTransferRouteRepository;
    private final TrainTicketTypeForTransferRegistrationRepository trainTicketTypeForTransferRegistrationRepository;
    private final NsiVersionManager nsiVersionManager;
    private final PdValidityPeriodCalculator pdValidityPeriodCalculator;
    private final TicketTypeChecker ticketTypeChecker;
    /**
     * Ограничивающий список кодов производственных участков, {@code null} - нет ограничений (режим мобильной кассы или разрешено оформление ПД вне привязанного участка)
     */
    @Nullable
    private List<Long> allowedProductionSectionCodes;
    /**
     * Ограничивающий список кодов тарифных планов, {@code null} - нет ограничений
     */
    @Nullable
    private Set<Long> allowedTariffPlanCodes;
    /**
     * Ограничивающий список кодов типов ПД, игнорирующий время действия, {@code null} - нет ограничений
     */
    @Nullable
    private Set<Long> allowedTimeIndependentTicketTypeCodes;
    /**
     * Ограничивающий список кодов типов ПД, {@code null} - нет ограничений
     */
    @Nullable
    private Set<Long> allowedTicketTypeCodes;
    /**
     * Ограничивающий список кодов станций по общим настройкам ПТК (CommonSettings), {@code null} - нет ограничений
     * https://aj.srvdev.ru/browse/CPPKPP-27005
     */
    @Nullable
    private Set<Long> allowedStationCodesBySettings = null;
    /**
     * Ограничивающий список кодов станций для участка работы ПТК, {@code null} - нет ограничений (режим мобильной кассы или разрешено оформление ПД вне привязанного участка)
     */
    @Nullable
    private Set<Long> allowedStationCodesByProductionSection;
    /**
     * Список кодов станций, принадлежащих участку работы ПТК, {@code null} - все станции (режим мобильной кассы или разрешено оформление ПД вне привязанного участка)
     */
    @Nullable
    private Set<Long> stationCodesForProductionSection;
    /**
     * Список кодов транзитных станций, принадлежащих участку работы ПТК.
     */
    @NonNull
    private Set<Long> transitStationCodes = Collections.emptySet();
    /**
     * Ограничивающий список кодов станций, доступных для оформления ПД
     * {@link #allowedStationCodesBySettings} inner join {@link #allowedStationCodesByProductionSection}.
     */
    @Nullable
    private Set<Long> allowedStationCodesBySettingsAndProdSection;
    /**
     * Ограничивающий список кодов станций, доступных для оформления ПД и принадлежащих участку
     * {@link #allowedStationCodesBySettingsAndProdSection} inner join {@link #stationCodesForProductionSection}.
     */
    @Nullable
    private Set<Long> allowedStationCodesBySettingsAndProdSectionInProdSection;
    /**
     * Запрещающий список кодов станций, которые не должны участвовать в оформлении ПД
     */
    @Nullable
    private Set<Long> deniedStationCodes;
    /**
     * Входные данные для расчета ограничений на оформление ПД.
     */
    private PdSaleRestrictionsParams params = null;

    private final PublishSubject<Boolean> restrictionsChanges = PublishSubject.create();

    PdSaleRestrictions(@NonNull NsiDaoSession nsiDaoSession,
                       @NonNull TicketTypesValidityTimesRepository ticketTypesValidityTimesRepository,
                       @NonNull TicketTypeRepository ticketTypeRepository,
                       @NonNull StationRepository stationRepository,
                       @NonNull TariffPlanRepository tariffPlanRepository,
                       @NonNull StationToTariffZoneRepository stationToTariffZoneRepository,
                       @NonNull TariffRepository tariffRepository,
                       @NonNull TrainCategoryRepository trainCategoryRepository,
                       @NonNull StationTransferRouteRepository stationTransferRouteRepository,
                       @NonNull TrainTicketTypeForTransferRegistrationRepository trainTicketTypeForTransferRegistrationRepository,
                       @NonNull NsiVersionManager nsiVersionManager,
                       @NonNull PdValidityPeriodCalculator pdValidityPeriodCalculator,
                       @NonNull PdSaleEnvType pdSaleEnvType,
                       @NonNull TicketTypeChecker ticketTypeChecker) {
        this.nsiDaoSession = nsiDaoSession;
        this.ticketTypesValidityTimesRepository = ticketTypesValidityTimesRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.stationRepository = stationRepository;
        this.tariffPlanRepository = tariffPlanRepository;
        this.stationToTariffZoneRepository = stationToTariffZoneRepository;
        this.tariffRepository = tariffRepository;
        this.trainCategoryRepository = trainCategoryRepository;
        this.stationTransferRouteRepository = stationTransferRouteRepository;
        this.trainTicketTypeForTransferRegistrationRepository = trainTicketTypeForTransferRegistrationRepository;
        this.nsiVersionManager = nsiVersionManager;
        this.pdValidityPeriodCalculator = pdValidityPeriodCalculator;
        this.pdSaleEnvType = pdSaleEnvType;
        this.ticketTypeChecker = ticketTypeChecker;
    }

    public void update(@NonNull PdSaleRestrictionsParams pdSaleRestrictionsParams) {
        boolean restrictionsChanged = false;
        PdSaleRestrictionsParams oldParams = this.params;
        if (!pdSaleRestrictionsParams.equalsForGeneralRestrictions(oldParams)) {
            this.params = pdSaleRestrictionsParams;
            updateGeneralRestrictions();
            restrictionsChanged = true;
        }
        if (!pdSaleRestrictionsParams.equalsForTimeDependentRestrictions(oldParams)) {
            this.params = pdSaleRestrictionsParams;
            Set<Long> prevAllowedTicketTypeCodes = allowedTicketTypeCodes;
            updateTimeDependentRestrictions();
            if (!CollectionUtils.equals(prevAllowedTicketTypeCodes, allowedTicketTypeCodes)) {
                restrictionsChanged = true;
            }
        }
        Logger.trace(TAG, "update() -> restrictionsChanged: " + restrictionsChanged);
        if (restrictionsChanged) {
            restrictionsChanges.onNext(Boolean.TRUE);
        }
    }

    private void updateTimeDependentRestrictions() {
        if (pdSaleEnvType == PdSaleEnvType.TARIFFS_INFO) {
            // На экране с информацией о тарифах не ограничиваем TicketTypes по времени действия
            allowedTicketTypeCodes = allowedTimeIndependentTicketTypeCodes;
            return;
        }

        // Загружаем список всех доступных типов ПД без учета времени валидности
        List<TicketType> allowedTimeIndependentTicketTypes = ticketTypeRepository.loadAll(allowedTimeIndependentTicketTypeCodes, params.getNsiVersion());

        allowedTicketTypeCodes = new HashSet<>();

        TicketTypeValidityTimeChecker ticketTypeValidityTimeChecker = new TicketTypeValidityTimeChecker();
        TicketTypesToDeviceTypesChecker ticketTypesToDeviceTypesChecker = new TicketTypesToDeviceTypesChecker(nsiDaoSession, params.getNsiVersion());

        Date parentPdStartDate = null;
        Date parentPdEndDate = null;
        if (pdSaleEnvType == PdSaleEnvType.TRANSFER) {
            if (params.getTransferSaleData().isWithParentPd()) {
                // Ищем родительский тариф
                Tariff parentTariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                        params.getTransferSaleData().getParentPdTariffCode(),
                        nsiVersionManager.getNsiVersionIdForDate(params.getTransferSaleData().getParentPdSaleDateTime()));
                // Получаем тип родительского ПД
                TicketType parentTicketType = ticketTypeRepository.load(parentTariff.getTicketTypeCode(), parentTariff.getVersionId());
                // Рассчитываем срок действия родительского ПД
                TicketWayType parentWayType = params.getTransferSaleData().getParentPdDirection();
                parentPdStartDate = params.getTransferSaleData().getParentPdStartDateTime();
                int validityPeriod = pdValidityPeriodCalculator.calcValidityPeriod(parentPdStartDate, parentWayType, parentTicketType, params.getNsiVersion());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parentPdStartDate);
                calendar.add(Calendar.DAY_OF_MONTH, validityPeriod);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                parentPdEndDate = calendar.getTime();
            }
        }

        for (TicketType ticketType : allowedTimeIndependentTicketTypes) {
            // Проверяем на возможность продавать на ПТК
            if (!ticketTypesToDeviceTypesChecker.checkForPtk(ticketType)) {
                continue;
            }
            // Загружаем информацию о сроках действия типа ПД
            List<TicketTypesValidityTimes> ticketTypeValidityTimeList = ticketTypesValidityTimesRepository
                    .getTicketTypesValidityTimesList(ticketType.getCode(), params.getNsiVersion());

            // Проверяем возможность использования типа ПД
            if (!ticketTypeValidityTimeChecker.isTicketTypeAllowedForTime(ticketTypeValidityTimeList, params.getTimestamp())) {
                continue;
            }

            if (pdSaleEnvType == PdSaleEnvType.TRANSFER) {
                if (params.getTransferSaleData().isWithParentPd()) {
                    Preconditions.checkNotNull(parentPdEndDate);
                    Date pdStartDate;
                    if (parentPdStartDate.after(params.getTimestamp())) {
                        // Если дата начала действия родительского ПД после текущей даты оформления,
                        // считаем датой начала действия трансфера дату начала действия родительского ПД
                        pdStartDate = parentPdStartDate;
                    } else {
                        // ПД начинает действовать с момента продажи, term = 0
                        pdStartDate = params.getTimestamp();
                    }

                    // Определяем направление по типу ПД
                    TicketWayType wayType = ticketTypeChecker.isTwoWayTransfer(ticketType.getCode()) ? TicketWayType.TwoWay : TicketWayType.OneWay;
                    // Собственные умозаключения из старого кода:
                    // Срок действия трансфера не должен выходить за сроки действия родительского ПД
                    int validityPeriod = pdValidityPeriodCalculator.calcValidityPeriod(pdStartDate, wayType, ticketType, params.getNsiVersion());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(pdStartDate);
                    calendar.add(Calendar.DAY_OF_MONTH, validityPeriod);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    Date pdEndDate = calendar.getTime();
                    if (pdEndDate.after(parentPdEndDate)) {
                        continue;
                    }
                }
            }

            allowedTicketTypeCodes.add((long) ticketType.getCode());
        }
    }

    private void updateGeneralRestrictions() {
        // Для доплаты другой алгоритм
        if (pdSaleEnvType == PdSaleEnvType.EXTRA_PAYMENT) {
            updateGeneralRestrictionsForExtraPayment();
            return;
        }
        // Для трансферов другой алгоритм
        if (pdSaleEnvType == PdSaleEnvType.TRANSFER) {
            updateGeneralRestrictionsForTransfer();
            return;
        }
        // Формируем ограничивающий список кодов производственных участков
        // В режиме мобильной кассы не ограничиваем текущим участком
        // http://agile.srvdev.ru/browse/CPPKPP-32576
        if (!params.isMobileCashRegister() && !params.isOutsideProductionSectionSaleEnabled()) {
            allowedProductionSectionCodes = nsiDaoSession.getProductionSectionForUkkDao().getProductionSectionCodesForUkk(params.getProductionSectionCode(), true, params.getNsiVersion());
        } else {
            allowedProductionSectionCodes = null;
        }
        // Формируем ограничивающий список кодов тарифных планов
        // Можно выбирать тарифный план для любой категории поезда
        // http://agile.srvdev.ru/browse/CPPKPP-31605
        List<TariffPlan> allowedTariffPlans = tariffPlanRepository.getTariffPlans(null, false, params.getNsiVersion());
        allowedTariffPlanCodes = CollectionUtils.asSet(allowedTariffPlans, tariffPlan -> (long) tariffPlan.getCode());
        // Формируем ограничивающий список кодов станций по общим настройкам ПТК (CommonSettings)
        if (params.getAllowedStationsCodes() != null) {
            allowedStationCodesBySettings = new HashSet<>(params.getAllowedStationsCodes().length);
            for (long stationCode : params.getAllowedStationsCodes()) {
                allowedStationCodesBySettings.add(stationCode);
            }
        } else {
            allowedStationCodesBySettings = null;
        }
        // Формируем ограничивающий список кодов станций по ограничивающему списку кодов участков работы ПТК
        if (allowedProductionSectionCodes == null) {
            stationCodesForProductionSection = null;
            allowedStationCodesByProductionSection = null;
        } else {
            // Формируем ограничивающий список кодов станций, принадлежащих участку работы ПТК
            List<Station> stationsForProductionSections = stationRepository.loadStationsForProductionSections(allowedProductionSectionCodes, params.getNsiVersion());
            stationCodesForProductionSection = CollectionUtils.asSet(stationsForProductionSections, station -> (long) station.getCode());
            List<Long> stationsOnRoutes = stationRepository.loadStationsOnRoute(stationCodesForProductionSection, params.getNsiVersion());
            allowedStationCodesByProductionSection = new HashSet<>(stationsOnRoutes);
        }
        // Формируем список кодов станций, доступных для оформления ПД
        allowedStationCodesBySettingsAndProdSection = CollectionUtils.innerJoin(allowedStationCodesBySettings, allowedStationCodesByProductionSection);
        // Формируем список кодов станций, доступных для оформления ПД и находящихся на участке работы ПТК
        allowedStationCodesBySettingsAndProdSectionInProdSection = CollectionUtils.innerJoin(allowedStationCodesBySettingsAndProdSection, stationCodesForProductionSection);

        // Формируем список транзитных станций
        List<Long> transitStationCodesList = stationRepository.filterTransitStationCodes(allowedStationCodesBySettingsAndProdSectionInProdSection, params.getNsiVersion());
        transitStationCodes = new HashSet<>(transitStationCodesList);

        // Формируем ограничивающий список кодов типов ПД
        // Заполняем список типов ПД, доступных для текущей категории ПД
        if (pdSaleEnvType == PdSaleEnvType.TARIFFS_INFO) {
            allowedTimeIndependentTicketTypeCodes = null;
        } else {
            allowedTimeIndependentTicketTypeCodes = new HashSet<>();
            List<Long> ticketCategoryCodes;
            if (pdSaleEnvType == PdSaleEnvType.SINGLE_PD) {
                ticketCategoryCodes = Collections.singletonList(TicketCategory.Code.SINGLE);
            } else if (pdSaleEnvType == PdSaleEnvType.BAGGAGE) {
                ticketCategoryCodes = Collections.singletonList(TicketCategory.Code.BAGGAGE);
            } else {
                throw new IllegalArgumentException("Unknown pdSaleEnvType: " + pdSaleEnvType);
            }
            List<TicketType> ticketTypesForTicketCategories = ticketTypeRepository.getTicketTypesForTicketCategories(ticketCategoryCodes, params.getNsiVersion());
            allowedTimeIndependentTicketTypeCodes = CollectionUtils.asSet(ticketTypesForTicketCategories, ticketCategory -> (long) ticketCategory.getCode());
        }
    }

    private void updateGeneralRestrictionsForExtraPayment() {
        // Формируем ограничивающий список кодов станций по ограничивающему списку кодов участков работы ПТК
        // http://agile.srvdev.ru/browse/CPPKPP-30337
        // Рзянкина Наталья: Набор станций ограничивается станциями, считанными с абонемента. На участок завязываться не надо.
        allowedProductionSectionCodes = null;
        stationCodesForProductionSection = null;
        allowedStationCodesByProductionSection = null;
        // Формируем ограничивающий список кодов тарифных планов
        List<TariffPlan> allowedTariffPlans = tariffPlanRepository.getTariffPlans(TrainCategory.CATEGORY_CODE_7, true, params.getNsiVersion());
        allowedTariffPlanCodes = CollectionUtils.asSet(allowedTariffPlans, tariffPlan -> (long) tariffPlan.getCode());
        // Формируем ограничивающий список кодов станций по общим настройкам ПТК (CommonSettings)
        if (params.getAllowedStationsCodes() != null) {
            allowedStationCodesBySettings = new HashSet<>(params.getAllowedStationsCodes().length);
            for (long stationCode : params.getAllowedStationsCodes()) {
                allowedStationCodesBySettings.add(stationCode);
            }
        } else {
            allowedStationCodesBySettings = null;
        }
        // Формируем ограничивающий список кодов станций, принадлежащих зоне родительского тарифа
        Set<Long> allowedStationCodesByTariffZone = new HashSet<>();
        Long departureTariffZoneCode = params.getExtraPaymentData().getParentDepartureTariffZoneCode();
        if (departureTariffZoneCode != null) {
            List<Long> stationCodesForTariffZone = stationToTariffZoneRepository.getStationCodesForTariffZone(departureTariffZoneCode, params.getNsiVersion());
            allowedStationCodesByTariffZone.addAll(stationCodesForTariffZone);
        }
        Long destinationTariffZoneCode = params.getExtraPaymentData().getParentDestinationTariffZoneCode();
        if (destinationTariffZoneCode != null) {
            List<Long> stationCodesForTariffZone = stationToTariffZoneRepository.getStationCodesForTariffZone(destinationTariffZoneCode, params.getNsiVersion());
            allowedStationCodesByTariffZone.addAll(stationCodesForTariffZone);
        }

        // Формируем ограничивающий список кодов станций, принадлежащих маршруту между родительскими станциями
        // Фильтруем станции по маршруту для родительского тарифа
        // http://agile.srvdev.ru/browse/CPPKPP-33184
        List<Long> stationCodesInParentRoute = stationRepository.getStationCodesBetweenStations(
                Collections.singletonList(params.getExtraPaymentData().getParentDepartureStationCode()),
                Collections.singletonList(params.getExtraPaymentData().getParentDestinationStationCode()),
                //http://agile.srvdev.ru/browse/CPPKPP-34537
                null,
                params.getNsiVersion()
        );
        // Фильтруем станции по маршруту
        // http://agile.srvdev.ru/browse/CPPKPP-30337
        // Ищем варианты среди станций от родительского маршрута
        // http://agile.srvdev.ru/browse/CPPKPP-33184
        List<Long> stationCodesInRoute = stationRepository.getStationCodesBetweenStations(
                stationCodesInParentRoute,
                stationCodesInParentRoute,
                // Не может он быть null, но оставим проверку чтобы анализатор не ругался
                allowedTariffPlanCodes != null ? new ArrayList<>(allowedTariffPlanCodes) : null,
                params.getNsiVersion()
        );
        Set<Long> allowedStationCodesByRouteAndTariffZone = new HashSet<>(stationCodesInRoute);
        allowedStationCodesByRouteAndTariffZone.addAll(allowedStationCodesByTariffZone);

        // Формируем список кодов станций, доступных для оформления ПД
        // allowedStationCodesByProductionSection всегда null, нет смысла делать join с ним,
        // но уже на данном этапе должен быть добавлен список станций отфильтрованных по маршруту и зоне тарифа
        allowedStationCodesBySettingsAndProdSection = CollectionUtils.innerJoin(allowedStationCodesBySettings, allowedStationCodesByRouteAndTariffZone);

        // Формируем список кодов станций, доступных для оформления ПД и находящихся на участке работы ПТК
        // stationCodesForProductionSection всегда null, нет смысла делать join с ним,
        // но поле должно быть заполнено, с ним работают loader'ы.
        allowedStationCodesBySettingsAndProdSectionInProdSection = allowedStationCodesBySettingsAndProdSection;

        // Формируем список транзитных станций
        transitStationCodes = Collections.emptySet();

        // Формируем ограничивающий список кодов типов ПД
        // Заполням список типов ПД, доступных для текущей категории ПД
        allowedTimeIndependentTicketTypeCodes = new HashSet<>();
        List<Long> ticketCategoryCodes = Collections.singletonList(TicketCategory.Code.SINGLE);
        List<TicketType> ticketTypesForTicketCategories = ticketTypeRepository.getTicketTypesForTicketCategories(ticketCategoryCodes, params.getNsiVersion());
        allowedTimeIndependentTicketTypeCodes = CollectionUtils.asSet(ticketTypesForTicketCategories, ticketCategory -> (long) ticketCategory.getCode());
    }

    private void updateGeneralRestrictionsForTransfer() {
        // Формируем ограничивающий список кодов производственных участков
        allowedProductionSectionCodes = null;
        // Формируем ограничивающий список кодов станций по ограничивающему списку кодов участков работы ПТК
        stationCodesForProductionSection = null;
        allowedStationCodesByProductionSection = null;

        // Формируем ограничивающий список кодов тарифных планов
        long trainCategoryCode;
        if (params.getTransferSaleData().isWithParentPd()) {
            // Фильтруем тарифные планы по категории родительского ПД
            // Ищем родительский тариф
            Tariff parentTariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                    params.getTransferSaleData().getParentPdTariffCode(),
                    nsiVersionManager.getNsiVersionIdForDate(params.getTransferSaleData().getParentPdSaleDateTime()));
            // Получем родительский тарифный план
            TariffPlan parentTariffPlan = tariffPlanRepository.load(parentTariff.getTariffPlanCode(), parentTariff.getVersionId());
            // Получаем родительскую категорию поезда
            trainCategoryCode = trainCategoryRepository.load(parentTariffPlan.getTrainCategoryCode(), parentTariff.getVersionId()).code;
        } else {
            // На стартовом экране офомления трансфера мы ещё не знаем,
            // с какой категорией поезда будет прочитан билет на поезд на следующем шаге.
            trainCategoryCode = -1;
        }

        // Ищем допустимые тарифный планы
        List<TariffPlan> allowedTariffPlans = tariffPlanRepository.getTariffPlans(trainCategoryCode, false, params.getNsiVersion());
        allowedTariffPlanCodes = CollectionUtils.asSet(allowedTariffPlans, tariffPlan -> (long) tariffPlan.getCode());

        // Формируем ограничивающий список кодов станций по общим настройкам ПТК (CommonSettings)
        if (params.getAllowedStationsCodes() != null) {
            allowedStationCodesBySettings = new HashSet<>(params.getAllowedStationsCodes().length);
            for (long stationCode : params.getAllowedStationsCodes()) {
                allowedStationCodesBySettings.add(stationCode);
            }
        } else {
            allowedStationCodesBySettings = null;
        }

        Set<Long> transferStationCodesForTransferRouteCodesSet = null;
        if (params.getTransferSaleData().isWithParentPd()) {
            // Формируем ограничивающий список кодов станций, принадлежащих маршруту между родительскими станциями
            // Ищем родительский тариф
            Tariff parentTariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                    params.getTransferSaleData().getParentPdTariffCode(),
                    nsiVersionManager.getNsiVersionIdForDate(params.getTransferSaleData().getParentPdSaleDateTime()));
            // Фильтруем станции по маршруту для родительского тарифа
            List<Long> stationCodesInParentRoute = stationRepository.getStationCodesBetweenStations(
                    Collections.singletonList(Long.valueOf(parentTariff.getStationDepartureCode())),
                    Collections.singletonList(Long.valueOf(parentTariff.getStationDestinationCode())),
                    null,
                    params.getNsiVersion()
            );
            // Проверяем, не работает ли ПТК в режиме мобильной кассы на вход
            Long mobileCashRegisterStationCode = params.getTransferSaleData().getMobileCashRegisterStationCode();
            if (mobileCashRegisterStationCode != null) {
                // Если ПТК работает в режиме мобильной кассы на вход
                if (!stationCodesInParentRoute.contains(mobileCashRegisterStationCode)) {
                    // http://agile.srvdev.ru/browse/CPPKPP-42672
                    // Если станции работы ПТК нет в маршруте исходного ПД на поезд,
                    // запрещаем оформление трансфера
                    // Оставляем список станций для оформления трансфера пустым
                    transferStationCodesForTransferRouteCodesSet = Collections.emptySet();
                }
            }
            if (transferStationCodesForTransferRouteCodesSet == null) {
                // Определяем список маршрутов трансфера на маршруте ПД на поезд
                List<Long> stationTransferRouteCodes = stationTransferRouteRepository.loadStationsTransferRouteCodesForStationCodes(stationCodesInParentRoute, params.getNsiVersion());
                // Список возможных станций на всех автобусных маршрутах,
                // которые находятся на маршруте от станции отправления до станции назначения родительского маршрута
                List<Long> transferStationCodesForTransferRouteCodes = stationRepository.loadStationCodesForRouteCodes(stationTransferRouteCodes, params.getNsiVersion());
                transferStationCodesForTransferRouteCodesSet = new HashSet<>(transferStationCodesForTransferRouteCodes);
            }
        }

        // Формируем список кодов станций, доступных для оформления ПД
        // allowedStationCodesByProductionSection всегда null, нет смысла делать join с ним,
        // но уже на данном этапе должен быть добавлен список станций отфильтрованных по маршруту
        allowedStationCodesBySettingsAndProdSection = CollectionUtils.innerJoin(allowedStationCodesBySettings, transferStationCodesForTransferRouteCodesSet);

        // Формируем список кодов станций, доступных для оформления ПД и находящихся на участке работы ПТК
        // stationCodesForProductionSection всегда null, нет смысла делать join с ним,
        // но поле должно быть заполнено, с ним работают loader'ы.
        allowedStationCodesBySettingsAndProdSectionInProdSection = allowedStationCodesBySettingsAndProdSection;

        // Формируем список транзитных станций
        transitStationCodes = Collections.emptySet();

        allowedTimeIndependentTicketTypeCodes = new HashSet<>();

        if (params.getTransferSaleData().isWithParentPd()) {
            // Ищем родительский тариф
            Tariff parentTariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                    params.getTransferSaleData().getParentPdTariffCode(),
                    nsiVersionManager.getNsiVersionIdForDate(params.getTransferSaleData().getParentPdSaleDateTime()));
            // Формируем ограничивающий список кодов типов ПД
            allowedTimeIndependentTicketTypeCodes = new HashSet<>(trainTicketTypeForTransferRegistrationRepository
                    .getTransferTicketTypeCodes(
                            parentTariff.getTicketTypeCode(),
                            params.getTransferSaleData().getParentPdDirection().getCode(),
                            params.getNsiVersion()));
        } else {
            // Хардкод кодов согласован
            // http://agile.srvdev.ru/browse/CPPKPP-42672
            List<Long> ticketCategoryCodes = Arrays.asList(
                    TicketCategory.Code.SINGLE_TRANSFER,
                    TicketCategory.Code.TRANSFER_SEASON_TICKET_BY_PERIOD,
                    TicketCategory.Code.TRANSFER_SEASON_TICKET_ON_WEEKEND,
                    TicketCategory.Code.TRANSFER_SEASON_TICKET_ON_WORKDAYS,
                    TicketCategory.Code.TRANSFER_SPECIAL_OFFER
            );
            List<TicketType> ticketTypesForTicketCategories = ticketTypeRepository.getTicketTypesForTicketCategories(ticketCategoryCodes, params.getNsiVersion());

            // Фильтруем типы ПД
            for (TicketType ticketType : ticketTypesForTicketCategories) {
                if (ticketType.isJointSale()) {
                    // Не продаем по типам ПД, предназначенным для совместной продажи
                    continue;
                }
                allowedTimeIndependentTicketTypeCodes.add((long) ticketType.getCode());
            }
        }
    }

    /**
     * Возвращает ограничивающий список кодов производственных участков
     */
    @Nullable
    public List<Long> getAllowedProductionSectionCodes() {
        return allowedProductionSectionCodes;
    }

    /**
     * Возвращает ограничивающий список кодов тарифных планов
     */
    @Nullable
    public Set<Long> getAllowedTariffPlanCodes() {
        return allowedTariffPlanCodes;
    }

    /**
     * Возвращает ограничивающий список кодов типов ПД
     */
    @Nullable
    public Set<Long> getAllowedTicketTypeCodes() {
        return allowedTicketTypeCodes;
    }

    /**
     * Возвращает ограничивающий список кодов станций по общим настройкам ПТК (CommonSettings)
     */
    @Nullable
    public Set<Long> getAllowedStationCodesBySettings() {
        return allowedStationCodesBySettings;
    }

    /**
     * Возвращает ограничивающий список кодов станций для участка работы ПТК
     */
    @Nullable
    public Set<Long> getAllowedStationCodesByProductionSection() {
        return allowedStationCodesByProductionSection;
    }

    /**
     * Возвращает ограничивающий список кодов станций, доступных для оформления ПД
     */
    @Nullable
    public Set<Long> getAllowedStationCodesBySettingsAndProdSection() {
        return allowedStationCodesBySettingsAndProdSection;
    }


    /**
     * Возвращает ограничивающий список кодов станций, доступных для оформления ПД и находящихся на участке работы ПТК
     */
    @Nullable
    public Set<Long> getAllowedStationCodesBySettingsAndProdSectionInProdSection() {
        return allowedStationCodesBySettingsAndProdSectionInProdSection;
    }

    /**
     * Возвращает список кодов станций, принадлежащих участку работы ПТК
     */
    @Nullable
    public Set<Long> getStationCodesForProductionSection() {
        return stationCodesForProductionSection;
    }

    /**
     * Возвращает список кодов транзитных станций, принадлежащих участку работы ПТК
     */
    @NonNull
    public Set<Long> getTransitStationCodes() {
        return transitStationCodes;
    }

    /**
     * Возвращает список кодов станций, запрещённых к оформлению
     */
    @Nullable
    public Set<Long> getDeniedStationCodes() {
        return deniedStationCodes;
    }

    /**
     * Проверяет, разрешена ли продажа ПД от/до указанной станции для текущего участка работы ПТК.
     *
     * @param stationCode Код станции
     * @return {@code true} если продажа разрешена, {@code false} иначе
     */
    public boolean isStationAllowedForProductionSection(@Nullable Long stationCode) {
        return stationCode == null || allowedStationCodesByProductionSection == null || allowedStationCodesByProductionSection.contains(stationCode);
    }

    /**
     * Проверяет, принадлежит ли указанная станция текущему участку работы ПТК.
     *
     * @param stationCode Код станции
     * @return {@code true} если принадлежит, {@code false} иначе
     */
    public boolean belongsToCurrentProductionSection(@NonNull Long stationCode) {
        return stationCodesForProductionSection == null || stationCodesForProductionSection.contains(stationCode);
    }

    /**
     * Проверяет, возможно ли оформление ПД по прямому маршруту между станциями.
     * http://agile.srvdev.ru/browse/CPPKPP-34719
     * Только если хотя бы одна из станций принадлежит участку работы ПТК, имеем право продавать прямой ПД.
     * Иначе, обязаны продать транзит, проходящий через станцию на нашем участке
     *
     * @param depStationCode  Код станции отправления
     * @param destStationCode Код станции назначения
     * @return {@code true} если возможно оформление ПД по прямому маршруту, {@code false} иначе
     */
    public boolean isDirectTariffAllowed(@NonNull Long depStationCode, @NonNull Long destStationCode) {
        return belongsToCurrentProductionSection(depStationCode) || belongsToCurrentProductionSection(destStationCode);
    }

    public int getNsiVersion() {
        return params.getNsiVersion();
    }

    public PublishSubject<Boolean> restrictionsChanges() {
        return restrictionsChanges;
    }
}
