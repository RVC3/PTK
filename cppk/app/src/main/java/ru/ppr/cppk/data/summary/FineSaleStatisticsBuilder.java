package ru.ppr.cppk.data.summary;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.nsi.entity.Fine;
import ru.ppr.nsi.repository.FineRepository;

/**
 * Билдер статистики по контролю и продаже услуг.
 *
 * @author Aleksandr Brazhkin
 */
public class FineSaleStatisticsBuilder {

    private final LocalDaoSession localDaoSession;
    private final FineRepository fineRepository;

    private String shiftId;
    private String monthId;
    private boolean buildForLastShift;
    private boolean buildForLastMonth;
    private boolean buildForClosedShiftsOnly;
    private boolean buildWithMonthStatistics = true;

    public FineSaleStatisticsBuilder(LocalDaoSession localDaoSession, FineRepository fineRepository) {
        this.localDaoSession = localDaoSession;
        this.fineRepository = fineRepository;
    }

    /**
     * Задать id смены
     */
    public FineSaleStatisticsBuilder setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public FineSaleStatisticsBuilder setBuildForLastShift(boolean buildForLastShift) {
        this.buildForLastShift = buildForLastShift;
        return this;
    }

    /**
     * Задать id месяца
     */
    public FineSaleStatisticsBuilder setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public FineSaleStatisticsBuilder setBuildForLastMonth(boolean buildForLastMonth) {
        this.buildForLastMonth = buildForLastMonth;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений только для закрытых смен
     *
     * @param buildForClosedShiftsOnly {@code true} - только для закрытых смен, {@code false} - иначе.
     * @return {@code this}
     */
    public FineSaleStatisticsBuilder setBuildForClosedShiftsOnly(boolean buildForClosedShiftsOnly) {
        this.buildForClosedShiftsOnly = buildForClosedShiftsOnly;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений за месяц целиком, по-умолчанию true
     *
     * @param buildWithMonthStatistics {@code true} - собирать за месяц целиком, {@code false} - иначе.
     * @return {@code this}
     */
    public FineSaleStatisticsBuilder setBuildWithMonthStatistics(boolean buildWithMonthStatistics) {
        this.buildWithMonthStatistics = buildWithMonthStatistics;
        return this;
    }

    public Statistics build() {

        boolean forShiftMode = true;
        ShiftEvent workingShifts = null;
        MonthEvent month = null;

        if (buildForLastShift && buildForLastMonth) {
            throw new IllegalArgumentException("buildForLastShift && buildForLastMonth");
        }

        if (shiftId != null && monthId != null) {
            throw new IllegalArgumentException("shiftId != null && monthId != null");
        }

        if ((buildForLastShift || buildForLastMonth) && (shiftId != null || monthId != null)) {
            throw new IllegalArgumentException("(buildForLastShift || buildForLastMonth) && (shiftId != null || monthId != null)");
        }

        if (!buildForLastShift && !buildForLastMonth && shiftId == null && monthId == null) {
            throw new IllegalArgumentException("!buildForLastShift && !buildForLastMonth && shiftId == null && monthId == null");
        }

        if (!buildWithMonthStatistics && !buildForLastShift && shiftId == null) {
            throw new IllegalArgumentException("!buildWithMonthStatistics && !buildForLastShift &&shiftId == null");
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

        // ID смены/месяца; Даты начала, оконачания
        Statistics statistics = new Statistics();
        statistics.shiftId = forShiftMode ? workingShifts.getShiftId() : null;
        statistics.monthId = month.getMonthId();

        // Штрафы
        List<FineSaleEvent> fineSaleEvents;

        if (buildWithMonthStatistics) {
            fineSaleEvents = localDaoSession.getFineSaleEventDao().getFineSaleEventsForMonth(
                    month.getMonthId(),
                    // https://aj.srvdev.ru/browse/CPPKPP-32090
                    // Для месячной ведомости учитываем только закрытые смены
                    buildForClosedShiftsOnly ? EnumSet.of(ShiftEvent.Status.ENDED) : null,
                    EnumSet.of(FineSaleEvent.Status.CHECK_PRINTED, FineSaleEvent.Status.COMPLETED)
            );
        } else {
            fineSaleEvents = localDaoSession.getFineSaleEventDao().getFineSaleEventsForShift(
                    workingShifts.getShiftId(),
                    EnumSet.of(FineSaleEvent.Status.CHECK_PRINTED, FineSaleEvent.Status.COMPLETED)
            );
        }

        boolean fineSaleEventForNeededShift = false;

        for (int i = 0; i < fineSaleEvents.size(); i++) {
            FineSaleEvent fineSaleEvent = fineSaleEvents.get(i);
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().load(fineSaleEvent.getShiftEventId());
            Event event = localDaoSession.getEventDao().load(fineSaleEvent.getEventId());
            Fine fine = fineRepository.load(fineSaleEvent.getFineCode(), event.getVersionId());
            TempItemData tempItemData = new TempItemData();

            if (forShiftMode) {
                if (fineSaleEventForNeededShift) {
                    if (!shiftEvent.getShiftId().equals(workingShifts.getShiftId())) {
                        // Эта смена уже после той, которая нам нужна. Останавливаемся
                        break;
                    }
                } else {
                    if (shiftEvent.getShiftId().equals(workingShifts.getShiftId())) {
                        // Дошли до нужной смены, на ней нужно будет остановиться
                        fineSaleEventForNeededShift = true;
                    }
                }
            }

            BankTransactionEvent bankTransactionEvent =  localDaoSession.getBankTransactionDao().load(fineSaleEvent.getBankTransactionEventId());

            tempItemData.isCardPayment = bankTransactionEvent != null;
            tempItemData.amount = fineSaleEvent.getAmount();
            tempItemData.tax = Decimals.getVATValueIncludedFromRate(fine.getValue(), BigDecimal.valueOf(fine.getNdsPercent()), Decimals.RoundMode.HUNDREDTH);

            if (forShiftMode) {
                // Потому что в ведомости за смену фигурируют показатели за месяц
                incrementCountAndProfit(statistics.monthCountAndProfit, tempItemData);
            }

            if (forShiftMode && !fineSaleEventForNeededShift) {
                // Пропускаем все смены до нужной, если строим отчет за смену
                continue;
            }

            incrementCountAndProfit(statistics.countAndProfit, tempItemData);
        }

        return statistics;
    }

    private void incrementCountAndProfit(CountAndProfit countAndProfit, TempItemData tempItemData) {
        countAndProfit.count.totalCount++;
        countAndProfit.count.cardPaymentCount += tempItemData.isCardPayment ? 1 : 0;
        countAndProfit.count.cashPaymentCount += !tempItemData.isCardPayment ? 1 : 0;
        //////////////////////////////////////////////////////////////
        countAndProfit.profit.total = countAndProfit.profit.total.add(tempItemData.amount);
        countAndProfit.profit.totalCardPaymentSum = countAndProfit.profit.totalCardPaymentSum.add(tempItemData.isCardPayment ? tempItemData.amount : BigDecimal.ZERO);
        countAndProfit.profit.totalCashPaymentSum = countAndProfit.profit.totalCashPaymentSum.add(!tempItemData.isCardPayment ? tempItemData.amount : BigDecimal.ZERO);
        //////////////////////////////////////////////////////////////
        countAndProfit.profit.totalTax = countAndProfit.profit.totalTax.add(tempItemData.tax);
        countAndProfit.profit.totalTaxCardPaymentSum = countAndProfit.profit.totalTaxCardPaymentSum.add(tempItemData.isCardPayment ? tempItemData.tax : BigDecimal.ZERO);
        countAndProfit.profit.totalTaxCashPaymentSum = countAndProfit.profit.totalTaxCashPaymentSum.add(!tempItemData.isCardPayment ? tempItemData.tax : BigDecimal.ZERO);
    }


    public static class Statistics {
        public String shiftId;
        public String monthId;
        public CountAndProfit countAndProfit = new CountAndProfit();
        public CountAndProfit monthCountAndProfit = new CountAndProfit();
    }

    public static class Count {
        public int totalCount;
        public int cashPaymentCount;
        public int cardPaymentCount;
    }

    public static class Profit {
        /**
         * Выручка всего
         */
        public BigDecimal total = BigDecimal.ZERO;
        /**
         * Выручка всего наличкой
         */
        public BigDecimal totalCashPaymentSum = BigDecimal.ZERO;
        /**
         * Выручка всего банковской картой
         */
        public BigDecimal totalCardPaymentSum = BigDecimal.ZERO;
        /**
         * НДС всего
         */
        public BigDecimal totalTax = BigDecimal.ZERO;
        /**
         * НДС всего наличкой
         */
        public BigDecimal totalTaxCashPaymentSum = BigDecimal.ZERO;
        /**
         * НДС всего банковской картой
         */
        public BigDecimal totalTaxCardPaymentSum = BigDecimal.ZERO;
    }

    public static class CountAndProfit {
        public Count count = new Count();
        public Profit profit = new Profit();
    }

    public static class TempItemData {
        public BigDecimal amount = BigDecimal.ZERO;
        public BigDecimal tax = BigDecimal.ZERO;
        public boolean isCardPayment;
    }

}
