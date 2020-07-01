package ru.ppr.cppk.reports;

import java.math.BigDecimal;
import java.util.ArrayList;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.data.summary.SalesForEttStatisticsBuilder;
import ru.ppr.cppk.data.summary.ShiftInfoStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.LegalEntity;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model.TicketKind;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog.PrintSalesForEttLogOperation;
import ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog.PrinterTplSalesForEttLog;
import ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog.PrinterTplSalesForEttLogInfo;
import ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog.PrinterTplSalesForEttPdInfo;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Carrier;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.TariffRepository;
import rx.Observable;

/**
 * Журнал оформления по ЭТТ
 *
 * @author Brazhkin A.V.
 */
public class ReportSalesForEttLog extends Report<ReportSalesForEttLog, PrintSalesForEttLogOperation.Result> {

    private static String TICKET_TYPE_FULL = "РАЗОВЫЙ ПОЛНЫЙ";
    private static String TICKET_TYPE_WITH_EXEMPTION = "РАЗОВЫЙ ЛЬГОТНЫЙ";
    private static String TICKET_TYPE_NO_MONEY = "РАЗОВЫЙ БЕЗДЕНЕЖНЫЙ";
    private static String TICKET_TYPE_CHILD = "РАЗОВЫЙ ДЕТСКИЙ";

    private PrinterTplSalesForEttLog.Params mainParams;

    private String shiftId;
    private boolean buildForLastShift;

    public ReportSalesForEttLog() {
        super();
    }

    public ReportSalesForEttLog setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public ReportSalesForEttLog setBuildForLastShift() {
        this.buildForLastShift = true;
        return this;
    }

    @Override
    public ReportSalesForEttLog build() throws Exception {
        mainParams = new PrinterTplSalesForEttLog.Params();
        mainParams.clicheParams = buildClicheParams();

        mainParams.salesForEttLogInfo = new PrinterTplSalesForEttLogInfo.Params();


        ShiftInfoStatisticsBuilder.Statistics shiftInfoStatistics = new ShiftInfoStatisticsBuilder()
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .build();

        SalesForEttStatisticsBuilder.Statistics salesForEttStatistics = new SalesForEttStatisticsBuilder()
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .build();


        mainParams.salesForEttLogInfo.shiftNum = shiftInfoStatistics.shiftNum;

        mainParams.ettPdCount = salesForEttStatistics.ettPdCount;
        mainParams.lossSum = salesForEttStatistics.lossSum;

        mainParams.events = new ArrayList<>();

        for (CPPKTicketSales cppkTicketSales : salesForEttStatistics.cppkTicketSalesList) {
            mainParams.events.add(toSellPDTplParams(cppkTicketSales));
        }

        return this;
    }

    PrinterTplSalesForEttPdInfo.Params toSellPDTplParams(CPPKTicketSales cppkTicketSales) {
        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();
        TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();
        StationRepository stationRepository = Dagger.appComponent().stationRepository();

        PrinterTplSalesForEttPdInfo.Params params = new PrinterTplSalesForEttPdInfo.Params();

        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
        ExemptionForEvent exemptionForEvent = localDaoSession.exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
        SmartCard exemptionSmartCard = exemptionForEvent.getSmartCardFromWhichWasReadAboutExemption();
        AdditionalInfoForEtt additionalInfoForEtt = localDaoSession.getAdditionalInfoForEttDao().load(ticketSaleReturnEventBase.getAdditionalInfoForEttId());
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

        params.exemptionBSKOuterNumber = exemptionSmartCard.getOuterNumber();
        params.exemptionBSCType = exemptionSmartCard.getType().getAbbreviation();
        params.exemptionNum = exemptionForEvent.getExpressCode();

        params.issueUnitCode = additionalInfoForEtt.getIssueUnitCode();
        params.ownerOrganizationCode = additionalInfoForEtt.getOwnerOrganizationCode();
        params.passengerCategory = additionalInfoForEtt.getPassengerCategory();
        params.snilsNumber = additionalInfoForEtt.getSnils();
        params.passengerFio = additionalInfoForEtt.getPassengerFio();
        params.guardianFio = additionalInfoForEtt.getGuardianFio();

        BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
        params.isNonCash = bankTransactionEvent != null;
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
            params.trackNum = smartCard.getTrack() + 1;
        }
        params.isTicketWritten = ticketSaleReturnEventBase.isTicketWritten();

        return params;
    }

    @Override
    protected Observable<PrintSalesForEttLogOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory()
                .getPrintSalesForEttLogOperation(mainParams).call();
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable(PrintSalesForEttLogOperation.Result printResult) {
        return addPrintReportEventObservable(ReportType.SalesForEttLog, printResult.getOperationTime());
    }


    private int getTrainCategory(TicketSaleReturnEventBase ticketSaleReturnEventBase) {

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();

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

        LocalDaoSession localDaoSession = Dagger.appComponent().localDaoSession();
        NsiDaoSession nsiDaoSession = Dagger.appComponent().nsiDaoSession();
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
        if (ticketCategory.getCode() == (int) TicketCategory.Code.SINGLE) {
            // Разовые ПД
            if (ticketType.getCode() == TicketType.Code.SINGLE_FULL) {
                // Полные
                if (ticketKind == TicketKind.Full) {
                    // Скидка 0% (Полный)
                    return TICKET_TYPE_FULL;
                } else if (ticketKind == TicketKind.WithExemption) {
                    Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
                    BigDecimal payedSum = fullPrice.getPayed();
                    // https://aj.srvdev.ru/browse/CPPKPP-31622
                    // Ошибка: в ведомостях, если безденежный ПД продан со сбором, то он учитывается в блоке «Льготные». Должен учитываться в блоке «Безденежные».
                    BigDecimal payedSumExcludeFee = payedSum.subtract(fee == null ? BigDecimal.ZERO : fee.getTotal());
                    if (Decimals.isZero(payedSumExcludeFee)) {
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
