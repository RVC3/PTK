package ru.ppr.core.dataCarrier.pd.base;

/**
 * ПД с контрольной суммой.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithCrc extends Pd {
    /**
     * Возвращает контрольную сумму.
     *
     * @return Контрольная сумма
     */
    byte[] getCrc();
}
