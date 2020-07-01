package ru.ppr.cppk.reports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;

import ru.ppr.cppk.data.summary.AuditTrailStatisticsBuilder;
import ru.ppr.cppk.data.summary.ShiftInfoStatisticsBuilder;
import ru.ppr.cppk.data.summary.UpdateStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKServiceSale;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.LegalEntity;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model.TicketKind;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.pd.checker.NoMoneyTicketChecker;
import ru.ppr.cppk.model.PrintReportEvent;
import ru.ppr.cppk.printer.rx.operation.auditTrail.AuditTrailInfoTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.AuditTrailTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.CancelPdTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.ExtraChargeTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.FineSaleTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.PdSaleTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.PrintAuditTrailOperation;
import ru.ppr.cppk.printer.rx.operation.auditTrail.PrintReportTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.ServiceSaleTpl;
import ru.ppr.cppk.printer.rx.operation.auditTrail.TransferSaleTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Carrier;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.FineRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.TariffRepository;
import rx.Observable;

/**
 * Контрольный журнал
 *
 * @author Aleksandr Brazhkin
 */
public class ReportAuditTrail extends Report<ReportAuditTrail, PrintAuditTrailOperation.Result> {

    private static String TICKET_TYPE_FULL = "РАЗОВЫЙ ПОЛНЫЙ";
    private static String TICKET_TYPE_WITH_EXEMPTION = "РАЗОВЫЙ ЛЬГОТНЫЙ";
    private static String TICKET_TYPE_NO_MONEY = "РАЗОВЫЙ БЕЗДЕНЕЖНЫЙ";
    private static String TICKET_TYPE_CHILD = "РАЗОВЫЙ ДЕТСКИЙ";

    private AuditTrailTpl.Params mainParams;

    private String shiftId;
    private Date fromDate;
    private Date toDate;
    private boolean buildForLastShift;
    private boolean buildForPeriod;

    public ReportAuditTrail() {
        super();
    }

    /**
     * Устанавливает id смены для построения отчета
     *
     * @param shiftId Идентификатор смены
     * @return Отчет
     */
    public ReportAuditTrail setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    /**
     * Устанавливает период для построения отчета
     *
     * @param fromDate Дата начала
     * @param toDate   Дата окончания
     * @return Отчет
     */
    public ReportAuditTrail setPeriod(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        return this;
    }

    /**
     * Устанавливает флаг, что следует строить отчет для последней смены
     *
     * @return Отчет
     */
    public ReportAuditTrail setBuildForPeriod() {
        this.buildForPeriod = true;
        return this;
    }

    /**
     * Выполняет построение отчета
     *
     * @return Отчет
     * @throws Exception в случае ошибки построения отчета
     */
    @Override
    public ReportAuditTrail build() throws Exception {
        mainParams = new AuditTrailTpl.Params();
        mainParams.clicheParams = buildClicheParams();

        mainParams.auditTrailInfo = new AuditTrailInfoTpl.Params();


        ShiftInfoStatisticsBuilder.Statistics shiftInfoStatistics = new ShiftInfoStatisticsBuilder()
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .build();

        UpdateStatisticsBuilder.Statistics updateStatistics = new UpdateStatisticsBuilder()
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .build();

        AuditTrailStatisticsBuilder.Statistics auditTrailStatistics = new AuditTrailStatisticsBuilder()
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .setPeriod(fromDate, toDate)
                .setBuildForPeriod(buildForPeriod)
                .build();


        mainParams.auditTrailInfo.shiftNum = shiftInfoStatistics.shiftNum;
        mainParams.auditTrailInfo.cashiers = new ArrayList<>();
        for (Cashier cashier : shiftInfoStatistics.cashiers) {
            mainParams.auditTrailInfo.cashiers.add(cashier.getFio());
        }

        mainParams.auditTrailInfo.SWVersion = updateStatistics.updateEvents.get(0).getVersion();
        mainParams.events = new ArrayList<>();

        for (AuditTrailEvent auditTrailEvent : auditTrailStatistics.auditTrailEvents) {
            switch (auditTrailEvent.getType()) {
                case PRINT_TEST_PD: {
                    mainParams.events.add(toTestPDTplParams(auditTrailEvent));
                    break;
                }
                case SALE: {
                    mainParams.events.add(toSellPDTplParams(auditTrailEvent));
                    break;
                }
                case SALE_WITH_ADD_PAYMENT: {
                    mainParams.events.add(toExtraChargeTplParams(auditTrailEvent));
                    break;
                }
                case RETURN: {
                    mainParams.events.add(toRepealPDTplParams(auditTrailEvent));
                    break;
                }
                case PRINT_REPORT: {
                    mainParams.events.add(toPrintReportTplParams(auditTrailEvent));
                    break;
                }
                case SERVICE_SALE: {
                    mainParams.events.add(toServiceSaleTplParams(auditTrailEvent));
                    break;
                }
                case FINE_SALE: {
                    mainParams.events.add(toFineSaleTplParams(auditTrailEvent));
                    break;
                }
                case TRANSFER_SALE: {
                    mainParams.events.add(toTransferSaleTplParams(auditTrailEvent));
                    break;
                }
            }
        }

        return this;
    }

    PdSaleTpl.Params toTestPDTplParams(AuditTrailEvent auditTrailEvent) {

        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();

        PdSaleTpl.Params params = new PdSaleTpl.Params();

        TestTicketEvent testTicketEvent = localDaoSession.getTestTicketDao().load(auditTrailEvent.getExtEventId());

        params.isTestPD = true;
        Check check = localDaoSession.getCheckDao().load(testTicketEvent.getCheckId());
        params.sellDateTime = check.getPrintDatetime();
        params.PDNumber = check.getOrderNumber();

        return params;

    }

    PdSaleTpl.Params toSellPDTplParams(AuditTrailEvent auditTrailEvent) {

        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();
        TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();
        StationRepository stationRepository = Dagger.appComponent().stationRepository();

        PdSaleTpl.Params params = new PdSaleTpl.Params();

        CPPKTicketSales cppkTicketSales = localDaoSession.getCppkTicketSaleDao().load(auditTrailEvent.getExtEventId());
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        ExemptionForEvent exemptionForEvent = localDaoSession.exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
        CPPKTicketReturn cppkTicketReturn = getCPPKTicketReturn()
                .findLastPdRepealEventForPdSaleEvent(cppkTicketSales.getId(), EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed));
        Fee fee = localDaoSession.getFeeDao().load(ticketSaleReturnEventBase.getFeeId());
        /////////////////////////////////////////////////////////////////////////////////////
        BigDecimal feeTotal = fee == null ? BigDecimal.ZERO : fee.getTotal();
        BigDecimal lossSum = exemptionForEvent == null ? BigDecimal.ZERO : exemptionForEvent.getLossSumm();
        Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
        BigDecimal tariff = fullPrice.getPayed().subtract(feeTotal);
        /////////////////////////////////////////////////////////////////////////////////////
        Tariff tariffIgnoringDeleteFlag = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                ticketEventBase.getTariffCode(),
                Di.INSTANCE.nsiVersionManager().getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
        );
        Station departureStation = stationRepository.load(ticketEventBase.getDepartureStationCode(), tariffIgnoringDeleteFlag.getVersionId());
        Station destinationStation = stationRepository.load(ticketEventBase.getDestinationStationCode(), tariffIgnoringDeleteFlag.getVersionId());
        params.departureStationName = departureStation.getShortName();
        params.destinationStationName = destinationStation.getShortName();
        if (exemptionForEvent != null) {
            SmartCard exemptionSmartCard = exemptionForEvent.getSmartCardFromWhichWasReadAboutExemption();
            if (exemptionSmartCard != null) {
                TicketStorageType smartCardType = exemptionSmartCard.getType();
                params.exemptionBSKOuterNumber = exemptionSmartCard.getOuterNumber();
                params.exemptionBSCType = smartCardType.getAbbreviation();
                SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());
                if (smartCard == null) {
                    // Льгота считана с карты, но билет не записан на карту, значит было переполнение, и билет печатали на бумаге
                    params.overflowBCSInfo = new PdSaleTpl.BCSOverflowInfo();
                    params.overflowBCSInfo.BCSType = exemptionSmartCard.getType().getAbbreviation();
                    if (exemptionSmartCard.getPresentTicket1() != null) {
                        params.overflowBCSInfo.PDNumber1 = exemptionSmartCard.getPresentTicket1().getTicketNumber();
                        params.overflowBCSInfo.sellDateTime1 = exemptionSmartCard.getPresentTicket1().getSaleDateTime();
                        params.overflowBCSInfo.PTKNumber1 = exemptionSmartCard.getPresentTicket1().getCashRegisterNumber();
                    }
                    if (exemptionSmartCard.getPresentTicket2() != null) {
                        params.overflowBCSInfo.PDNumber2 = exemptionSmartCard.getPresentTicket2().getTicketNumber();
                        params.overflowBCSInfo.sellDateTime2 = exemptionSmartCard.getPresentTicket2().getSaleDateTime();
                        params.overflowBCSInfo.PTKNumber2 = exemptionSmartCard.getPresentTicket2().getCashRegisterNumber();
                    }
                }
            }
            params.exemptionNum = exemptionForEvent.getExpressCode();
            params.preferentialDocumentFIO = exemptionForEvent.getFio();
            params.preferentialDocumentNum = exemptionForEvent.getNumberOfDocumentWhichApproveExemption();
        }
        params.isCanceled = cppkTicketReturn != null;
        BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
        if (bankTransactionEvent != null) {
            params.isNonCash = true;
            params.bankCardPan = bankTransactionEvent.getCardPan();
        }
        params.isThereBack = TicketWayType.TwoWay.equals(ticketEventBase.getWayType());
        params.fullCostSum = tariff;
        params.feeSum = feeTotal;
        params.incomeLoss = lossSum;
        Check check = localDaoSession.getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
        params.PDNumber = check.getOrderNumber();
        params.PDType = getPDType(ticketSaleReturnEventBase);
        params.sellDateTime = ticketEventBase.getSaledateTime();
        params.trainCategory = getTrainCategory(ticketSaleReturnEventBase);
        SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());
        if (smartCard != null) {
            TicketStorageType smartCardType = smartCard.getType();
            params.BSCType = smartCardType.getAbbreviation();
            params.BSKOuterNumber = smartCard.getOuterNumber();
            params.trackNum = smartCard.getTrack() + 1;
        }
        params.writeToSmartCardError = cppkTicketSales.getErrors() == null ? null : cppkTicketSales.getErrors().getCode();
        params.isTicketWritten = ticketSaleReturnEventBase.isTicketWritten();

        return params;
    }

    CancelPdTpl.Params toRepealPDTplParams(AuditTrailEvent auditTrailEvent) {

        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();
        TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();
        StationRepository stationRepository = Dagger.appComponent().stationRepository();

        CancelPdTpl.Params params = new CancelPdTpl.Params();

        CPPKTicketReturn cppkTicketReturn = localDaoSession.getCppkTicketReturnDao().load(auditTrailEvent.getExtEventId());

        CPPKTicketSales pdSaleEvent = localDaoSession.getCppkTicketSaleDao().load(cppkTicketReturn.getPdSaleEventId());
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        ExemptionForEvent exemptionForEvent = localDaoSession.exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
        Fee fee = localDaoSession.getFeeDao().load(ticketSaleReturnEventBase.getFeeId());
        /////////////////////////////////////////////////////////////////////////////////////
        BigDecimal feeTotal = fee == null ? BigDecimal.ZERO : fee.getTotal();
        BigDecimal lossSum = exemptionForEvent == null ? BigDecimal.ZERO : exemptionForEvent.getLossSumm();
        Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
        BigDecimal tariff = fullPrice.getPayed().subtract(feeTotal);
        /////////////////////////////////////////////////////////////////////////////////////
        Tariff tariffIgnoringDeleteFlag = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                ticketEventBase.getTariffCode(),
                Di.INSTANCE.nsiVersionManager().getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
        );
        Station departureStation = stationRepository.load(ticketEventBase.getDepartureStationCode(), tariffIgnoringDeleteFlag.getVersionId());
        Station destinationStation = stationRepository.load(ticketEventBase.getDestinationStationCode(), tariffIgnoringDeleteFlag.getVersionId());
        params.departureStationName = departureStation.getShortName();
        params.destinationStationName = destinationStation.getShortName();
        if (exemptionForEvent != null) {
            SmartCard smartCard = exemptionForEvent.getSmartCardFromWhichWasReadAboutExemption();
            if (smartCard != null) {
                TicketStorageType smartCardType = smartCard.getType();
                params.exemptionBSCOuterNumber = smartCard.getOuterNumber();
                params.exemptionBSCType = smartCardType.getAbbreviation();
            }
            params.exemptionNum = exemptionForEvent.getExpressCode();
            params.incomeLoss = exemptionForEvent.getLossSumm();
            params.preferentialDocumentFIO = exemptionForEvent.getFio();
            params.preferentialDocumentNum = exemptionForEvent.getNumberOfDocumentWhichApproveExemption();
        }
        params.isThereBack = TicketWayType.TwoWay.equals(ticketEventBase.getWayType());
        params.fullCostSum = tariff;
        params.feeSum = feeTotal;
        params.incomeLoss = lossSum;
        Check returnCheck = localDaoSession.getCheckDao().load(cppkTicketReturn.getCheckId());
        params.PDNumber = returnCheck.getOrderNumber();
        Check check = localDaoSession.getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
        params.canceledPDNumber = check.getOrderNumber();
        params.PDType = getPDType(ticketSaleReturnEventBase);
        params.cancelDateTime = cppkTicketReturn.getRecallDateTime();
        SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());
        if (smartCard != null) {
            TicketStorageType smartCardType = smartCard.getType();
            params.BSCType = smartCardType.getAbbreviation();
            params.BSKOuterNumber = smartCard.getOuterNumber();
            params.trackNum = smartCard.getTrack() + 1;
        }
        params.isTicketWritten = ticketSaleReturnEventBase.isTicketWritten();
        return params;
    }

    ExtraChargeTpl.Params toExtraChargeTplParams(AuditTrailEvent auditTrailEvent) {

        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();
        TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();
        StationRepository stationRepository = Dagger.appComponent().stationRepository();

        ExtraChargeTpl.Params params = new ExtraChargeTpl.Params();

        CPPKTicketSales cppkTicketSales = localDaoSession.getCppkTicketSaleDao().load(auditTrailEvent.getExtEventId());
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        ExemptionForEvent exemptionForEvent = localDaoSession.exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
        ParentTicketInfo parentTicketInfo = localDaoSession.getParentTicketInfoDao().load(ticketSaleReturnEventBase.getParentTicketInfoId());
        Fee fee = localDaoSession.getFeeDao().load(ticketSaleReturnEventBase.getFeeId());
        /////////////////////////////////////////////////////////////////////////////////////
        BigDecimal feeTotal = fee == null ? BigDecimal.ZERO : fee.getTotal();
        BigDecimal lossSum = exemptionForEvent == null ? BigDecimal.ZERO : exemptionForEvent.getLossSumm();
        Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
        BigDecimal tariff = fullPrice.getPayed().subtract(feeTotal);
        /////////////////////////////////////////////////////////////////////////////////////
        Check check = localDaoSession.getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
        params.PDNumber = check.getOrderNumber();
        params.sellDateTime = ticketEventBase.getSaledateTime();
        params.basePDNumber = parentTicketInfo.getTicketNumber();
        params.basePDPTKNumber = parentTicketInfo.getCashRegisterNumber();
        params.basePDsellDateTime = parentTicketInfo.getSaleDateTime();
        params.fullCostSum = tariff;
        params.feeSum = feeTotal;
        params.incomeLoss = lossSum;
        Tariff tariffIgnoringDeleteFlag = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                ticketEventBase.getTariffCode(),
                Di.INSTANCE.nsiVersionManager().getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
        );
        Station departureStation = stationRepository.load(ticketEventBase.getDepartureStationCode(), tariffIgnoringDeleteFlag.getVersionId());
        Station destinationStation = stationRepository.load(ticketEventBase.getDestinationStationCode(), tariffIgnoringDeleteFlag.getVersionId());
        params.departureStationName = TicketWayType.OneWay.equals(ticketEventBase.getWayType()) ?
                departureStation.getShortName() :
                destinationStation.getShortName();
        params.destinationStationName = TicketWayType.OneWay.equals(ticketEventBase.getWayType()) ?
                destinationStation.getShortName() :
                departureStation.getShortName();
        params.trainCategory = getTrainCategory(ticketSaleReturnEventBase);
        return params;

    }

    PrintReportTpl.Params toPrintReportTplParams(AuditTrailEvent auditTrailEvent) {

        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();

        PrintReportTpl.Params params = new PrintReportTpl.Params();

        PrintReportEvent printReportEvent = localDaoSession.getPrintReportEventDao().load(auditTrailEvent.getExtEventId());

        params.printDateTime = printReportEvent.getOperationTime();
        params.reportName = printReportEvent.getReportType().getName();
        params.cashInFR = printReportEvent.getCashInFR();

        return params;

    }

    ServiceSaleTpl.Params toServiceSaleTplParams(AuditTrailEvent auditTrailEvent) {

        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();

        ServiceSaleTpl.Params params = new ServiceSaleTpl.Params();

        CPPKServiceSale cppkServiceSale = localDaoSession.getCppkServiceSaleDao().load(auditTrailEvent.getExtEventId());

        params.serviceName = cppkServiceSale.getServiceFeeName();
        Price price = localDaoSession.getPriceDao().load(cppkServiceSale.getPriceId());
        params.cost = price.getFull();
        Check check = localDaoSession.getCheckDao().load(cppkServiceSale.getCheckId());
        params.pdNumber = check.getOrderNumber();
        params.saleDateTime = check.getPrintDatetime();

        return params;
    }

    private FineSaleTpl.Params toFineSaleTplParams(AuditTrailEvent auditTrailEvent) {
        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();
        FineRepository fineRepository = Dagger.appComponent().fineRepository();

        FineSaleTpl.Params params = new FineSaleTpl.Params();

        FineSaleEvent fineSaleEvent = localDaoSession.getFineSaleEventDao().load(auditTrailEvent.getExtEventId());
        Event event = localDaoSession.getEventDao().load(fineSaleEvent.getId());
        int versionId = event.getVersionId();

        params.fineName = fineRepository.load(fineSaleEvent.getFineCode(), versionId).getName();
        params.cost = fineSaleEvent.getAmount();

        Check check = localDaoSession.getCheckDao().load(fineSaleEvent.getCheckId());
        params.pdNumber = check.getOrderNumber();
        params.saleDateTime = check.getPrintDatetime();

        BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(fineSaleEvent.getBankTransactionEventId());

        if (bankTransactionEvent != null) {
            params.isNonCash = true;
            params.bankCardPan = bankTransactionEvent.getCardPan();
        }

        return params;
    }

    private TransferSaleTpl.Params toTransferSaleTplParams(AuditTrailEvent auditTrailEvent) {
        LocalDaoSession localDaoSession = Dagger.appComponent().localDaoSession();
        NsiDaoSession nsiDaoSession = Dagger.appComponent().nsiDaoSession();
        TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();
        StationRepository stationRepository = Dagger.appComponent().stationRepository();

        CPPKTicketSales cppkTicketSales = localDaoSession.getCppkTicketSaleDao().load(auditTrailEvent.getExtEventId());
        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        ParentTicketInfo parentTicketInfo = localDaoSession.getParentTicketInfoDao().load(ticketSaleReturnEventBase.getParentTicketInfoId());
        CPPKTicketReturn cppkTicketReturn = getCPPKTicketReturn()
                .findLastPdRepealEventForPdSaleEvent(cppkTicketSales.getId(), EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed));

        Tariff tariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                ticketEventBase.getTariffCode(),
                Di.INSTANCE.nsiVersionManager().getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
        );
        TicketType ticketType = tariff.getTicketType(nsiDaoSession);

        TransferSaleTpl.Params params = new TransferSaleTpl.Params();
        Check check = localDaoSession.getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
        params.pdNumber = check.getOrderNumber();
        params.saleDateTime = ticketEventBase.getSaledateTime();
        params.name = ticketType.getShortName();
        params.canceled = cppkTicketReturn != null;
        Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
        params.cost = fullPrice.getPayed();
        Station departureStation = stationRepository.load(ticketEventBase.getDepartureStationCode(), tariff.getVersionId());
        Station destinationStation = stationRepository.load(ticketEventBase.getDestinationStationCode(), tariff.getVersionId());
        params.departureStationName = departureStation.getShortName();
        params.destinationStationName = destinationStation.getShortName();
        SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());
        if (smartCard != null) {
            params.trackNum = smartCard.getTrack() + 1;
            params.bscType = smartCard.getType().getAbbreviation();
            params.bscOuterNumber = smartCard.getOuterNumber();
        }
        BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
        if (bankTransactionEvent != null) {
            params.isNonCash = true;
            params.bankCardPan = bankTransactionEvent.getCardPan();
        }
        if (parentTicketInfo != null) {
            params.parentPdNumber = parentTicketInfo.getTicketNumber();
            params.parentPdDeviceId = parentTicketInfo.getCashRegisterNumber();
            params.parentPdSaleDateTime = parentTicketInfo.getSaleDateTime();
        }

        return params;
    }

    @Override
    protected Observable<PrintAuditTrailOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory().
                getPrintAuditTrailOperation(mainParams).call();
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable(PrintAuditTrailOperation.Result printResult) {
        return addPrintReportEventObservable(ReportType.AuditTrail, printResult.getOperationTime());
    }


    private int getTrainCategory(TicketSaleReturnEventBase ticketSaleReturnEventBase) {

        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();

        TrainInfo trainInfo = localDaoSession.trainInfoDao().load(ticketSaleReturnEventBase.getTrainInfoId());
        int trainCategoryCode = trainInfo.getTrainCategoryCode();
        switch (trainCategoryCode) {
            case TrainCategory.CATEGORY_CODE_O: {
                return 0;
            }
            case TrainCategory.CATEGORY_CODE_7: {
                LegalEntity legalEntity = localDaoSession.legalEntityDao().load(ticketSaleReturnEventBase.getLegalEntityId());
                if (Carrier.CPPK_CODE.equals(legalEntity.getCode())) {
                    return 1;
                } else {
                    return 2;
                }
            }
            case TrainCategory.CATEGORY_CODE_C: {
                LegalEntity legalEntity = localDaoSession.legalEntityDao().load(ticketSaleReturnEventBase.getLegalEntityId());
                if (Carrier.CPPK_CODE.equals(legalEntity.getCode())) {
                    return 1;
                } else {
                    return 2;
                }
            }
            case TrainCategory.CATEGORY_CODE_M: {
                return 3;
            }
            default: {
                throw new IllegalArgumentException("unknown trainCategoryCode");
            }
        }
    }

    private String getPDType(TicketSaleReturnEventBase ticketSaleReturnEventBase) {

        LocalDaoSession localDaoSession = Di.INSTANCE.getDbManager().getLocalDaoSession().get();
        NsiDaoSession nsiDaoSession = Di.INSTANCE.getDbManager().getNsiDaoSession().get();
        TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();

        TicketKind ticketKind = ticketSaleReturnEventBase.getKind();
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        Tariff tariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                ticketEventBase.getTariffCode(),
                Di.INSTANCE.nsiVersionManager().getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
        );
        TicketType ticketType = tariff.getTicketType(nsiDaoSession);
        TicketCategory ticketCategory = ticketType.getTicketCategory(nsiDaoSession);
        Fee fee = localDaoSession.getFeeDao().load(ticketSaleReturnEventBase.getFeeId());

        /**
         * Определяется текущий тип билета
         */
        if (Dagger.appComponent().ticketCategoryChecker().isTransferTicket(ticketCategory.getCode())) {
            return ticketType.getShortName();
        } else if (ticketCategory.getCode() == (int) TicketCategory.Code.SINGLE) {
            // Разовые ПД
            if (ticketType.getCode() == TicketType.Code.SINGLE_FULL) {
                // Полные
                if (ticketKind == TicketKind.Full) {
                    // Скидка 0% (Полный)
                    return TICKET_TYPE_FULL;
                } else if (ticketKind == TicketKind.WithExemption) {
                    Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
                    // https://aj.srvdev.ru/browse/CPPKPP-31622
                    // Ошибка: в ведомостях, если безденежный ПД продан со сбором, то он учитывается в блоке «Льготные». Должен учитываться в блоке «Безденежные».
                    if (new NoMoneyTicketChecker().check(fullPrice, fee, true)) {
                        // Скидка 100% (Безденежный)
                        return TICKET_TYPE_NO_MONEY;
                    } else {
                        // Скидка меньше 100% (Льготный)
                        return TICKET_TYPE_WITH_EXEMPTION;
                    }
                }
            } else if (ticketType.getCode() == TicketType.Code.SINGLE_CHILD) {
                // Детские
                return TICKET_TYPE_CHILD;
            }
        } else if (ticketCategory.getCode() == (int) TicketCategory.Code.BAGGAGE) {
            // Квитанции на багаж
            return ticketType.getShortName();
        }

        return null;
    }

}
