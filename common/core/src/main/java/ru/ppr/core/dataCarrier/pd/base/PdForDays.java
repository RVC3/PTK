package ru.ppr.core.dataCarrier.pd.base;

/**
 * ПД, действующий по датам.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdForDays extends Pd {
    /**
     * Возвращает даты действия ПД.
     *
     * @return Даты действия ПД. Каждому биту соответствует один из дней.
     */
    int getForDays();
}
