package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT4;

import android.support.annotation.Nullable;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaDecoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Декодер зоны действия type 4.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaT4Decoder implements CoverageAreaDecoder {

    @Nullable
    @Override
    public CoverageArea decode(byte[] data) {
        if (data.length < CoverageAreaT4Structure.COVERAGE_AREA_SIZE)
            return null;

        byte[] productionSectionCodeData = DataCarrierUtils.subArray(data, CoverageAreaT4Structure.PRODUCTION_SECTION_CODE_INDEX, CoverageAreaT4Structure.PRODUCTION_SECTION_CODE_LENGTH);
        long productionSectionCode = DataCarrierUtils.bytesToLong(productionSectionCodeData, ByteOrder.LITTLE_ENDIAN);

        CoverageAreaT4Impl coverageAreaType1 = new CoverageAreaT4Impl();
        coverageAreaType1.setProductionSectionCode(productionSectionCode);

        return coverageAreaType1;
    }

}
