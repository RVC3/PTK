package ru.ppr.nsi;

import ru.ppr.database.Database;
import ru.ppr.database.DbOpenHelper;
import ru.ppr.database.cache.DefaultQueryCache;
import ru.ppr.nsi.dao.AccessRuleDao;
import ru.ppr.nsi.dao.AccessSchemeDao;
import ru.ppr.nsi.dao.CalendarDao;
import ru.ppr.nsi.dao.CarrierDao;
import ru.ppr.nsi.dao.DirectionDao;
import ru.ppr.nsi.dao.ExemptionBannedForTariffPlanDao;
import ru.ppr.nsi.dao.ExemptionBannedForTicketStorageTypeDao;
import ru.ppr.nsi.dao.ExemptionDao;
import ru.ppr.nsi.dao.ExemptionGroupDao;
import ru.ppr.nsi.dao.ExemptionOrganizationDao;
import ru.ppr.nsi.dao.ExemptionToDao;
import ru.ppr.nsi.dao.FineDao;
import ru.ppr.nsi.dao.ProcessingFeeDao;
import ru.ppr.nsi.dao.ProductionSectionDao;
import ru.ppr.nsi.dao.ProductionSectionForUkkDao;
import ru.ppr.nsi.dao.ProhibitedForManualEntryExemptionDao;
import ru.ppr.nsi.dao.ProhibitedTicketTypeForExemptionCategoryDao;
import ru.ppr.nsi.dao.RegionCalendarDao;
import ru.ppr.nsi.dao.RegionDao;
import ru.ppr.nsi.dao.RouteToTariffPlanDao;
import ru.ppr.nsi.dao.ServiceFeeDao;
import ru.ppr.nsi.dao.SmartCardCancellationReasonDao;
import ru.ppr.nsi.dao.SmartCardStopListReasonDao;
import ru.ppr.nsi.dao.StationDao;
import ru.ppr.nsi.dao.StationToTariffZoneDao;
import ru.ppr.nsi.dao.StationTransferRouteDao;
import ru.ppr.nsi.dao.StationsOnRouteDao;
import ru.ppr.nsi.dao.TariffDao;
import ru.ppr.nsi.dao.TariffPlanDao;
import ru.ppr.nsi.dao.TariffZoneDao;
import ru.ppr.nsi.dao.TicketCategoryDao;
import ru.ppr.nsi.dao.TicketStorageTypeDao;
import ru.ppr.nsi.dao.TicketStorageTypeToTicketTypeDao;
import ru.ppr.nsi.dao.TicketTypeDao;
import ru.ppr.nsi.dao.TicketTypeToDeviceTypeDao;
import ru.ppr.nsi.dao.TicketTypeValidityTimeDao;
import ru.ppr.nsi.dao.TrainCategoryDao;
import ru.ppr.nsi.dao.TrainTicketTypeForTransferRegistrationDao;
import ru.ppr.nsi.dao.VersionDao;

/**
 * Высокоуровневая обертка над NSI БД.
 * Точка входа в слой для работы с NSI БД.
 * Объединяет в себе все мелкие DAO-объекты.
 * Никак не управляет подключением!
 * В случае закрытия соединения с БД через {@link DbOpenHelper}
 * и повторного получения БД через {@link DbOpenHelper#getReadableDatabase()}
 * нужно создавать новый объект {@link NsiDaoSession} на основе {@link Database}
 *
 * @author Aleksandr Brazhkin
 */
public class NsiDaoSession {

    private final Database database;
    /////////////////////////////////////
    private final VersionDao versionDao;
    private final StationDao stationDao;
    private final ProductionSectionDao productionSectionDao;
    private final AccessRuleDao accessRuleDao;
    private final TariffDao tariffDao;
    private final SmartCardStopListReasonDao smartCardStopListReasonDao;
    private final CalendarDao calendarDao;
    private final RegionCalendarDao regionCalendarDao;
    private final ServiceFeeDao serviceFeeDao;
    private final TrainCategoryDao trainCategoryDao;
    private final RegionDao regionDao;
    private final ExemptionDao exemptionDao;
    private final SmartCardCancellationReasonDao smartCardCancellationReasonDao;
    private final TariffPlanDao tariffPlanDao;
    private final TicketTypeDao ticketTypeDao;
    private final ExemptionGroupDao exemptionGroupDao;
    private final ExemptionToDao exemptionToDao;
    private final ExemptionBannedForTariffPlanDao exemptionBannedForTariffPlanDao;
    private final TicketCategoryDao ticketCategoryDao;
    private final TicketTypeValidityTimeDao ticketTypeValidityTimeDao;
    private final TicketStorageTypeDao ticketStorageTypeDao;
    private final TicketStorageTypeToTicketTypeDao ticketStorageTypeToTicketTypeDao;
    private final ExemptionBannedForTicketStorageTypeDao exemptionBannedForTicketStorageTypeDao;
    private final ProcessingFeeDao processingFeeDao;
    private final CarrierDao carrierDao;
    private final ExemptionOrganizationDao exemptionOrganizationDao;
    private final ProductionSectionForUkkDao productionSectionForUkkDao;
    private final ProhibitedForManualEntryExemptionDao prohibitedForManualEntryExemptionDao;
    private final AccessSchemeDao accessSchemeDao;
    private final RouteToTariffPlanDao routeToTariffPlanDao;
    private final ProhibitedTicketTypeForExemptionCategoryDao prohibitedTicketTypeForExemptionCategoryDao;
    private final FineDao fineDao;
    private final TicketTypeToDeviceTypeDao ticketTypeToDeviceTypeDao;
    private final StationsOnRouteDao stationsOnRouteDao;
    private final StationTransferRouteDao stationTransferRouteDao;
    private final DirectionDao directionDao;
    private final TariffZoneDao tariffZoneDao;
    private final StationToTariffZoneDao stationToTariffZoneDao;
    private final TrainTicketTypeForTransferRegistrationDao trainTicketTypeForTransferRegistrationDao;

    public NsiDaoSession(Database database) {
        this.database = database;
        this.versionDao = new VersionDao(this, new DefaultQueryCache());
        this.stationDao = new StationDao(this, new DefaultQueryCache());
        this.productionSectionDao = new ProductionSectionDao(this, new DefaultQueryCache());
        this.accessRuleDao = new AccessRuleDao(this, new DefaultQueryCache());
        this.tariffDao = new TariffDao(this, new DefaultQueryCache());
        this.smartCardStopListReasonDao = new SmartCardStopListReasonDao(this, new DefaultQueryCache());
        this.calendarDao = new CalendarDao(this, new DefaultQueryCache());
        this.regionCalendarDao = new RegionCalendarDao(this, new DefaultQueryCache());
        this.serviceFeeDao = new ServiceFeeDao(this, new DefaultQueryCache());
        this.trainCategoryDao = new TrainCategoryDao(this, new DefaultQueryCache());
        this.regionDao = new RegionDao(this, new DefaultQueryCache());
        this.exemptionDao = new ExemptionDao(this, new DefaultQueryCache());
        this.smartCardCancellationReasonDao = new SmartCardCancellationReasonDao(this, new DefaultQueryCache());
        this.tariffPlanDao = new TariffPlanDao(this, new DefaultQueryCache());
        this.ticketTypeDao = new TicketTypeDao(this, new DefaultQueryCache());
        this.exemptionGroupDao = new ExemptionGroupDao(this, new DefaultQueryCache());
        this.exemptionToDao = new ExemptionToDao(this, new DefaultQueryCache());
        this.exemptionBannedForTariffPlanDao = new ExemptionBannedForTariffPlanDao(this, new DefaultQueryCache());
        this.ticketCategoryDao = new TicketCategoryDao(this, new DefaultQueryCache());
        this.ticketTypeValidityTimeDao = new TicketTypeValidityTimeDao(this, new DefaultQueryCache());
        this.ticketStorageTypeDao = new TicketStorageTypeDao(this, new DefaultQueryCache());
        this.ticketStorageTypeToTicketTypeDao = new TicketStorageTypeToTicketTypeDao(this, new DefaultQueryCache());
        this.exemptionBannedForTicketStorageTypeDao = new ExemptionBannedForTicketStorageTypeDao(this, new DefaultQueryCache());
        this.processingFeeDao = new ProcessingFeeDao(this, new DefaultQueryCache());
        this.carrierDao = new CarrierDao(this, new DefaultQueryCache());
        this.exemptionOrganizationDao = new ExemptionOrganizationDao(this, new DefaultQueryCache());
        this.productionSectionForUkkDao = new ProductionSectionForUkkDao(this, new DefaultQueryCache());
        this.prohibitedForManualEntryExemptionDao = new ProhibitedForManualEntryExemptionDao(this, new DefaultQueryCache());
        this.accessSchemeDao = new AccessSchemeDao(this, new DefaultQueryCache());
        this.routeToTariffPlanDao = new RouteToTariffPlanDao(this, new DefaultQueryCache());
        this.prohibitedTicketTypeForExemptionCategoryDao = new ProhibitedTicketTypeForExemptionCategoryDao(this, new DefaultQueryCache());
        this.fineDao = new FineDao(this, new DefaultQueryCache());
        this.ticketTypeToDeviceTypeDao = new TicketTypeToDeviceTypeDao(this, new DefaultQueryCache());
        this.stationsOnRouteDao = new StationsOnRouteDao(this, new DefaultQueryCache());
        this.stationTransferRouteDao = new StationTransferRouteDao(this, new DefaultQueryCache());
        this.directionDao = new DirectionDao(this, new DefaultQueryCache());
        this.tariffZoneDao = new TariffZoneDao(this, new DefaultQueryCache());
        this.stationToTariffZoneDao = new StationToTariffZoneDao(this, new DefaultQueryCache());
        this.trainTicketTypeForTransferRegistrationDao = new TrainTicketTypeForTransferRegistrationDao(this, new DefaultQueryCache());
    }

    public Database getNsiDb() {
        return database;
    }

    public VersionDao getVersionDao() {
        return versionDao;
    }

    public StationDao getStationDao() {
        return stationDao;
    }

    public ProductionSectionDao getProductionSectionDao() {
        return productionSectionDao;
    }

    public AccessRuleDao getAccessRuleDao() {
        return accessRuleDao;
    }

    public TariffDao getTariffDao() {
        return tariffDao;
    }

    public SmartCardStopListReasonDao getSmartCardStopListReasonDao() {
        return smartCardStopListReasonDao;
    }

    public CalendarDao getCalendarDao() {
        return calendarDao;
    }

    public RegionCalendarDao getRegionCalendarDao() {
        return regionCalendarDao;
    }

    public ServiceFeeDao getServiceFeeDao() {
        return serviceFeeDao;
    }

    public TrainCategoryDao getTrainCategoryDao() {
        return trainCategoryDao;
    }

    public RegionDao getRegionDao() {
        return regionDao;
    }

    public ExemptionDao getExemptionDao() {
        return exemptionDao;
    }

    public SmartCardCancellationReasonDao getSmartCardCancellationReasonDao() {
        return smartCardCancellationReasonDao;
    }

    public TariffPlanDao getTariffPlanDao() {
        return tariffPlanDao;
    }

    public TicketTypeDao getTicketTypeDao() {
        return ticketTypeDao;
    }

    public ExemptionGroupDao getExemptionGroupDao() {
        return exemptionGroupDao;
    }

    public ExemptionToDao getExemptionToDao() {
        return exemptionToDao;
    }

    public ExemptionBannedForTariffPlanDao getExemptionBannedForTariffPlanDao() {
        return exemptionBannedForTariffPlanDao;
    }

    public TicketCategoryDao getTicketCategoryDao() {
        return ticketCategoryDao;
    }

    public TicketTypeValidityTimeDao getTicketTypeValidityTimeDao() {
        return ticketTypeValidityTimeDao;
    }

    public TicketStorageTypeDao getTicketStorageTypeDao() {
        return ticketStorageTypeDao;
    }

    public TicketStorageTypeToTicketTypeDao getTicketStorageTypeToTicketTypeDao() {
        return ticketStorageTypeToTicketTypeDao;
    }

    public ExemptionBannedForTicketStorageTypeDao getExemptionBannedForTicketStorageTypeDao() {
        return exemptionBannedForTicketStorageTypeDao;
    }

    public ProcessingFeeDao getProcessingFeeDao() {
        return processingFeeDao;
    }

    public CarrierDao getCarrierDao() {
        return carrierDao;
    }

    public ExemptionOrganizationDao getExemptionOrganizationDao() {
        return exemptionOrganizationDao;
    }

    public ProductionSectionForUkkDao getProductionSectionForUkkDao() {
        return productionSectionForUkkDao;
    }

    public ProhibitedForManualEntryExemptionDao getProhibitedForManualEntryExemptionDao() {
        return prohibitedForManualEntryExemptionDao;
    }

    public AccessSchemeDao getAccessSchemeDao() {
        return accessSchemeDao;
    }

    public RouteToTariffPlanDao getRouteToTariffPlanDao() {
        return routeToTariffPlanDao;
    }

    public ProhibitedTicketTypeForExemptionCategoryDao getProhibitedTicketTypeForExemptionCategoryDao() {
        return prohibitedTicketTypeForExemptionCategoryDao;
    }

    public FineDao getFineDao() {
        return fineDao;
    }

    public TicketTypeToDeviceTypeDao getTicketTypeToDeviceTypeDao() {
        return ticketTypeToDeviceTypeDao;
    }

    public StationsOnRouteDao getStationsOnRouteDao() {
        return stationsOnRouteDao;
    }

    public DirectionDao getDirectionDao() {
        return directionDao;
    }

    public StationTransferRouteDao getStationTransferRouteDao() {
        return stationTransferRouteDao;
    }

    public TariffZoneDao getTariffZoneDao() {
        return tariffZoneDao;
    }

    public StationToTariffZoneDao getStationToTariffZoneDao() {
        return stationToTariffZoneDao;
    }

    public TrainTicketTypeForTransferRegistrationDao getTrainTicketTypeForTransferRegistrationDao() {
        return trainTicketTypeForTransferRegistrationDao;
    }

}
