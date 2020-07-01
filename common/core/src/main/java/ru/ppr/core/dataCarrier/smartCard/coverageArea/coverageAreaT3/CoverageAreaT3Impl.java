package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT3;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaType;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.BaseCoverageArea;

/**
 * Зона действия type 3.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaT3Impl extends BaseCoverageArea implements CoverageAreaT3 {

    private long directionCode;

    public CoverageAreaT3Impl() {
        super(CoverageAreaType.SINGLE_DIRECTION);
    }

    @Override
    public long getDirectionCode() {
        return directionCode;
    }

    public void setDirectionCode(long directionCode) {
        this.directionCode = directionCode;
    }
}
