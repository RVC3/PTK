package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT3;

import android.support.annotation.Nullable;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaDecoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Декодер зоны действия type 3.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaT3Decoder implements CoverageAreaDecoder {

    @Nullable
    @Override
    public CoverageArea decode(byte[] data) {
        if (data.length < CoverageAreaT3Structure.COVERAGE_AREA_SIZE)
            return null;

        byte[] directionCodeData = DataCarrierUtils.subArray(data, CoverageAreaT3Structure.DIRECTION_CODE_INDEX, CoverageAreaT3Structure.DIRECTION_CODE_LENGTH);
        long directionCode = DataCarrierUtils.bytesToLong(directionCodeData, ByteOrder.LITTLE_ENDIAN);

        CoverageAreaT3Impl coverageAreaType1 = new CoverageAreaT3Impl();
        coverageAreaType1.setDirectionCode(directionCode);

        return coverageAreaType1;
    }

}
