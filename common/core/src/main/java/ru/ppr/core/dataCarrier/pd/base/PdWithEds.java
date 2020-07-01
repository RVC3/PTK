package ru.ppr.core.dataCarrier.pd.base;

/**
 * ПД с ЭЦП.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithEds extends Pd {
    /**
     * Возвращает ЭЦП.
     *
     * @return ЭЦП
     */
    byte[] getEds();
}
