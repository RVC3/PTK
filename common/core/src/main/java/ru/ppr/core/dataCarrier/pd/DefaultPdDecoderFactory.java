package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.v1.PdV1Decoder;
import ru.ppr.core.dataCarrier.pd.v10.PdV10Decoder;
import ru.ppr.core.dataCarrier.pd.v11.PdV11Decoder;
import ru.ppr.core.dataCarrier.pd.v12.PdV12Decoder;
import ru.ppr.core.dataCarrier.pd.v13.PdV13Decoder;
import ru.ppr.core.dataCarrier.pd.v14.PdV14Decoder;
import ru.ppr.core.dataCarrier.pd.v15.PdV15Decoder;
import ru.ppr.core.dataCarrier.pd.v16.PdV16Decoder;
import ru.ppr.core.dataCarrier.pd.v17.PdV17Decoder;
import ru.ppr.core.dataCarrier.pd.v18.PdV18Decoder;
import ru.ppr.core.dataCarrier.pd.v19.PdV19Decoder;
import ru.ppr.core.dataCarrier.pd.v2.PdV2Decoder;
import ru.ppr.core.dataCarrier.pd.v20.PdV20Decoder;
import ru.ppr.core.dataCarrier.pd.v21.PdV21Decoder;
import ru.ppr.core.dataCarrier.pd.v22.PdV22Decoder;
import ru.ppr.core.dataCarrier.pd.v23.PdV23Decoder;
import ru.ppr.core.dataCarrier.pd.v24.PdV24Decoder;
import ru.ppr.core.dataCarrier.pd.v25.PdV25Decoder;
import ru.ppr.core.dataCarrier.pd.v3.PdV3Decoder;
import ru.ppr.core.dataCarrier.pd.v4.PdV4Decoder;
import ru.ppr.core.dataCarrier.pd.v5.PdV5Decoder;
import ru.ppr.core.dataCarrier.pd.v6.PdV6Decoder;
import ru.ppr.core.dataCarrier.pd.v64.PdV64Decoder;
import ru.ppr.core.dataCarrier.pd.v69.PdV69Decoder;
import ru.ppr.core.dataCarrier.pd.v7.PdV7Decoder;
import ru.ppr.core.dataCarrier.pd.v9.PdV9Decoder;

/**
 * Фабрика декодеров ПД, используемая по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultPdDecoderFactory implements PdDecoderFactory {

    private final PdVersionDetector pdVersionDetector;

    @Inject
    public DefaultPdDecoderFactory(PdVersionDetector pdVersionDetector) {
        this.pdVersionDetector = pdVersionDetector;
    }

    @NonNull
    @Override
    public PdDecoder create(@NonNull byte[] data) {
        PdVersion version = pdVersionDetector.getVersion(data);

        if (version == null) {
            return new DefaultPdDecoder();
        }

        switch (version) {
            case V1:
                return new PdV1Decoder();
            case V2:
                return new PdV2Decoder();
            case V3:
                return new PdV3Decoder();
            case V4:
                return new PdV4Decoder();
            case V5:
                return new PdV5Decoder();
            case V6:
                return new PdV6Decoder();
            case V7:
                return new PdV7Decoder();
            case V8:
                return new DefaultPdDecoder();
            case V9:
                return new PdV9Decoder();
            case V10:
                return new PdV10Decoder();
            case V11:
                return new PdV11Decoder();
            case V12:
                return new PdV12Decoder();
            case V13:
                return new PdV13Decoder();
            case V14:
                return new PdV14Decoder();
            case V15:
                return new PdV15Decoder();
            case V16:
                return new PdV16Decoder();
            case V17:
                return new PdV17Decoder();
            case V18:
                return new PdV18Decoder();
            case V19:
                return new PdV19Decoder();
            case V20:
                return new PdV20Decoder();
            case V21:
                return new PdV21Decoder();
            case V22:
                return new PdV22Decoder();
            case V23:
                return new PdV23Decoder();
            case V24:
                return new PdV24Decoder();
            case V25:
                return new PdV25Decoder();
            case V64:
                return new PdV64Decoder();
            default:
                return new DefaultPdDecoder();
        }
    }
}
