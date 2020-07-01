package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4Encoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5Encoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v7.PassageMarkV7Encoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8Encoder;

/**
 * Фабрика кодеров метки проход, используемая по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultPassageMarkEncoderFactory implements PassageMarkEncoderFactory {

    @Inject
    public DefaultPassageMarkEncoderFactory() {

    }

    @NonNull
    @Override
    public PassageMarkEncoder create(PassageMark passageMark) {
        PassageMarkVersion version = passageMark.getVersion();
        switch (version) {
            case V4:
                return new PassageMarkV4Encoder();
            case V5:
                return new PassageMarkV5Encoder();
            case V6:
                return new DefaultPassageMarkEncoder();
            case V7:
                return new PassageMarkV7Encoder();
            case V8:
                return new PassageMarkV8Encoder();
            default:
                return new DefaultPassageMarkEncoder();
        }
    }
}
