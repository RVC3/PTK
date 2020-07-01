package ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT2;

import android.support.annotation.Nullable;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaDecoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Декодер зоны действия type 2.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaT2Decoder implements CoverageAreaDecoder {

    @Nullable
    @Override
    public CoverageArea decode(byte[] data) {
        if (data.length < CoverageAreaT2Structure.COVERAGE_AREA_SIZE)
            return null;

        byte[] stationCodeData = DataCarrierUtils.subArray(data, CoverageAreaT2Structure.STATION_CODE_INDEX, CoverageAreaT2Structure.STATION_CODE_LENGTH);
        long stationCode = DataCarrierUtils.bytesToLong(stationCodeData, ByteOrder.LITTLE_ENDIAN);

        CoverageAreaT2Impl coverageAreaType1 = new CoverageAreaT2Impl();
        coverageAreaType1.setStationCode(stationCode);

        return coverageAreaType1;
    }

}
