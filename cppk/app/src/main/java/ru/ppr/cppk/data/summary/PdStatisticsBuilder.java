package ru.ppr.cppk.data.summary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CPPKServiceSale;
import ru.ppr.cppk.entity.event.base34.CPPKTicketControl;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.LegalEntity;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model.TicketKind;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Carrier;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ExemptionGroup;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.TariffRepository;

/**
 * Билдер статистики по контролю и продаже ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdStatisticsBuilder {

    private int nsiVersion;
    private String shiftId;
    private String monthId;
    private boolean buildForLastShift;
    private boolean buildForLastMonth;
    private boolean buildForClosedShiftsOnly;
    private boolean buildWithMonthStatistics = true;
    ////////////////////////////////////////

    public PdStatisticsBuilder(int nsiVersion) {
        this.nsiVersion = nsiVersion;
    }

    /**
     * Задать id смены
     */
    public PdStatisticsBuilder setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public PdStatisticsBuilder setBuildForLastShift(boolean buildForLastShift) {
        this.buildForLastShift = buildForLastShift;
        return this;
    }

    /**
     * Задать id месяца
     */
    public PdStatisticsBuilder setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public PdStatisticsBuilder setBuildForLastMonth(boolean buildForLastMonth) {
        this.buildForLastMonth = buildForLastMonth;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений только для закрытых смен
     *
     * @param buildForClosedShiftsOnly {@code true} - только для закрытых смен, {@code false} - иначе.
     * @return {@code this}
     */
    public PdStatisticsBuilder setBuildForClosedShiftsOnly(boolean buildForClosedShiftsOnly) {
        this.buildForClosedShiftsOnly = buildForClosedShiftsOnly;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений за месяц целиком
     *
     * @param buildWithMonthStatistics {@code true} - собирать за месяц целиком, {@code false} - иначе.
     * @return {@code this}
     */
    public PdStatisticsBuilder setBuildWithMonthStatistics(boolean buildWithMonthStatistics) {
        this.buildWithMonthStatistics = buildWithMonthStatistics;
        return this;
    }

    public Statistics build() {
        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();
        NsiDaoSession nsiDaoSession = Globals.getInstance().getNsiDaoSession();
        TariffRepository tariffRepository = Dagger.appComponent().tariffRepository();
        TicketCategoryChecker ticketCategoryChecker = Dagger.appComponent().ticketCategoryChecker();

        boolean forShiftMode = true;
        ShiftEvent workingShifts = null;
        MonthEvent month = null;
        Date fromTimeStampShift = null;
        Date toTimeStampShift = null;
        Date fromTimeStampMonth = null;
        Date toTimeStampMonth = null;
        Date fromTimeStamp = null;
        Date toTimeStamp = null;

        if (buildForLastShift && buildForLastMonth) {
            throw new IllegalArgumentException("buildForLastShift && buildForLastMonth");
        }

        if (shiftId != null && monthId != null) {
            throw new IllegalArgumentException("shiftId != null && monthId != null");
        }

        if ((buildForLastShift || buildForLastMonth) && (shiftId != null || monthId != null)) {
            throw new IllegalArgumentException("(buildForLastShift || buildForLastMonth) && (shiftId != null || monthId != null)");
        }

        if (buildForLastShift) {
            // Для последней смены
            workingShifts = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShifts == null) {
                throw new IllegalStateException("last shift is null");
            }
            forShiftMode = true;
        }

        if (buildForLastMonth) {
            // Для последнего месяца
            month = localDaoSession.getMonthEventDao().getLastMonthEvent();
            if (month == null) {
                throw new IllegalStateException("last month is null");
            }
            forShiftMode = false;
        }

        if (shiftId != null) {
            // Для конкретной смены
            workingShifts = localDaoSession.getShiftEventDao().getLastCashRegisterWorkingShiftByShiftId(shiftId, ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShifts == null) {
                throw new IllegalStateException("shift is null");
            }
            forShiftMode = true;
        }

        if (monthId != null) {
            // Для конкретного месяца
            month = localDaoSession.getMonthEventDao().getLastMonthByMonthId(monthId);
            if (month == null) {
                throw new IllegalStateException("month is null");
            }
            forShiftMode = false;
        }

        if (forShiftMode) {
            // Для смены получаем месяц
            month = localDaoSession.getMonthEventDao().getMonthEventById(workingShifts.getMonthEventId());
            if (month == null) {
                throw new IllegalStateException("month is null");
            }
        }

        // Время начала/окончания
        fromTimeStampMonth = month.getOpenDate();
        toTimeStampMonth = month.getCloseDate();
        if (forShiftMode) {
            fromTimeStampShift = workingShifts.getStartTime();
            toTimeStampShift = workingShifts.getCloseTime();
            toTimeStamp = fromTimeStampShift;
            fromTimeStamp = fromTimeStampShift;
        } else {
            toTimeStamp = fromTimeStampMonth;
            fromTimeStamp = toTimeStampMonth;
        }

        // ID смены/месяца; Даты начала, оконачания
        Statistics statistics = new Statistics();
        statistics.shiftId = forShiftMode ? workingShifts.getShiftId() : null;
        statistics.monthId = month.getMonthId();
        statistics.fromDate = fromTimeStamp;
        statistics.toDate = toTimeStamp;

        // Продажи и аннулирования
        EnumSet<ProgressStatus> progressStatuses = EnumSet.of(ProgressStatus.Completed, ProgressStatus.CheckPrinted);
        List<CPPKTicketSales> cppkTicketSalesList;
        if (buildWithMonthStatistics) {
            cppkTicketSalesList = localDaoSession
                    .getCppkTicketSaleDao()
                    .getSaleEventsForMonth(
                            month.getMonthId(),
                            // https://aj.srvdev.ru/browse/CPPKPP-32090
                            // Для месячной ведомости учитываем только закрытые смены
                            buildForClosedShiftsOnly ? EnumSet.of(ShiftEvent.Status.ENDED) : null,
                            progressStatuses,
                            false
                    );
        } else {
            cppkTicketSalesList = localDaoSession
                    .getCppkTicketSaleDao()
                    .getSaleEventsForShift(
                            workingShifts.getShiftId(),
                            progressStatuses,
                            false
                    );
        }
        boolean saleEventForNeededShift = false;
        for (int i = 0; i < cppkTicketSalesList.size(); i++) {
            CPPKTicketSales cppkTicketSales = cppkTicketSalesList.get(i);
            TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
            TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().load(ticketEventBase.getShiftEventId());
            TicketKind ticketKind = ticketSaleReturnEventBase.getKind();
            CPPKTicketReturn cppkTicketReturn = localDaoSession.getCppkTicketReturnDao()
                    .findLastPdRepealEventForPdSaleEvent(cppkTicketSales.getId(), EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed));
            LegalEntity legalEntity = localDaoSession.legalEntityDao().load(ticketSaleReturnEventBase.getLegalEntityId());
            TrainInfo trainInfo = localDaoSession.trainInfoDao().load(ticketSaleReturnEventBase.getTrainInfoId());
            Tariff tariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(
                    ticketEventBase.getTariffCode(),
                    Di.INSTANCE.nsiVersionManager().getNsiVersionIdForDate(ticketEventBase.getSaledateTime())
            );
            TicketType ticketType = tariff.getTicketType(nsiDaoSession);
            TicketCategory ticketCategory = ticketType.getTicketCategory(nsiDaoSession);
            Fee fee = localDaoSession.getFeeDao().load(ticketSaleReturnEventBase.getFeeId());

            TempItemData tempItemData = new TempItemData();

            if (forShiftMode) {
                if (saleEventForNeededShift) {
                    if (!shiftEvent.getShiftId().equals(workingShifts.getShiftId())) {
                        // Эта смена уже после той, которая нам нужна. Останавливаемся
                        break;
                    }
                } else {
                    if (shiftEvent.getShiftId().equals(workingShifts.getShiftId())) {
                        // Дошли до нужной смены, на ней нужно будет остановиться
                        saleEventForNeededShift = true;
                    }
                }
            }

            tempItemData.isTransfer = cppkTicketSales.getConnectionType() == ConnectionType.TRANSFER;
            tempItemData.isReturn = cppkTicketReturn != null;
            BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
            tempItemData.isCardPayment = bankTransactionEvent != null;
            ExemptionForEvent exemptionForEvent = localDaoSession.exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
            tempItemData.lossSum = exemptionForEvent == null ? BigDecimal.ZERO : exemptionForEvent.getLossSumm();
            tempItemData.tariff = cppkTicketSales.getFullTicketPrice().subtract(tempItemData.lossSum);
            tempItemData.fee = fee == null ? BigDecimal.ZERO : fee.getTotal();
            Price fullPrice = localDaoSession.getPriceDao().load(ticketSaleReturnEventBase.getFullPriceId());
            tempItemData.tariffVat = fullPrice.getNds().subtract(fee == null ? BigDecimal.ZERO : fee.getNds());
            tempItemData.feeVat = fee == null ? BigDecimal.ZERO : fee.getNds();
            tempItemData.feeType = fee == null ? null : fee.getFeeType();
            tempItemData.tariffAndFee = tempItemData.tariff.add(tempItemData.fee);

            if (forShiftMode) {
                // Потому что в ведомости за смену фигурируют показатели за месяц
                incrementCountAndProfit(statistics.monthCountAndProfit, tempItemData);
            }

            if (forShiftMode && !saleEventForNeededShift) {
                // Пропускаем все смены до нужной, если строим отчет за смену
                continue;
            }

            statistics.documentCount.totalCount++;

            if (tempItemData.isReturn) {
                statistics.documentCount.trainRepealCount++;
                statistics.documentCount.totalCount++;
            }

            // Определяется текущий тип билета
            TicketTypeStatistics ticketTypeStatistics = null;
            int ticketCategoryCode = ticketCategory.getCode();

            if (ticketCategoryChecker.isTrainOneOffTicket(ticketCategoryCode)) {
                // Разовые ПД на поезд
                statistics.documentCount.trainSingleCount++;
                if (ticketType.getCode() == TicketType.Code.SINGLE_FULL) {
                    // Полные
                    ticketTypeStatistics = statistics.ticketTypeFullStatistics;
                } else if (ticketType.getCode() == TicketType.Code.SINGLE_CHILD) {
                    // Детские
                    ticketTypeStatistics = statistics.ticketTypeChildStatistics;
                }
            } else if (ticketCategoryChecker.isTrainBaggageTicket(ticketCategoryCode)) {
                // Квитанции на багаж в поезде
                statistics.documentCount.trainBaggageCount++;
                ticketTypeStatistics = statistics.ticketTypeBaggageStatistics;
            } else if (ticketCategoryChecker.isTransferTicket(ticketCategoryCode)) {
                // трансферы
                if (ticketCategoryChecker.isTransferSingleTicket(ticketCategoryCode)) {
                    statistics.documentCount.transferSingleCount++;
                } else {
                    statistics.documentCount.transferSeasonCount++;
                }
                ticketTypeStatistics = statistics.ticketTypeTransferStatistics;
            }

            if (ticketTypeStatistics == null) {
                throw new IllegalStateException("ticketTypeStatistics is null");
            }

            // По доплате
            if (cppkTicketSales.getConnectionType() == ConnectionType.SURCHARGE) {
                // Количество и деньги по документам по доплате
                incrementCountAndProfit(ticketTypeStatistics.withAddPaymentCountAndProfit, tempItemData);
                statistics.documentCount.trainSingleWithAddPaymentCount++;
            }

            // Определяется текущее направление
            DirectionStatistics directionStatistics = null;
            ParentTicketInfo parentTicketInfo = localDaoSession.getParentTicketInfoDao().load(ticketSaleReturnEventBase.getParentTicketInfoId());
            if (TicketWayType.TwoWay.equals(ticketEventBase.getWayType()) && parentTicketInfo == null) {
                directionStatistics = ticketTypeStatistics.directionThereBackStatistics;
            } else {
                directionStatistics = ticketTypeStatistics.directionThereStatistics;
            }
            if (directionStatistics == null) {
                throw new IllegalStateException("directionStatistics is null");
            }

            // Определяется текущий тип билета в рамках направления.
            TicketTypeInDirectionStatistics ticketTypeInDirectionStatistics = null;
            for (TicketTypeInDirectionStatistics ticketTypeInDirectionStatisticsInList : directionStatistics.ticketTypeInDirectionStatisticsList) {
                if (ticketType.getCode() == ticketTypeInDirectionStatisticsInList.ticketType.getCode()) {
                    ticketTypeInDirectionStatistics = ticketTypeInDirectionStatisticsInList;
                }
            }
            if (ticketTypeInDirectionStatistics == null) {
                ticketTypeInDirectionStatistics = new TicketTypeInDirectionStatistics();
                ticketTypeInDirectionStatistics.ticketType = ticketType;
                directionStatistics.ticketTypeInDirectionStatisticsList.add(ticketTypeInDirectionStatistics);
            }

            // Определяется текущий маршрут
            int trainCategoryCode = trainInfo.getTrainCategoryCode();
            int routeNum;
            switch (trainCategoryCode) {
                case TrainCategory.CATEGORY_CODE_O: {
                    routeNum = 0;
                    break;
                }
                case TrainCategory.CATEGORY_CODE_7: {
                    if (Carrier.CPPK_CODE.equals(legalEntity.getCode())) {
                        routeNum = 1;
                    } else {
                        routeNum = 2;
                    }
                    break;
                }
                case TrainCategory.CATEGORY_CODE_C: {
                    if (Carrier.CPPK_CODE.equals(legalEntity.getCode())) {
                        routeNum = 1;
                    } else {
                        routeNum = 2;
                    }
                    break;
                }
                case TrainCategory.CATEGORY_CODE_M: {
                    routeNum = 3;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("unknown trainCategoryCode");
                }
            }

            RouteStatistics routeStatistics = null;
            if (statistics.routesStatistics.containsKey(routeNum)) {
                routeStatistics = statistics.routesStatistics.get(routeNum);
            } else {
                routeStatistics = new RouteStatistics();
                statistics.routesStatistics.put(routeNum, routeStatistics);
            }

            RouteCarrierStatistics routeCarrierStatistics = null;
            if (routeStatistics.routeCarriersStatistics.containsKey(legalEntity.getCode())) {
                routeCarrierStatistics = routeStatistics.routeCarriersStatistics.get(legalEntity.getCode());
            } else {
                routeCarrierStatistics = new RouteCarrierStatistics(legalEntity.getName(), legalEntity.getCode());
                routeStatistics.routeCarriersStatistics.put(legalEntity.getCode(), routeCarrierStatistics);
            }

            RouteCarrierTrainCategoryStatistics routeCarrierTrainCategoryStatistics = null;
            if (routeCarrierStatistics.routeCarrierTrainCategoriesStatistics.containsKey(trainInfo)) {
                routeCarrierTrainCategoryStatistics = routeCarrierStatistics.routeCarrierTrainCategoriesStatistics.get(trainInfo);
            } else {
                routeCarrierTrainCategoryStatistics = new RouteCarrierTrainCategoryStatistics();
                routeCarrierStatistics.routeCarrierTrainCategoriesStatistics.put(trainInfo, routeCarrierTrainCategoryStatistics);
            }

            // Определяется текущий вид льготы
            CountAndProfit directionExemptionTypeCountAndProfit = null;
            CountAndProfit directionExemptionFromSmartCardCountAndProfit = null;
            CountAndProfit routeExemptionTypeCountAndProfit = null;
            CountAndProfit routeExemptionFromSmartCardCountAndProfit = null;
            CountAndProfit directionEttExemptionCountAndProfit = null;
            if (ticketKind == TicketKind.Full || ticketKind == TicketKind.Child) {
                // Скидка 0% (Полный)
                directionExemptionTypeCountAndProfit = directionStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit;
                routeExemptionTypeCountAndProfit = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit;
                directionExemptionFromSmartCardCountAndProfit = directionStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit;
                routeExemptionFromSmartCardCountAndProfit = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit;
                directionEttExemptionCountAndProfit = directionStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.ettCountAndProfit;
            } else if (ticketKind == TicketKind.WithExemption) {
                BigDecimal payedSum = fullPrice.getPayed();
                // https://aj.srvdev.ru/browse/CPPKPP-31622
                // Ошибка: в ведомостях, если безденежный ПД продан со сбором, то он учитывается в блоке «Льготные». Должен учитываться в блоке «Безденежные».
                BigDecimal payedSumExcludeFee = payedSum.subtract(fee == null ? BigDecimal.ZERO : fee.getTotal());
                if (Decimals.isZero(payedSumExcludeFee)) {
                    // Скидка 100% (Безденежный)
                    directionExemptionTypeCountAndProfit = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit;
                    routeExemptionTypeCountAndProfit = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit;
                    directionExemptionFromSmartCardCountAndProfit = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit;
                    routeExemptionFromSmartCardCountAndProfit = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit;
                    directionEttExemptionCountAndProfit = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.ettCountAndProfit;
                } else {
                    // Скидка меньше 100% (Льготный)
                    directionExemptionTypeCountAndProfit = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit;
                    routeExemptionTypeCountAndProfit = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit;
                    directionExemptionFromSmartCardCountAndProfit = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit;
                    routeExemptionFromSmartCardCountAndProfit = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit;
                    directionEttExemptionCountAndProfit = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.ettCountAndProfit;
                }
            }
            if (directionExemptionTypeCountAndProfit == null) {
                throw new IllegalStateException("exemptionTypeCountAndProfit is null");
            }
            if (routeExemptionTypeCountAndProfit == null) {
                throw new IllegalStateException("routeExemptionTypeCountAndProfit is null");
            }
            if (directionExemptionFromSmartCardCountAndProfit == null) {
                throw new IllegalStateException("directionExemptionFromSmartCardCountAndProfit is null");
            }
            if (routeExemptionFromSmartCardCountAndProfit == null) {
                throw new IllegalStateException("routeExemptionFromSmartCardCountAndProfit is null");
            }
            if (directionEttExemptionCountAndProfit == null) {
                throw new IllegalStateException("directionEttExemptionCountAndProfit is null");
            }

            // Для льгот, считанных с БСК
            if (exemptionForEvent != null) {
                SmartCard smartCardFromWhichWasReadAboutExemption = exemptionForEvent.getSmartCardFromWhichWasReadAboutExemption();
                if (smartCardFromWhichWasReadAboutExemption != null) {
                    incrementCountAndProfit(directionExemptionFromSmartCardCountAndProfit, tempItemData);
                    incrementCountAndProfit(routeExemptionFromSmartCardCountAndProfit, tempItemData);
                    // Для льгот, считанных с ЭТТ
                    if (smartCardFromWhichWasReadAboutExemption.getType() == TicketStorageType.ETT) {
                        incrementCountAndProfit(directionEttExemptionCountAndProfit, tempItemData);
                    }
                }
            }

            // Определяется конкретная льгота и её группа
            if (exemptionForEvent != null) {
                Exemption exemption = Dagger.appComponent().exemptionRepository().getExemption(
                        exemptionForEvent.getCode(),
                        exemptionForEvent.getActiveFromDate(),
                        exemptionForEvent.getVersionId()
                );

                if (exemption == null) {
                    throw new IllegalStateException("exemption is null");
                }

                ExemptionGroup exemptionGroup = exemption.getExemptionGroup(Dagger.appComponent().exemptionGroupRepository(), nsiVersion);

                if (exemptionGroup == null) {
                    exemptionGroup = new ExemptionGroup();
                    exemptionGroup.setGroupName("БЕЗ ГРУППЫ");
                }

                ExemptionGroupStatistics routeExemptionGroupStatistics = null;
                if (routeStatistics.exemptionGroupsStatistics.containsKey(exemptionGroup)) {
                    routeExemptionGroupStatistics = routeStatistics.exemptionGroupsStatistics.get(exemptionGroup);
                } else {
                    routeExemptionGroupStatistics = new ExemptionGroupStatistics();
                    routeStatistics.exemptionGroupsStatistics.put(exemptionGroup, routeExemptionGroupStatistics);
                }

                ExemptionStatistics routeExemptionStatistics = null;
                if (routeExemptionGroupStatistics.exemptionsStatistics.containsKey(exemption)) {
                    routeExemptionStatistics = routeExemptionGroupStatistics.exemptionsStatistics.get(exemption);
                } else {
                    routeExemptionStatistics = new ExemptionStatistics();
                    routeExemptionGroupStatistics.exemptionsStatistics.put(exemption, routeExemptionStatistics);
                }

                ExemptionStatistics exemptionStatistics = null;
                if (statistics.exemptionsStatistics.containsKey(exemption)) {
                    exemptionStatistics = statistics.exemptionsStatistics.get(exemption);
                } else {
                    exemptionStatistics = new ExemptionStatistics();
                    statistics.exemptionsStatistics.put(exemption, exemptionStatistics);
                }

                // Количество и деньги по документам по льготе
                incrementCountAndProfit(routeExemptionGroupStatistics.countAndProfit, tempItemData);
                incrementCountAndProfit(routeExemptionStatistics.countAndProfit, tempItemData);
                incrementCountAndProfit(exemptionStatistics.countAndProfit, tempItemData);
                if (ticketKind == TicketKind.Child) {
                    incrementCountAndProfit(routeExemptionStatistics.childCountAndProfit, tempItemData);
                    incrementCountAndProfit(exemptionStatistics.childCountAndProfit, tempItemData);
                }
            }

            //заполним статистику по услугам (сборам)
            if (fee != null) {
                FeeStatisticsDetails feeStatisticsDetails = null;
                if (statistics.feeStatistics.containsKey(fee.getFeeType())) {
                    feeStatisticsDetails = statistics.feeStatistics.get(fee.getFeeType());
                } else {
                    feeStatisticsDetails = new FeeStatisticsDetails();
                    statistics.feeStatistics.put(fee.getFeeType(), feeStatisticsDetails);
                }
                feeStatisticsDetails.count++;
                feeStatisticsDetails.repealCount += tempItemData.isReturn ? 1 : 0;

                feeStatisticsDetails.total = feeStatisticsDetails.total.add(tempItemData.fee);
                feeStatisticsDetails.totalRepeal = feeStatisticsDetails.totalRepeal.add(tempItemData.isReturn ? tempItemData.fee : BigDecimal.ZERO);
            }

            // Количество и деньги по документам
            incrementCountAndProfit(statistics.countAndProfit, tempItemData);
            incrementCountAndProfit(ticketTypeStatistics.countAndProfit, tempItemData);
            incrementCountAndProfit(directionStatistics.presenceOfExemptionStatistics.countAndProfit, tempItemData);
            incrementCountAndProfit(ticketTypeInDirectionStatistics.countAndProfit, tempItemData);
            incrementCountAndProfit(routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit, tempItemData);
            incrementCountAndProfit(directionExemptionTypeCountAndProfit, tempItemData);
            incrementCountAndProfit(routeExemptionTypeCountAndProfit, tempItemData);
        }

        // Проверено документов
        List<CPPKTicketControl> cppkTicketControls;
        if (forShiftMode) {
            cppkTicketControls = localDaoSession
                    .getCppkTicketControlsDao()
                    .getControlEventsForShift(
                            workingShifts.getShiftId()
                    );
        } else {
            cppkTicketControls = localDaoSession
                    .getCppkTicketControlsDao()
                    .getControlEventsForMonth(
                            month.getMonthId(),
                            // https://aj.srvdev.ru/browse/CPPKPP-32090
                            // Для месячной ведомости учитываем только закрытые смены
                            buildForClosedShiftsOnly ? EnumSet.of(ShiftEvent.Status.ENDED) : null
                    );
        }
        for (CPPKTicketControl cppkTicketControl : cppkTicketControls) {
            TicketEventBase controlTicketEventBase = localDaoSession.getTicketEventBaseDao().load(cppkTicketControl.getTicketEventBaseId());
            SmartCard smartCard = localDaoSession.getSmartCardDao().load(controlTicketEventBase.getSmartCardId());
            if (smartCard == null) {
                statistics.controlCount.singleCount++;
            } else {
                boolean isSubscribe = ticketCategoryChecker.isSeasonTicket(controlTicketEventBase.getTicketCategoryCode());
                if (isSubscribe) {
                    statistics.controlCount.bscForPeriodCount++;
                } else {
                    statistics.controlCount.bscSingleCount++;
                }
            }
        }

        // Пробные ПД
        List<TestTicketEvent> testTicketEvents = null;
        if (forShiftMode) {
            testTicketEvents = localDaoSession.getTestTicketDao().getTestTicketEventsForShift(workingShifts.getShiftId(), EnumSet.of(TestTicketEvent.Status.CHECK_PRINTED, TestTicketEvent.Status.COMPLETED));
        } else {
            testTicketEvents = localDaoSession.getTestTicketDao().getTestTicketEventsForMonth(
                    month.getMonthId(),
                    // https://aj.srvdev.ru/browse/CPPKPP-32090
                    // Для месячной ведомости учитываем только закрытые смены
                    buildForClosedShiftsOnly ? EnumSet.of(ShiftEvent.Status.ENDED) : null,
                    EnumSet.of(TestTicketEvent.Status.CHECK_PRINTED, TestTicketEvent.Status.COMPLETED)
            );
        }
        statistics.documentCount.testCount = testTicketEvents.size();
        statistics.documentCount.totalCount += testTicketEvents.size();

        // Услуги (сборы)
        List<CPPKServiceSale> cppkServiceSalesList = localDaoSession.getCppkServiceSaleDao().getServiceSaleEventsForMonth(
                month.getMonthId(),
                // https://aj.srvdev.ru/browse/CPPKPP-32090
                // Для месячной ведомости учитываем только закрытые смены
                buildForClosedShiftsOnly ? EnumSet.of(ShiftEvent.Status.ENDED) : null
        );
        boolean serviceEventForNeededShift = false;
        for (int i = 0; i < cppkServiceSalesList.size(); i++) {
            CPPKServiceSale cppkServiceSale = cppkServiceSalesList.get(i);
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().load(cppkServiceSale.getShiftEventId());

            TempItemData tempItemData = new TempItemData();

            if (forShiftMode) {
                if (serviceEventForNeededShift) {
                    if (!shiftEvent.getShiftId().equals(workingShifts.getShiftId())) {
                        // Эта смена уже после той, которая нам нужна. Останавливаемся
                        break;
                    }
                } else {
                    if (shiftEvent.getShiftId().equals(workingShifts.getShiftId())) {
                        // Дошли до нужной смены, на ней нужно будет остановиться
                        serviceEventForNeededShift = true;
                    }
                }
            }

            // Такая полухардкорная реализация потому, что пока есть только одна услуга - Активизация ЭКЛЗ
            tempItemData.isTransfer = false;
            tempItemData.isReturn = false;
            tempItemData.isCardPayment = false;
            tempItemData.lossSum = BigDecimal.ZERO;
            Price price = localDaoSession.getPriceDao().load(cppkServiceSale.getPriceId());
            tempItemData.tariff = price.getFull();
            tempItemData.fee = BigDecimal.ZERO;
            tempItemData.tariffVat = price.getNds();
            tempItemData.feeVat = BigDecimal.ZERO;
            tempItemData.feeType = null;
            tempItemData.tariffAndFee = tempItemData.tariff.add(tempItemData.fee);

            if (forShiftMode) {
                // Потому что в ведомости за смену фигурируют показатели за месяц
                incrementCountAndProfit(statistics.monthCountAndProfit, tempItemData);
            }

            if (forShiftMode && !serviceEventForNeededShift) {
                // Пропускаем все смены до нужной, если строим отчет за смену
                continue;
            }

            statistics.documentCount.serviceCount++;
            statistics.documentCount.totalCount++;

            incrementCountAndProfit(statistics.serviceCountAndProfit, tempItemData);
            incrementCountAndProfit(statistics.countAndProfit, tempItemData);
        }

        return statistics;
    }


    private void incrementCountAndProfit(CountAndProfit countAndProfit, TempItemData tempItemData) {
        countAndProfit.count.totalCount++;
        countAndProfit.count.totalRepealCount += tempItemData.isReturn ? 1 : 0;
        countAndProfit.count.totalFeeCount += tempItemData.feeType == null ? 0 : 1;
        countAndProfit.count.totalRepealFeeCount += (tempItemData.feeType != null && tempItemData.isReturn) ? 1 : 0;
        countAndProfit.count.cardPaymentCount += tempItemData.isCardPayment ? 1 : 0;
        countAndProfit.count.cardPaymentCountRepeal += tempItemData.isCardPayment && tempItemData.isReturn ? 1 : 0;
        countAndProfit.count.cashPaymentCount += !tempItemData.isCardPayment ? 1 : 0;
        countAndProfit.count.cashPaymentCountRepeal += !tempItemData.isCardPayment && tempItemData.isReturn ? 1 : 0;
        //////////////////////////////////////////////////////////////
        countAndProfit.profit.tariff = countAndProfit.profit.tariff.add(tempItemData.tariff);
        countAndProfit.profit.tariffRepeal = countAndProfit.profit.tariffRepeal.add(tempItemData.isReturn ? tempItemData.tariff : BigDecimal.ZERO);
        countAndProfit.profit.tariffVat = countAndProfit.profit.tariffVat.add(tempItemData.tariffVat);
        countAndProfit.profit.tariffVatRepeal = countAndProfit.profit.tariffVatRepeal.add(tempItemData.isReturn ? tempItemData.tariffVat : BigDecimal.ZERO);
        countAndProfit.profit.fee = countAndProfit.profit.fee.add(tempItemData.fee);
        countAndProfit.profit.feeRepeal = countAndProfit.profit.feeRepeal.add(tempItemData.isReturn ? tempItemData.fee : BigDecimal.ZERO);
        countAndProfit.profit.feeVat = countAndProfit.profit.feeVat.add(tempItemData.feeVat);
        countAndProfit.profit.feeVatRepeal = countAndProfit.profit.feeVatRepeal.add(tempItemData.isReturn ? tempItemData.feeVat : BigDecimal.ZERO);
        countAndProfit.profit.total = countAndProfit.profit.total.add(tempItemData.tariffAndFee);
        countAndProfit.profit.totalRepeal = countAndProfit.profit.totalRepeal.add(tempItemData.isReturn ? tempItemData.tariffAndFee : BigDecimal.ZERO);
        countAndProfit.profit.totalCardPaymentSum = countAndProfit.profit.totalCardPaymentSum.add(tempItemData.isCardPayment ? tempItemData.tariffAndFee : BigDecimal.ZERO);
        countAndProfit.profit.totalCardPaymentSumRepeal = countAndProfit.profit.totalCardPaymentSumRepeal.add(tempItemData.isCardPayment && tempItemData.isReturn ? tempItemData.tariffAndFee : BigDecimal.ZERO);
        countAndProfit.profit.totalCashPaymentSum = countAndProfit.profit.totalCashPaymentSum.add(!tempItemData.isCardPayment ? tempItemData.tariffAndFee : BigDecimal.ZERO);
        countAndProfit.profit.totalCashPaymentSumRepeal = countAndProfit.profit.totalCashPaymentSumRepeal.add(!tempItemData.isCardPayment && tempItemData.isReturn ? tempItemData.tariffAndFee : BigDecimal.ZERO);
        ////разрезы сумм по тарифу и сбор
        countAndProfit.profit.tariffCashPaymentSum = countAndProfit.profit.tariffCashPaymentSum.add(!tempItemData.isCardPayment ? tempItemData.tariff : BigDecimal.ZERO);
        countAndProfit.profit.tariffCashPaymentSumRepeal = countAndProfit.profit.tariffCashPaymentSumRepeal.add(!tempItemData.isCardPayment && tempItemData.isReturn ? tempItemData.tariff : BigDecimal.ZERO);
        countAndProfit.profit.tariffCardPaymentSum = countAndProfit.profit.tariffCardPaymentSum.add(tempItemData.isCardPayment ? tempItemData.tariff : BigDecimal.ZERO);
        countAndProfit.profit.tariffCardPaymentSumRepeal = countAndProfit.profit.tariffCardPaymentSumRepeal.add(tempItemData.isCardPayment && tempItemData.isReturn ? tempItemData.tariff : BigDecimal.ZERO);
        countAndProfit.profit.feeCashPaymentSum = countAndProfit.profit.feeCashPaymentSum.add(!tempItemData.isCardPayment ? tempItemData.fee : BigDecimal.ZERO);
        countAndProfit.profit.feeCashPaymentSumRepeal = countAndProfit.profit.feeCashPaymentSumRepeal.add(!tempItemData.isCardPayment && tempItemData.isReturn ? tempItemData.fee : BigDecimal.ZERO);
        countAndProfit.profit.feeCardPaymentSum = countAndProfit.profit.feeCardPaymentSum.add(tempItemData.isCardPayment ? tempItemData.fee : BigDecimal.ZERO);
        countAndProfit.profit.feeCardPaymentSumRepeal = countAndProfit.profit.feeCardPaymentSumRepeal.add(tempItemData.isCardPayment && tempItemData.isReturn ? tempItemData.fee : BigDecimal.ZERO);
        ////
        countAndProfit.profit.lossSum = countAndProfit.profit.lossSum.add(tempItemData.lossSum);
        countAndProfit.profit.lossSumRepeal = countAndProfit.profit.lossSumRepeal.add(tempItemData.isReturn ? tempItemData.lossSum : BigDecimal.ZERO);
        ///////////////////////////////////////////////////////////////
        countAndProfit.profit.totalVat = countAndProfit.profit.feeVat.add(countAndProfit.profit.tariffVat);
        countAndProfit.profit.totalVatRepeal = countAndProfit.profit.feeVatRepeal.add(countAndProfit.profit.tariffVatRepeal);


    }

    public static class Statistics {
        // деньги и количество за месяц включая эту смену
        public CountAndProfit monthCountAndProfit = new CountAndProfit();
        public String shiftId;
        public String monthId;
        public Date fromDate;
        public Date toDate;
        public DocumentCount documentCount = new DocumentCount();
        public ControlCount controlCount = new ControlCount();
        public CountAndProfit countAndProfit = new CountAndProfit();
        public CountAndProfit serviceCountAndProfit = new CountAndProfit();
        public HashMap<Integer, RouteStatistics> routesStatistics = new HashMap<>();
        public TicketTypeStatistics ticketTypeFullStatistics = new TicketTypeStatistics();
        public TicketTypeStatistics ticketTypeChildStatistics = new TicketTypeStatistics();
        public TicketTypeStatistics ticketTypeBaggageStatistics = new TicketTypeStatistics();
        public TicketTypeStatistics ticketTypeTransferStatistics = new TicketTypeStatistics();
        public HashMap<Exemption, ExemptionStatistics> exemptionsStatistics = new HashMap<>();
        public HashMap<FeeType, FeeStatisticsDetails> feeStatistics = new HashMap<>();
    }

    public static class DirectionStatistics {
        public PresenceOfExemptionStatistics presenceOfExemptionStatistics = new PresenceOfExemptionStatistics();
        public List<TicketTypeInDirectionStatistics> ticketTypeInDirectionStatisticsList = new ArrayList<>();
    }

    public static class TicketTypeInDirectionStatistics {
        public TicketType ticketType;
        public CountAndProfit countAndProfit = new CountAndProfit();
    }

    public static class TicketTypeStatistics {
        public CountAndProfit countAndProfit = new CountAndProfit();
        public CountAndProfit withAddPaymentCountAndProfit = new CountAndProfit();
        public DirectionStatistics directionThereStatistics = new DirectionStatistics();
        public DirectionStatistics directionThereBackStatistics = new DirectionStatistics();
    }

    public static class RouteStatistics {
        public HashMap<String, RouteCarrierStatistics> routeCarriersStatistics = new HashMap<>();
        public HashMap<ExemptionGroup, ExemptionGroupStatistics> exemptionGroupsStatistics = new HashMap<>();
    }

    public static class RouteCarrierStatistics {
        public String carrierName;
        public String carrierId;
        public HashMap<TrainInfo, RouteCarrierTrainCategoryStatistics> routeCarrierTrainCategoriesStatistics = new HashMap<>();

        public RouteCarrierStatistics() {
        }

        public RouteCarrierStatistics(String carrierName, String carrierId) {
            this.carrierName = carrierName;
            this.carrierId = carrierId;
        }
    }

    public static class RouteCarrierTrainCategoryStatistics {
        public PresenceOfExemptionStatistics presenceOfExemptionStatistics = new PresenceOfExemptionStatistics();
    }

    public static class PresenceOfExemptionDetailStatistics {
        public CountAndProfit countAndProfit = new CountAndProfit();
        public ExemptionFromSmartCardStatistics exemptionFromSmartCardStatistics = new ExemptionFromSmartCardStatistics();
    }

    public static class ExemptionFromSmartCardStatistics {
        public CountAndProfit countAndProfit = new CountAndProfit();
        public CountAndProfit ettCountAndProfit = new CountAndProfit();
    }

    public static class PresenceOfExemptionStatistics {
        public CountAndProfit countAndProfit = new CountAndProfit();
        public PresenceOfExemptionDetailStatistics fullPricePresenceOfExemptionDetailStatistics = new PresenceOfExemptionDetailStatistics();
        public PresenceOfExemptionDetailStatistics withExemptionPresenceOfExemptionDetailStatistics = new PresenceOfExemptionDetailStatistics();
        public PresenceOfExemptionDetailStatistics noMoneyPresenceOfExemptionDetailStatistics = new PresenceOfExemptionDetailStatistics();
    }

    public static class ExemptionGroupStatistics {
        public CountAndProfit countAndProfit = new CountAndProfit();
        public HashMap<Exemption, ExemptionStatistics> exemptionsStatistics = new HashMap<>();
    }

    public static class ExemptionStatistics {
        public CountAndProfit countAndProfit = new CountAndProfit();
        public CountAndProfit childCountAndProfit = new CountAndProfit();
    }

    /**
     * Детализация статистики по продаже сборов (услуг)
     */
    public static class FeeStatisticsDetails {
        /**
         * Общее количество
         */
        public int count;
        /**
         * Количество аннулированных
         */
        public int repealCount;
        /**
         * Общая сумма
         */
        public BigDecimal total = BigDecimal.ZERO;
        /**
         * Сумма аннулированных
         */
        public BigDecimal totalRepeal = BigDecimal.ZERO;
    }

    /**
     * Количество документов
     */
    public static class DocumentCount {
        /**
         * Общее количество документов
         */
        public int totalCount;
        /**
         * Пробные ПД
         */
        public int testCount;
        /**
         * Услуги
         */
        public int serviceCount;
        /**
         * Разовые на поезд
         */
        public int trainSingleCount;
        /**
         * Доплаты на поезд
         */
        public int trainSingleWithAddPaymentCount;
        /**
         * Багажные квитанции в поезде
         */
        public int trainBaggageCount;
        /**
         * Аннулированные ПД на поезд
         */
        public int trainRepealCount;
        /**
         * Количество разовых трансферов
         */
        public int transferSingleCount;
        /**
         * Количество абонементов трансферов
         */
        public int transferSeasonCount;
    }

    public static class ControlCount {
        public int bscForPeriodCount;
        public int bscSingleCount;
        public int singleCount;
    }

    public static class Count {
        public int totalCount;
        public int totalRepealCount;
        /**
         * общее количество проданных сборов
         */
        public int totalFeeCount;
        /**
         * Общее количество аннулированных сборов
         */
        public int totalRepealFeeCount;
        public int cashPaymentCount;
        public int cashPaymentCountRepeal;
        public int cardPaymentCount;
        public int cardPaymentCountRepeal;
    }

    public static class Profit {
        /**
         * Выручка всего
         */
        public BigDecimal total = BigDecimal.ZERO;
        /**
         * Сколько денег было аннлировано
         */
        public BigDecimal totalRepeal = BigDecimal.ZERO;
        public BigDecimal totalVat = BigDecimal.ZERO;
        public BigDecimal totalVatRepeal = BigDecimal.ZERO;
        public BigDecimal tariff = BigDecimal.ZERO;
        public BigDecimal tariffRepeal = BigDecimal.ZERO;
        public BigDecimal tariffVat = BigDecimal.ZERO;
        public BigDecimal tariffVatRepeal = BigDecimal.ZERO;
        public BigDecimal feeVat = BigDecimal.ZERO;
        public BigDecimal feeVatRepeal = BigDecimal.ZERO;
        public BigDecimal fee = BigDecimal.ZERO;
        public BigDecimal feeRepeal = BigDecimal.ZERO;
        /**
         * Выручка всего наличкой
         */
        public BigDecimal totalCashPaymentSum = BigDecimal.ZERO;
        /**
         * Наличкой было аннулировано
         */
        public BigDecimal totalCashPaymentSumRepeal = BigDecimal.ZERO;
        /**
         * Выручка всего банковской картой
         */
        public BigDecimal totalCardPaymentSum = BigDecimal.ZERO;
        /**
         * Банковской картой было аннулировано
         */
        public BigDecimal totalCardPaymentSumRepeal = BigDecimal.ZERO;
        /**
         * Выпадвющий доход всего
         */
        public BigDecimal lossSum = BigDecimal.ZERO;
        /**
         * Выпадвющий доход было аннулировано
         */
        public BigDecimal lossSumRepeal = BigDecimal.ZERO;
        /**
         * Наличкой всего в разрезе тарифа
         */
        public BigDecimal tariffCashPaymentSum = BigDecimal.ZERO;
        /**
         * Наличкой было аннулировано в разрезе тарифа
         */
        public BigDecimal tariffCashPaymentSumRepeal = BigDecimal.ZERO;
        /**
         * Банковской картой всего в разрезе тарифа
         */
        public BigDecimal tariffCardPaymentSum = BigDecimal.ZERO;
        /**
         * Банковской картой было аннулировано в разрезе тарифа
         */
        public BigDecimal tariffCardPaymentSumRepeal = BigDecimal.ZERO;
        /**
         * Наличкой всего в разрезе суммы сбора
         */
        public BigDecimal feeCashPaymentSum = BigDecimal.ZERO;
        /**
         * Наличкой было аннулировано в разрезе суммы сбора
         */
        public BigDecimal feeCashPaymentSumRepeal = BigDecimal.ZERO;
        /**
         * Банковской картой всего в разрезе суммы сбора
         */
        public BigDecimal feeCardPaymentSum = BigDecimal.ZERO;
        /**
         * Банковской картой было аннулировано в разрезе суммы сбора
         */
        public BigDecimal feeCardPaymentSumRepeal = BigDecimal.ZERO;
    }

    public static class CountAndProfit {
        public Count count = new Count();
        public Profit profit = new Profit();
    }

    public static class TempItemData {
        public BigDecimal tariff = BigDecimal.ZERO;
        public BigDecimal tariffVat = BigDecimal.ZERO;
        public BigDecimal fee = BigDecimal.ZERO;
        public BigDecimal feeVat = BigDecimal.ZERO;
        public FeeType feeType;
        public BigDecimal tariffAndFee = BigDecimal.ZERO;
        public BigDecimal lossSum = BigDecimal.ZERO;
        public boolean isReturn;
        public boolean isCardPayment;
        public boolean isTransfer;
    }
}
