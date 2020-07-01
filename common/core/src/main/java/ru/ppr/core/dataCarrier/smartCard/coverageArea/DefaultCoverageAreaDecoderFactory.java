package ru.ppr.core.dataCarrier.smartCard.coverageArea;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT1.CoverageAreaT1Decoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT2.CoverageAreaT2Decoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT3.CoverageAreaT3Decoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT4.CoverageAreaT4Decoder;

/**
 * Фабрика декодеров зоны действия, используемая по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultCoverageAreaDecoderFactory implements CoverageAreaDecoderFactory {

    @Inject
    DefaultCoverageAreaDecoderFactory(){

    }

    @NonNull
    @Override
    public CoverageAreaDecoder create(byte[] data) {
        int coverageAreaTypeCode = data[0];
        CoverageAreaType coverageAreaType = CoverageAreaType.getByCode(coverageAreaTypeCode);

        if (coverageAreaType == null) {
            return new DefaultCoverageAreaDecoder();
        }

        switch (coverageAreaType) {
            case ALL_AREA:
                return new CoverageAreaT1Decoder();
            case SINGLE_STATION:
                return new CoverageAreaT2Decoder();
            case SINGLE_DIRECTION:
                return new CoverageAreaT3Decoder();
            case SINGLE_PRODUCTION_SECTION:
                return new CoverageAreaT4Decoder();
            default:
                return new DefaultCoverageAreaDecoder();
        }
    }
}
