package ru.ppr.core.dataCarrier.smartCard.coverageArea;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Декодер списка зон действия по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultCoverageAreaListDecoder implements CoverageAreaListDecoder {

    private static final byte COVERAGE_AREA_SIZE_IN_BYTES = 8;

    private final CoverageAreaDecoderFactory coverageAreaDecoderFactory;

    public DefaultCoverageAreaListDecoder(CoverageAreaDecoderFactory coverageAreaDecoderFactory) {
        this.coverageAreaDecoderFactory = coverageAreaDecoderFactory;
    }

    @NonNull
    @Override
    public List<CoverageArea> decode(@NonNull byte[] data) {
        List<CoverageArea> coverageAreaList = new ArrayList<>();
        int count = data.length / COVERAGE_AREA_SIZE_IN_BYTES;
        for (int i = 0; i < count; i++) {
            int startIndex = i * COVERAGE_AREA_SIZE_IN_BYTES;
            byte[] coverageAreaData = DataCarrierUtils.subArray(data, startIndex, COVERAGE_AREA_SIZE_IN_BYTES);
            CoverageAreaDecoder coverageAreaDecoder = coverageAreaDecoderFactory.create(coverageAreaData);
            CoverageArea coverageArea = coverageAreaDecoder.decode(coverageAreaData);
            coverageAreaList.add(coverageArea);
        }
        return coverageAreaList;
    }
}
