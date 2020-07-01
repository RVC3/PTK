package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT3;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Зона действия type 3.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageAreaT3 extends CoverageArea {
    /**
     * Возвращает код направления.
     *
     * @return Код направления
     */
    long getDirectionCode();
}
