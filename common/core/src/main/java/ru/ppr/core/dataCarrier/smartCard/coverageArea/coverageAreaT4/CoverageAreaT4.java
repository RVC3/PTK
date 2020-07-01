package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT4;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Зона действия type 4.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageAreaT4 extends CoverageArea {
    /**
     * Возвращает производственного участка.
     *
     * @return Код производственного участка
     */
    long getProductionSectionCode();
}
