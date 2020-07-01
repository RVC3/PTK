package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT2;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaType;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.BaseCoverageArea;

/**
 * Зона действия type 2.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaT2Impl extends BaseCoverageArea implements CoverageAreaT2 {

    private long stationCode;

    public CoverageAreaT2Impl() {
        super(CoverageAreaType.SINGLE_STATION);
    }

    @Override
    public long getStationCode() {
        return stationCode;
    }

    public void setStationCode(long stationCode) {
        this.stationCode = stationCode;
    }
}
