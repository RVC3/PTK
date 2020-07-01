package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4Decoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5Decoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8Decoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v7.PassageMarkV7Decoder;

/**
 * Фабрика декодеров метки прохода, используемая по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultPassageMarkDecoderFactory implements PassageMarkDecoderFactory {

    private static final int VERSION_INDEX = 0;

    @Inject
    public DefaultPassageMarkDecoderFactory() {

    }

    @NonNull
    @Override
    public PassageMarkDecoder create(@NonNull byte[] data) {
        PassageMarkVersion version = PassageMarkVersion.getByCode(data[VERSION_INDEX]);

        if (version == null){
            return new DefaultPassageMarkDecoder();
        }

        switch (version) {
            case V4:
                return new PassageMarkV4Decoder();
            case V5:
                return new PassageMarkV5Decoder();
            case V7:
                return new PassageMarkV7Decoder();
            case V8:
                return new PassageMarkV8Decoder();
            default:
                return new DefaultPassageMarkDecoder();
        }
    }
}
