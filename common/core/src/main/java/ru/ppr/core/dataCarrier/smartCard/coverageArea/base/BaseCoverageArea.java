package ru.ppr.core.dataCarrier.smartCard.coverageArea.base;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaType;

/**
 * Базовый класс для зон действия.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseCoverageArea implements CoverageArea {

    private final CoverageAreaType coverageAreaType;

    public BaseCoverageArea(CoverageAreaType coverageAreaType) {
        this.coverageAreaType = coverageAreaType;
    }

    public CoverageAreaType getCoverageAreaType() {
        return coverageAreaType;
    }
}
