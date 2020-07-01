package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT4;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaType;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.BaseCoverageArea;

/**
 * Зона действия type 4.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaT4Impl extends BaseCoverageArea implements CoverageAreaT4 {

    private long productionSectionCode;

    public CoverageAreaT4Impl() {
        super(CoverageAreaType.SINGLE_PRODUCTION_SECTION);
    }

    @Override
    public long getProductionSectionCode() {
        return productionSectionCode;
    }

    public void setProductionSectionCode(long productionSectionCode) {
        this.productionSectionCode = productionSectionCode;
    }
}
