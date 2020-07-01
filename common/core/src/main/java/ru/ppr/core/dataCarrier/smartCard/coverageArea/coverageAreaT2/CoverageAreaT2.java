package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT2;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Зона действия type 2.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageAreaT2 extends CoverageArea {
    /**
     * Возвращает код станции.
     *
     * @return Код станции
     */
    long getStationCode();
}
