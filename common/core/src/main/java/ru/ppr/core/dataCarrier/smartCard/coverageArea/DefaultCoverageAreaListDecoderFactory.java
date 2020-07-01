package ru.ppr.core.dataCarrier.smartCard.coverageArea;

import android.support.annotation.NonNull;

import javax.inject.Inject;

/**
 * Фабрика декодеров зоны действия, используемая по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultCoverageAreaListDecoderFactory implements CoverageAreaListDecoderFactory {

    private final CoverageAreaDecoderFactory coverageAreaDecoderFactory;

    @Inject
    public DefaultCoverageAreaListDecoderFactory(CoverageAreaDecoderFactory coverageAreaDecoderFactory) {
        this.coverageAreaDecoderFactory = coverageAreaDecoderFactory;
    }

    @NonNull
    @Override
    public CoverageAreaListDecoder create() {
        return new DefaultCoverageAreaListDecoder(coverageAreaDecoderFactory);
    }
}
