package ru.ppr.core.dataCarrier.pd.base;

/**
 * ПД без места.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithoutPlace extends RealPd {
    /**
     * Возвращает дату начала действия ПД.
     *
     * @return Дата начала действия ПД: количество дней с дня продажи. От 0 (в день продажи) до 31.
     */
    int getStartDayOffset();

    /**
     * Возвращает код тарифа.
     *
     * @return Код тарифа для ПД с местом, с использованием которого сформирован ПД, в соответствии с НСИ
     */
    long getTariffCode();
}
