package ru.ppr.core.dataCarrier.pd.base;

/**
 * ПД со льготой.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithExemption extends Pd {
    /**
     * Возвращает код льготы.
     *
     * @return Код льготы
     */
    int getExemptionCode();
}
