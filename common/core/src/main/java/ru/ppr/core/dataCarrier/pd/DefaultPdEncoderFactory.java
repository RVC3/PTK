package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.v1.PdV1Encoder;
import ru.ppr.core.dataCarrier.pd.v10.PdV10Encoder;
import ru.ppr.core.dataCarrier.pd.v11.PdV11Encoder;
import ru.ppr.core.dataCarrier.pd.v12.PdV12Encoder;
import ru.ppr.core.dataCarrier.pd.v13.PdV13Encoder;
import ru.ppr.core.dataCarrier.pd.v14.PdV14Encoder;
import ru.ppr.core.dataCarrier.pd.v15.PdV15Encoder;
import ru.ppr.core.dataCarrier.pd.v16.PdV16Encoder;
import ru.ppr.core.dataCarrier.pd.v17.PdV17Encoder;
import ru.ppr.core.dataCarrier.pd.v18.PdV18Encoder;
import ru.ppr.core.dataCarrier.pd.v19.PdV19Encoder;
import ru.ppr.core.dataCarrier.pd.v2.PdV2Encoder;
import ru.ppr.core.dataCarrier.pd.v20.PdV20Encoder;
import ru.ppr.core.dataCarrier.pd.v21.PdV21Encoder;
import ru.ppr.core.dataCarrier.pd.v22.PdV22Encoder;
import ru.ppr.core.dataCarrier.pd.v23.PdV23Encoder;
import ru.ppr.core.dataCarrier.pd.v24.PdV24Encoder;
import ru.ppr.core.dataCarrier.pd.v25.PdV25Encoder;
import ru.ppr.core.dataCarrier.pd.v3.PdV3Encoder;
import ru.ppr.core.dataCarrier.pd.v4.PdV4Encoder;
import ru.ppr.core.dataCarrier.pd.v5.PdV5Encoder;
import ru.ppr.core.dataCarrier.pd.v6.PdV6Encoder;
import ru.ppr.core.dataCarrier.pd.v64.PdV64Encoder;
import ru.ppr.core.dataCarrier.pd.v7.PdV7Encoder;
import ru.ppr.core.dataCarrier.pd.v9.PdV9Encoder;

/**
 * Фабрика кодеров ПД, используемая по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultPdEncoderFactory implements PdEncoderFactory {

    @Inject
    public DefaultPdEncoderFactory() {

    }

    @NonNull
    @Override
    public PdEncoder create(@NonNull Pd pd) {
        PdVersion version = pd.getVersion();
        switch (version) {
            case V1:
                return new PdV1Encoder();
            case V2:
                return new PdV2Encoder();
            case V3:
                return new PdV3Encoder();
            case V4:
                return new PdV4Encoder();
            case V5:
                return new PdV5Encoder();
            case V6:
                return new PdV6Encoder();
            case V7:
                return new PdV7Encoder();
            case V8:
                return new DefaultPdEncoder();
            case V9:
                return new PdV9Encoder();
            case V10:
                return new PdV10Encoder();
            case V11:
                return new PdV11Encoder();
            case V12:
                return new PdV12Encoder();
            case V13:
                return new PdV13Encoder();
            case V14:
                return new PdV14Encoder();
            case V15:
                return new PdV15Encoder();
            case V16:
                return new PdV16Encoder();
            case V17:
                return new PdV17Encoder();
            case V18:
                return new PdV18Encoder();
            case V19:
                return new PdV19Encoder();
            case V20:
                return new PdV20Encoder();
            case V21:
                return new PdV21Encoder();
            case V22:
                return new PdV22Encoder();
            case V23:
                return new PdV23Encoder();
            case V24:
                return new PdV24Encoder();
            case V25:
                return new PdV25Encoder();
            case V64:
                return new PdV64Encoder();
            default:
                return new DefaultPdEncoder();
        }
    }
}
