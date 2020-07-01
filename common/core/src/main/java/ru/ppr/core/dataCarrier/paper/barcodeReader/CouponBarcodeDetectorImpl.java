package ru.ppr.core.dataCarrier.paper.barcodeReader;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.paper.barcodeReader.base.CouponBarcodeDetector;

/**
 * Реализация по-умолчанию для {@link CouponBarcodeDetector}
 *
 * @author Dmitry Nevolin
 */
public class CouponBarcodeDetectorImpl implements CouponBarcodeDetector {

    @Inject
    CouponBarcodeDetectorImpl() {
    }

    /**
     * см. {@link ru.ppr.core.dataCarrier.coupon.CouponDecoderImpl}
     */
    private static final int COUPON_SIZE = 8;

    @Override
    public boolean isCoupon(byte[] data) {
        return data.length == COUPON_SIZE;
    }

}
