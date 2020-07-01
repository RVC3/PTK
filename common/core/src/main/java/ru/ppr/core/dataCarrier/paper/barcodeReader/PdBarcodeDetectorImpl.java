package ru.ppr.core.dataCarrier.paper.barcodeReader;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.paper.barcodeReader.base.PdBarcodeDetector;

/**
 * Реализация по-умолчанию для {@link PdBarcodeDetector}
 *
 * @author Dmitry Nevolin
 */
public class PdBarcodeDetectorImpl implements PdBarcodeDetector {

    /**
     * см. {@link ru.ppr.core.dataCarrier.pd.v1.PdV1Decoder}
     */
    private static final int PD_V1_SIZE = 82;
    /**
     * см. {@link ru.ppr.core.dataCarrier.pd.v2.PdV2Decoder}
     */
    private static final int PD_V2_SIZE = 91;
    /**
     * см. {@link ru.ppr.core.dataCarrier.pd.v22.PdV22Decoder}
     */
    private static final int PD_V22_SIZE = 88;
    /**
     * см. {@link ru.ppr.core.dataCarrier.pd.v9.PdV9Decoder}
     */
    private static final int PD_V9_SIZE = 124;

    @Inject
    PdBarcodeDetectorImpl() {
    }

    @Override
    public boolean isPd(byte[] data) {
        return data.length == PD_V1_SIZE || data.length == PD_V2_SIZE || data.length == PD_V22_SIZE || data.length == PD_V9_SIZE;
    }

}
