package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT1;

import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaDecoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Декодер зоны действия type 1.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaT1Decoder implements CoverageAreaDecoder {

    @Nullable
    @Override
    public CoverageArea decode(byte[] data) {
        if (data.length < CoverageAreaT1Structure.COVERAGE_AREA_SIZE)
            return null;

        return new CoverageAreaT1Impl();
    }

}
