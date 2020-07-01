package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.util.Calendar;
import java.util.Date;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.data.summary.RecentStationsStatistics;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.LegalEntity;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.event.model.TicketKind;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.SeasonTicket;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.cppk.entity.utils.builders.events.CppkTicketSaleGenerator;
import ru.ppr.cppk.entity.utils.builders.events.TicketEventBaseGenerator;
import ru.ppr.cppk.entity.utils.builders.events.TicketSaleReturnEventBaseGenerator;
import ru.ppr.cppk.entity.utils.builders.events.TrainInfoGenerator;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.helpers.TicketTypeChecker;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.builder.AuditTrailEventBuilder;
import ru.ppr.cppk.logic.builder.CheckBuilder;
import ru.ppr.cppk.logic.fiscaldocument.base.DocumentStateSyncronizer;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.logic.utils.DateUtils;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.printer.rx.operation.saleCheck.PrintSaleCheckOperation;
import ru.ppr.cppk.printer.rx.operation.saleCheck.SaleCheckTpl;
import ru.ppr.cppk.ui.helper.TicketTypeStringifier;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Carrier;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.CarrierRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.utils.CommonUtils;
import rx.Observable;
import rx.Single;

/**
 * Документ "Чек продажи ПД"
 *
 * @author Aleksandr Brazhkin
 */
public class DocumentSalePd {

    private static final String TAG = Logger.makeLogTag(DocumentSalePd.class);

    private final LocalDaoSession localDaoSession;
    private final NsiDaoSession nsiDaoSession;
    private final TariffRepository tariffRepository;
    private final StationRepository stationRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final TicketTypeStringifier ticketTypeStringifier;
    private final RecentStationsStatistics recentStationsStatistics;
    private final PdValidityPeriodCalculator pdValidityPeriodCalculator;
    private final NsiVersionManager nsiVersionManager;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketCategoryChecker ticketCategoryChecker;
    private final TicketTypeChecker ticketTypeChecker;
    private final CarrierRepository carrierRepository;

    /**
     * Информация о последнем фискальном документе
     */
    private PrintSaleCheckOperation.Result printResult;
    private int pdNumber;
    private SignDataResult signDataResult;
    /**
     * Данные продаваемого ПД
     */
    private DataSalePD dataSalePD;
    /**
     * Идентификатор события продажи ПД
     */
    private long saleTicketId;

    public ShiftEventDao getCashRegisterWorkingShiftDao() {
        return localDaoSession.getShiftEventDao();
    }

    public DocumentSalePd() {
        localDaoSession = Dagger.appComponent().localDaoSession();
        nsiDaoSession = Dagger.appComponent().nsiDaoSession();
        tariffRepository = Dagger.appComponent().tariffRepository();
        stationRepository = Dagger.appComponent().stationRepository();
        tariffPlanRepository = Dagger.appComponent().tariffPlanRepository();
        ticketTypeStringifier = Dagger.appComponent().ticketTypeStringifier();
        recentStationsStatistics = Dagger.appComponent().recentStationsStatistics();
        pdValidityPeriodCalculator = Dagger.appComponent().pdValidityPeriodCalculator();
        nsiVersionManager = Dagger.appComponent().nsiVersionManager();
        ticketTypeRepository = Dagger.appComponent().ticketTypeRepository();
        ticketCategoryChecker = Dagger.appComponent().ticketCategoryChecker();
        ticketTypeChecker = Dagger.appComponent().ticketTypeChecker();
        carrierRepository = Dagger.appComponent().carrierRepository();
        getCashRegisterWorkingShiftDao();
    }

    public DocumentSalePd setDataSalePD(DataSalePD dataSalePD) {
        this.dataSalePD = dataSalePD;
        Log.d(TAG, "setDataSalePD() called with: dataSalePD = [" + dataSalePD + "]");
        Log.e(TAG, "setDataSalePD: sssstack ", new Exception());
        return this;
    }

    public DocumentSalePd setSignDataResult(SignDataResult signedData) {
        this.signDataResult = signedData;
        return this;
    }

    /**
     * Выполняет печать ШК на принтере
     */
    public Single<DocumentSalePd> printBarcode() {
        return Single
                .fromCallable(() -> {
                    byte[] barcodeData = Dagger.appComponent().barcodeBuilder().buildAsByteArray(signDataResult);
                    Logger.info(TAG, "Print barcode - " + CommonUtils.bytesToHexWithoutSpaces(barcodeData));
                    return barcodeData;
                })
                .flatMap(barcodeData -> Di.INSTANCE.printerManager().getOperationFactory().getPrintBarcodeOperation(barcodeData)
                        .call()
                        .toSingle())
                .onErrorResumeNext(throwable -> Single.error(new DocumentStateSyncronizer.PdInFrNotPrintedException(throwable)))
                .flatMap(aVoid -> Single.just(DocumentSalePd.this));
    }

    /**
     * Собирает параметры для шаблона печати чека продажи ПД
     *
     * @return Параметры
     */
    private PrintSaleCheckOperation.Params buildParams() {
        PrintSaleCheckOperation.Params params = new PrintSaleCheckOperation.Params();
        SaleCheckTpl.Params saleCheckTplParams = new SaleCheckTpl.Params();
        params.saleCheckTplPrams = saleCheckTplParams;

        String pdDescription = ticketTypeStringifier.stringify(dataSalePD.getExemption(), dataSalePD.getTicketType());

        saleCheckTplParams.ticketTypeName = pdDescription;
        params.ticketTypeName = pdDescription;

        saleCheckTplParams.direction = dataSalePD.getDirection();
        saleCheckTplParams.departureStationName = dataSalePD.getDepartureStation().getShortName();
        saleCheckTplParams.destinationStationName = dataSalePD.getDestinationStation().getShortName();
        params.ticketCostCostValueWithoutDiscount = dataSalePD.getTicketCostValueWithoutDiscount();
        params.ticketCostCostValueWithDiscount = dataSalePD.getTicketCostValueWithDiscount();
        params.ticketCostVatRate = dataSalePD.getTicketCostVatRate();
        params.ticketCostVatValue = dataSalePD.getTicketCostVatValue();
        params.payment = dataSalePD.getPaymentSum();
        saleCheckTplParams.tariffPlanName = dataSalePD.getTariffPlan().getShortName();
        saleCheckTplParams.pdNumber = dataSalePD.getPDNumber();
        params.pdNumber = dataSalePD.getPDNumber();
        saleCheckTplParams.smartCard = dataSalePD.getSmartCard();
        params.paymentType = dataSalePD.getPaymentType();
        params.customerPhoneNumber = dataSalePD.getETicketDataParams() == null ? null : dataSalePD.getETicketDataParams().getPhone();
        params.customerEmail = dataSalePD.getETicketDataParams() == null ? null : dataSalePD.getETicketDataParams().getEmail();
        saleCheckTplParams.startDate = dataSalePD.getStartDate();
        saleCheckTplParams.endDate = dataSalePD.getEndDate();

        TariffPlan tariffPlan = tariffPlanRepository.load(dataSalePD.getTariffThere().getTariffPlanCode(), dataSalePD.getTariffThere().getVersionId());
        Carrier carrier = carrierRepository.load(tariffPlan.getCarrierCode(), tariffPlan.getVersionId());

        saleCheckTplParams.carrierName = carrier.getShortName();

        if (dataSalePD.isIncludeFee() && dataSalePD.getProcessingFee() != null) {
            Logger.debug(TAG, "buildParams() called" + dataSalePD.getFeeValue() + "  " + dataSalePD.getFeeVatRate() + " " + dataSalePD.getFeeVatValue());
            params.feeValue = dataSalePD.getFeeValue();
            params.feeVatRate = dataSalePD.getFeeVatRate();
            params.feeVatValue = dataSalePD.getFeeVatValue();
        }

        if (dataSalePD.getParentTicketInfo() != null) {
            saleCheckTplParams.parentPdNumber = dataSalePD.getParentTicketInfo().getTicketNumber();
            saleCheckTplParams.parentPdDeviceId = dataSalePD.getParentTicketInfo().getCashRegisterNumber();
            saleCheckTplParams.parentPdSaleDateTime = dataSalePD.getParentTicketInfo().getSaleDateTime();
            saleCheckTplParams.connectionType = dataSalePD.getConnectionType();
        }

        if (dataSalePD.getExemptionForEvent() != null) {
            saleCheckTplParams.exemptionSmartCard = dataSalePD.getExemptionForEvent()
                    .getSmartCardFromWhichWasReadAboutExemption();
            saleCheckTplParams.exemptionCode = dataSalePD.getExemptionForEvent().getExpressCode();
            saleCheckTplParams.exemptionOrganizationName = dataSalePD.getExemptionForEvent().getOrganization();
            saleCheckTplParams.exemptionDocumentNumber = dataSalePD.getExemptionForEvent()
                    .getNumberOfDocumentWhichApproveExemption();
        }

        params.headerParams = Di.INSTANCE.fiscalHeaderParamsBuilder().build();

        // Нужно добавить отступы, если после этого нет печати ШК
        int ticketCategoryCode = dataSalePD.getTicketType().getTicketCategoryCode();
        params.addSpaceAfterCheck = dataSalePD.getSmartCard() != null || ticketCategoryChecker.isTrainBaggageTicket(ticketCategoryCode);

        return params;
    }

    /**
     * Выполняет печать документа на принтере
     */
    public Single<DocumentSalePd> print() {
        return Di.INSTANCE.printerManager().getOperationFactory().getGetOdometerValue()
                .call()
                .doOnNext(result -> Globals.getInstance().getPaperUsageCounter().setCurrentOdometerValueBeforePrinting(result.getOdometerValue()))
                .flatMap(result -> Observable.fromCallable(() -> {
                    pdNumber = Di.INSTANCE.documentNumberProvider().getNextDocumentNumber();
                    dataSalePD.setPDNumber(pdNumber);
                    return dataSalePD.getPDNumber();
                }))
                .flatMap(checkNumber -> Di.INSTANCE.printerManager().getOperationFactory().getPrinterPrintSaleCheck(buildParams()).call())
                .doOnNext(result -> printResult = result)
                .flatMap(result -> Observable.just(DocumentSalePd.this))
                .subscribeOn(SchedulersCPPK.printer())
                .toSingle();
    }

    public long getSaleTicketId() {
        return saleTicketId;
    }

    /**
     * Сохраняет событие продажи ПД со статусом {@link ProgressStatus#PrePrinting}
     */
    public Single<DocumentSalePd> initCppkTicketSale() {
        return Single.fromCallable(() -> {

            ShiftEvent shiftEvent = getCashRegisterWorkingShiftDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            TariffPlan tariffPlan = tariffPlanRepository.load(dataSalePD.getTariffThere().getTariffPlanCode(), dataSalePD.getTariffThere().getVersionId());
            Carrier carrier = carrierRepository.load(tariffPlan.getCarrierCode(), tariffPlan.getVersionId());
            TrainInfo trainInfo = new TrainInfoGenerator()
                    .setTrainCategory(tariffPlan.getTrainCategory(nsiDaoSession))
                    .build();

            LegalEntity legalEntity = new LegalEntity();
            legalEntity.setCode(carrier.getCode());
            legalEntity.setName(carrier.getName());
            legalEntity.setInn(carrier.getInn());

            TicketTapeEvent ticketTapeEvent = localDaoSession.getTicketTapeEventDao().getInstalledTicketTape();
            SmartCard smartCard = dataSalePD.getSmartCard();
            AdditionalInfoForEtt additionalInfoForEtt = dataSalePD.getAdditionalInfoForEtt();
            Fee fee = dataSalePD.getFee();
            Price price = dataSalePD.getPrice();
            ParentTicketInfo parentTicketInfo = dataSalePD.getParentTicketInfo(); // информация об исходном ПД
            ExemptionForEvent exemptionForEvent = dataSalePD.getExemptionForEvent(); // льгота
            BankTransactionEvent bankTransactionEvent = dataSalePD.getBankTransactionEvent();
            Tariff tariff = dataSalePD.getTariffThere(); // тариф, по которому формируется ПД
            String ticketTypeShortName = dataSalePD.getTicketType().getShortName();
            TicketWayType wayType = dataSalePD.getDirection(); // направление для ПД(туда/туда-обратно)
            String ticketType = dataSalePD.getTariffThere().getTicketType(nsiDaoSession).getExpressTicketTypeCode();
            int ticketCategoryCode = dataSalePD.getTariffThere().getTicketType(nsiDaoSession).getTicketCategory(nsiDaoSession).getCode();
            TicketKind ticketKind = getTicketKind(dataSalePD.getTicketType(), exemptionForEvent); // тип билета(детский/полный/льготный)
            // В будущем: если ситуация изменится, добавить здесь создание или извлечение seasonTicket
            SeasonTicket seasonTicket = null; // на ПТК пока это поле всегда null
            PaymentType paymentType = dataSalePD.getPaymentType();
            Boolean isTicketWritten = false; // при инициализации ставим флаг в true

            localDaoSession.beginTransaction();
            try {

                // Сохранение ticketEventBase

                Preconditions.checkNotNull(shiftEvent, "Cash register working shift is null");
                //Событие смены не записываем заново, а берем существующее
                long shiftEventId = shiftEvent.getId();
                if (shiftEventId == -1) {
                    //Т.к. событие смены не записывается заново, а берется существуюущее, то и ид не может быть отрицательным
                    throw new IllegalStateException("CashRegisterWorkingShiftId must be positive");
                }

                if (smartCard != null) {
                    localDaoSession.getSmartCardDao().save(smartCard);
                }

                if (wayType == null || tariff == null || ticketTypeShortName == null || ticketType == null) {
                    throw new IllegalArgumentException("Not all entities for insert to database is created");
                }

                TicketEventBase ticketEventBase = new TicketEventBaseGenerator()
                        .setSmartCard(smartCard)
                        .setWayType(wayType)
                        .setTicketTypeCode(tariff.getTicketTypeCode())
                        .setTicketCategoryCode(ticketCategoryCode)
                        .setDepartureStationCode(tariff.getStationDepartureCode())
                        .setDestinationStationCode(tariff.getStationDestinationCode())
                        .setTariffCode(Long.valueOf(tariff.getCode()))
                        .setTicketTypeShortName(ticketTypeShortName)
                        .setType(ticketType)
                        .setCurrentShift(shiftEvent)
                        .setStartDayOffset(dataSalePD.getTerm())
                        .build();

                Preconditions.checkNotNull(ticketEventBase, "TicketEventBase is null");
                localDaoSession.getTicketEventBaseDao().insertOrThrow(ticketEventBase);

                // Сохранение ticketSaleReturnEventBase

                if (exemptionForEvent != null) {
                    long exemptionId = localDaoSession.exemptionDao().insertExemption(exemptionForEvent);
                    if (exemptionId <= 0) {
                        throw new IllegalStateException("Exemption id is wrong");
                    }
                }

                if (parentTicketInfo != null) {
                    localDaoSession.getParentTicketInfoDao().insertOrThrow(parentTicketInfo);
                }

                Preconditions.checkNotNull(legalEntity, "Carrier is null");
                localDaoSession.legalEntityDao().insertOrThrow(legalEntity);

                Preconditions.checkNotNull(price, "Price is null");
                localDaoSession.getPriceDao().insertOrThrow(price);

                Preconditions.checkNotNull(ticketKind, "ticketKind is null");

                if (additionalInfoForEtt != null) {
                    localDaoSession.getAdditionalInfoForEttDao().insertOrThrow(additionalInfoForEtt);
                }

                if (seasonTicket != null) {
                    localDaoSession.seasonTicketDao().insertOrThrow(seasonTicket);
                }

                Preconditions.checkNotNull(trainInfo, "TrainInfo is null");
                localDaoSession.trainInfoDao().insertOrThrow(trainInfo);

                if (fee != null) {
                    localDaoSession.getFeeDao().insertOrThrow(fee);
                }

                if (price == null || ticketKind == null || trainInfo == null || paymentType == null || ticketEventBase == null) {
                    throw new IllegalArgumentException("Not all entities for insert to database is created");
                }

                TicketSaleReturnEventBase ticketSaleReturnEventBase = new TicketSaleReturnEventBaseGenerator()
                        .setExemption(exemptionForEvent)
                        .setLegalEntity(legalEntity)
                        .setFullPrice(price)
                        .setKind(ticketKind)
                        .setFee(fee)
                        .setOneTimeTicket(true) //продаем только разовые ПД
                        .setParentTicket(parentTicketInfo)
                        .setSeasonTicket(seasonTicket)
                        .setTicketEventBase(ticketEventBase)
                        .setTrainInfo(trainInfo)
                        .setPaymentMethod(paymentType)
                        .setBankTransactionEvent(bankTransactionEvent)
                        .setTicketWritten(isTicketWritten)
                        .setAdditionalInfoForEtt(additionalInfoForEtt)
                        .build();

                Preconditions.checkNotNull(ticketSaleReturnEventBase, "TicketSaleReturnEventBase is null");
                localDaoSession.getTicketSaleReturnEventBaseDao().insertOrThrow(ticketSaleReturnEventBase);

                // Сохранение sales

                Preconditions.checkNotNull(ticketTapeEvent, "TicketTapeEvent is null");
                if (ticketTapeEvent.getEndTime() != null) {
                    throw new IllegalStateException("cppkTicketSales.getTicketTapeEvent().getEndTime() != null");
                }

                // добавляем информацию о ПТК
                StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
                if (stationDevice != null) {
                    localDaoSession.getStationDeviceDao().insertOrThrow(stationDevice);
                }

                Event event = Di.INSTANCE.eventBuilder()
                        .setDeviceId(stationDevice.getId())
                        .build();

                Preconditions.checkNotNull(event, "Event is null");
                localDaoSession.getEventDao().insertOrThrow(event);

                CPPKTicketSales sales = new CppkTicketSaleGenerator()
                        .setTicketSaleReturnEventBase(ticketSaleReturnEventBase)
                        .setEvent(event)
                        .setStorageTicketType(smartCard == null
                                ? TicketStorageType.Paper
                                : smartCard.getType())
                        .setTicketTapeEvent(ticketTapeEvent)
                        .setProgressStatus(ProgressStatus.PrePrinting)
                        .setFullTicketPrice(dataSalePD.getFullTicketPrice())
                        .setCouponReadEvent(dataSalePD.getCouponReadEvent())
                        .setConnectionType(dataSalePD.getConnectionType())
                        .build();
                long id = localDaoSession.getCppkTicketSaleDao().insertOrThrow(sales);

                // Сохранение auditTrailEvent
                AuditTrailEventType type;
                if (ticketCategoryChecker.isTransferTicket(ticketCategoryCode)) {
                    type = AuditTrailEventType.TRANSFER_SALE;
                } else if (sales.getConnectionType() == ConnectionType.SURCHARGE) {
                    type = AuditTrailEventType.SALE_WITH_ADD_PAYMENT;
                } else {
                    type = AuditTrailEventType.SALE;
                }

                AuditTrailEvent auditTrailEvent = new AuditTrailEventBuilder()
                        .setType(type)
                        .setExtEventId(id)
                        .setOperationTime(event.getCreationTimestamp())
                        .setShiftEventId(shiftEvent.getId())
                        .setMonthEventId(shiftEvent.getMonthEventId())
                        .build();

                localDaoSession.getAuditTrailEventDao().insertOrThrow(auditTrailEvent);

                localDaoSession.setTransactionSuccessful();

                saleTicketId = id;
            } finally {
                localDaoSession.endTransaction();
            }

            return DocumentSalePd.this;
        });
    }

    /**
     * Обновляет событие продажи ПД до статуса {@link ProgressStatus#CheckPrinted}
     */
    public Single<DocumentSalePd> updateCPPKTicketSale() {
        return Single.fromCallable(() -> {

            BankTransactionEvent bankTransactionEvent =
                    dataSalePD.getBankTransactionEvent();

            CPPKTicketSales lastSaleEvent = localDaoSession.getCppkTicketSaleDao().load(getSaleTicketId());

            if (lastSaleEvent == null) {
                throw new IllegalArgumentException("lastSaleEvent == null");
            }

            TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(lastSaleEvent.getTicketSaleReturnEventBaseId());

            if (ticketSaleReturnEventBase == null) {
                throw new IllegalArgumentException("ticketSaleReturnEventBase == null");
            }

            TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());

            if (ticketEventBase == null) {
                throw new IllegalArgumentException("ticketEventBase == null");
            }

            Check check = new CheckBuilder()
                    .setDocumentNumber(pdNumber)
                    .setSnpdNumber(getSpnd())
                    .setPrintDateTime(getSaleDateTime())
                    .build();

            localDaoSession.beginTransaction();

            try {

                localDaoSession.getCheckDao().insertOrThrow(check);

                //время продажи
                Date saleDateTime = getSaleDateTime(); // с точностью до сеунд
                ticketEventBase.setSaletime(saleDateTime); // с точностью до сеунд

                // дата начала действия ПД
                Calendar calendar = DateUtils.getStartOfDay(saleDateTime);
                calendar.add(Calendar.DAY_OF_MONTH, ticketEventBase.getStartDayOffset());
                Date validFromDate = calendar.getTime();
                ticketEventBase.setValidFromDate(validFromDate); // с точностью до сеунд

                // дата окончания действия ПД
                TicketType ticketType = ticketTypeRepository.load(ticketEventBase.getTypeCode(), nsiVersionManager.getCurrentNsiVersionId());
                int validityPeriodDay = pdValidityPeriodCalculator.calcValidityPeriod(validFromDate, dataSalePD.getDirection(), ticketType, nsiVersionManager.getCurrentNsiVersionId());

                calendar.add(Calendar.DAY_OF_MONTH, validityPeriodDay);
                calendar.add(Calendar.SECOND, -1); // вычитаем 1 секунду, т.к. действует до 23:25:59
                ticketEventBase.setValidTillDate(calendar.getTime());

                localDaoSession.getTicketEventBaseDao().update(ticketEventBase);

                ticketSaleReturnEventBase.setCheckId(check.getId());

                localDaoSession.getTicketSaleReturnEventBaseDao().update(ticketSaleReturnEventBase);

                lastSaleEvent.setProgressStatus(ProgressStatus.CheckPrinted);

                localDaoSession.getCppkTicketSaleDao().update(lastSaleEvent);

                if (bankTransactionEvent != null) {
                    bankTransactionEvent.setStatus(BankTransactionEvent.Status.COMPLETED_FULLY);

                    localDaoSession.getBankTransactionDao().update(bankTransactionEvent);
                }
                localDaoSession.setTransactionSuccessful();
            } finally {
                localDaoSession.endTransaction();
            }

            return DocumentSalePd.this;
        });
    }

    /**
     * Обновляет событие продажи ПД до статуса {@link ProgressStatus#Completed}
     */
    public void completeCppkTicketSaleCommon() {

        CPPKTicketSales lastSaleEvent = localDaoSession.getCppkTicketSaleDao().load(getSaleTicketId());

        if (lastSaleEvent == null) {
            throw new IllegalArgumentException("lastSaleEvent == null");
        }

        TicketSaleReturnEventBase ticketSaleReturnEventBase =
                localDaoSession.getTicketSaleReturnEventBaseDao().load(lastSaleEvent.getTicketSaleReturnEventBaseId());

        if (ticketSaleReturnEventBase == null) {
            throw new IllegalArgumentException("ticketSaleReturnEventBase == null");
        }
        ticketSaleReturnEventBase.setTicketWritten(dataSalePD.isTicketWritten());

        TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());

        if (ticketEventBase == null) {
            throw new IllegalArgumentException("ticketEventBase == null");
        }

        localDaoSession.beginTransaction();
        try {
            localDaoSession.getTicketSaleReturnEventBaseDao().update(ticketSaleReturnEventBase);

            if (signDataResult == null) {
                throw new IllegalArgumentException("signDataResult == null");
            }

            lastSaleEvent.setEDSKeyNumber(signDataResult.getEdsKeyNumber());

            lastSaleEvent.setProgressStatus(ProgressStatus.Completed);
            lastSaleEvent.setErrors(dataSalePD.getWriteError());

            localDaoSession.getCppkTicketSaleDao().update(lastSaleEvent);

            localDaoSession.setTransactionSuccessful();
        } finally {
            localDaoSession.endTransaction();
        }

        Tariff tariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                ticketEventBase.getTariffCode(),
                nsiVersionManager.getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
        );
        Station departureStation = stationRepository.load(ticketEventBase.getDepartureStationCode(), tariff.getVersionId());
        Station destinationStation = stationRepository.load(ticketEventBase.getDestinationStationCode(), tariff.getVersionId());

        recentStationsStatistics.addDepartureStationCode(departureStation.getCode());
        recentStationsStatistics.addDestinationStationCode(destinationStation.getCode());
    }

    /**
     * Обновляет событие продажи ПД до статуса {@link ProgressStatus#Completed}
     */
    public Single<DocumentSalePd> completeCppkTicketSale() {
        return Single.fromCallable(() -> {
            completeCppkTicketSaleCommon();
            return DocumentSalePd.this;
        });
    }

    public int getPdNumber() {
        return pdNumber;
    }

    private int getSpnd() {
        return printResult.getSpnd();
    }

    public Date getSaleDateTime() {
        return printResult.getOperationTime();
    }

    public TicketKind getTicketKind(@NonNull TicketType ticketType, @Nullable ExemptionForEvent exemptionForEvent) {
        TicketKind ticketKind;
        if (exemptionForEvent != null) {
            ticketKind = TicketKind.WithExemption;
        } else if (ticketTypeChecker.isChild(ticketType.getCode())) {
            ticketKind = TicketKind.Child;
        } else {
            ticketKind = TicketKind.Full;
        }
        return ticketKind;
    }

}
