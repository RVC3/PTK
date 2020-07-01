package ru.ppr.cppk.logic.base;

import android.util.Pair;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Tariff;

/**
 * Калькулятор цен.
 *
 * @author Dmitry Nevolin
 */
public abstract class PdCostCalculator {

    protected abstract List<Tariff> getTariffsThere();

    protected abstract List<Tariff> getTariffsBack();

    protected abstract TicketWayType getDirection();

    protected abstract List<Pair<ExemptionForEvent, Exemption>> getExemptions();

    protected abstract boolean isIncludeFee();

    protected abstract ProcessingFee getProcessingFee();

    protected abstract int getPdCount();

    /**
     * Возвращает стоимость билета по тарифу без сбора, без учёта скидки.
     *
     * @param tariffIndex Индекс тарифа в группе
     * @return Стоимость билета по тарифу
     */
    public final BigDecimal getOneTicketCostValueWithoutDiscount(int tariffIndex) {
        BigDecimal cost = BigDecimal.ZERO;
        if (getTariffsThere() == null) {
            return cost;
        }
        Tariff tariffThere = getTariffsThere().get(tariffIndex);
        cost = cost.add(tariffThere.getPricePd());
        // getTariffsBack() != null - костыль для трансферов, т.к. у них
        // не существует тарифов обратно, направление определяется по TicketType...
        if (getDirection() == TicketWayType.TwoWay && getTariffsBack() != null) {
            Tariff tariffTBack = getTariffsBack().get(tariffIndex);
            cost = cost.add(tariffTBack.getPricePd());
        }
        return cost;
    }

    /**
     * Возвращает стоимость билета по тарифу без сбора, с учётом скидки.
     *
     * @param tariffIndex Индекс тарифа в группе
     * @return Стоимость билета по тарифу с учётом скидки
     */
    public final BigDecimal getOneTicketCostValueWithDiscount(int tariffIndex) {
        int percentage = 0;

        if (getExemptions() != null) {
            percentage = getExemptions().get(tariffIndex).second.getPercentage();
        }

        BigDecimal ticketCostValueWithoutDiscount = getOneTicketCostValueWithoutDiscount(tariffIndex);

        //округляем в соответствии с "Сначала высчитываем стоимость билета со скидкой, округляем (15.25 -> округляем -> 15.30)"
        //взято из таска https://aj.srvdev.ru/browse/CPPKPP-24054
        return Decimals.round(
                ticketCostValueWithoutDiscount.subtract(Decimals.percentage(new BigDecimal(percentage), ticketCostValueWithoutDiscount, Decimals.RoundMode.WITHOUT)),
                Decimals.RoundMode.TENTH);
    }

    /**
     * Возвращает сумму сбора.
     *
     * @param tariffIndex Индекс тарифа в группе
     * @return Сумма сбора
     */
    public final BigDecimal getOneTicketFeeValue(int tariffIndex) {
        // Сбор взимается только с первого ПД при оформлении транзита
        // http://agile.srvdev.ru/browse/CPPKPP-33911
        return tariffIndex == 0 && isIncludeFee() ? getProcessingFee().getTariff() : BigDecimal.ZERO;
    }

    /**
     * Возвращает полную стоимость с тарифом, сбором и скидкой.
     *
     * @param tariffIndex Индекс тарифа в группе
     * @return Полную стоимость с тарифом, сбором и скидкой
     */
    public final BigDecimal getOneTicketTotalCostValueWithDiscount(int tariffIndex) {
        return getOneTicketFeeValue(tariffIndex).add(getOneTicketCostValueWithDiscount(tariffIndex));
    }

    /**
     * Возвращает утерянную по скидке сумму.
     *
     * @param tariffIndex Индекс тарифа в группе
     * @return Утерянная по скидке сумма
     */
    public final BigDecimal getOneTicketCostLossSum(int tariffIndex) {
        return getOneTicketCostValueWithoutDiscount(tariffIndex).subtract(getOneTicketCostValueWithDiscount(tariffIndex));
    }

    /**
     * Возвращает стоимость билета по тарифу без сбора, без учёта скидки.
     *
     * @return Стоимость билета по тарифу
     */
    public final BigDecimal getOneTicketCostValueWithoutDiscountForAllTariffs() {
        BigDecimal cost = BigDecimal.ZERO;
        for (int i = 0; i < getTariffsThere().size(); i++) {
            cost = cost.add(getOneTicketCostValueWithoutDiscount(i));
        }
        return cost;
    }

    /**
     * Возвращает стоимость билета по тарифу без сбора, с учётом скидки.
     *
     * @return Стоимость билета по тарифу с учётом скидки
     */
    public final BigDecimal getOneTicketCostValueWithDiscountForAllTariffs() {
        BigDecimal cost = BigDecimal.ZERO;
        for (int i = 0; i < getTariffsThere().size(); i++) {
            cost = cost.add(getOneTicketCostValueWithDiscount(i));
        }
        return cost;
    }

    /**
     * Возвращает сумму сбора.
     *
     * @return Сумма сбора
     */
    public final BigDecimal getOneTicketFeeValueForAllTariffs() {
        BigDecimal cost = BigDecimal.ZERO;
        for (int i = 0; i < getTariffsThere().size(); i++) {
            cost = cost.add(getOneTicketFeeValue(i));
        }
        return cost;
    }

    /**
     * Возвращает полную стоимость с тарифом, сбором и скидкой.
     *
     * @return Полную стоимость с тарифом, сбором и скидкой
     */
    public final BigDecimal getOneTicketTotalCostValueWithDiscountForAllTariffs() {
        BigDecimal cost = BigDecimal.ZERO;
        for (int i = 0; i < getTariffsThere().size(); i++) {
            cost = cost.add(getOneTicketTotalCostValueWithDiscount(i));
        }
        return cost;
    }

    /**
     * Возвращает утерянную по скидке сумму.
     *
     * @return Утерянная по скидке сумма
     */
    public final BigDecimal getOneTicketCostLossSumForAllTariffs() {
        BigDecimal cost = BigDecimal.ZERO;
        for (int i = 0; i < getTariffsThere().size(); i++) {
            cost = cost.add(getOneTicketCostLossSum(i));
        }
        return cost;
    }

    /**
     * Возвращает стоимость всех билетов по тарифу без сбора, с учётом скидки.
     *
     * @return Стоимость всех билетов по тарифу с учётом скидки
     */
    public final BigDecimal getAllTicketsTotalCostValueWithDiscount() {
        return getOneTicketTotalCostValueWithDiscountForAllTariffs().multiply(new BigDecimal(getPdCount()));
    }

}
