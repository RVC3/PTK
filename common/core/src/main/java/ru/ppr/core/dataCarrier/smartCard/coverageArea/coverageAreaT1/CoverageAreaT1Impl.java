package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT1;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaType;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.BaseCoverageArea;

/**
 * Зона действия type 1.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaT1Impl extends BaseCoverageArea implements CoverageAreaT1 {

    public CoverageAreaT1Impl() {
        super(CoverageAreaType.ALL_AREA);
    }
}
