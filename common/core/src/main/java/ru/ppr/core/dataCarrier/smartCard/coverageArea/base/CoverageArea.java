package ru.ppr.core.dataCarrier.smartCard.coverageArea.base;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaType;

/**
 * Зона действия.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageArea {

    /**
     * Возвращает тип зоны действия
     *
     * @return Тип зоны действия
     */
    CoverageAreaType getCoverageAreaType();

}
