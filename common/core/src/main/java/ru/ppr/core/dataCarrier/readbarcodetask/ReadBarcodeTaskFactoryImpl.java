package ru.ppr.core.dataCarrier.readbarcodetask;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.core.dataCarrier.coupon.CouponDecoder;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.CouponBarcodeDetector;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.PdBarcodeDetector;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;

/**
 * Фабрика {@link ReadBarcodeTask}.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadBarcodeTaskFactoryImpl implements ReadBarcodeTaskFactory {

    private final IBarcodeReader barcodeReader;
    private final CouponDecoder couponDecoder;
    private final CouponBarcodeDetector couponBarcodeDetector;
    private final PdDecoderFactory pdDecoderFactory;
    private final PdBarcodeDetector pdBarcodeDetector;

    @Inject
    ReadBarcodeTaskFactoryImpl(@NonNull IBarcodeReader barcodeReader,
                               @NonNull CouponDecoder couponDecoder,
                               @NonNull CouponBarcodeDetector couponBarcodeDetector,
                               @NonNull PdDecoderFactory pdDecoderFactory,
                               @NonNull PdBarcodeDetector pdBarcodeDetector) {
        this.barcodeReader = barcodeReader;
        this.couponDecoder = couponDecoder;
        this.couponBarcodeDetector = couponBarcodeDetector;
        this.pdDecoderFactory = pdDecoderFactory;
        this.pdBarcodeDetector = pdBarcodeDetector;
    }

    @Override
    public ReadBarcodeTask create() {
        return new ReadBarcodeTask(
                barcodeReader,
                couponDecoder,
                couponBarcodeDetector,
                pdDecoderFactory,
                pdBarcodeDetector
        );
    }
}
