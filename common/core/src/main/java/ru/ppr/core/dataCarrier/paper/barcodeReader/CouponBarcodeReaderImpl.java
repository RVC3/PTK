package ru.ppr.core.dataCarrier.paper.barcodeReader;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.coupon.CouponDecoder;
import ru.ppr.core.dataCarrier.coupon.base.Coupon;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.BarcodeReader;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.CouponBarcodeReader;

/**
 * Реализация по-умолчанию для {@link CouponBarcodeReader}
 *
 * @author Dmitry Nevolin
 */
public class CouponBarcodeReaderImpl implements CouponBarcodeReader {

    private final BarcodeReader barcodeReader;
    private final CouponDecoder couponDecoder;
    private Coupon cachedCoupon;

    public CouponBarcodeReaderImpl(@NonNull BarcodeReader barcodeReader, @NonNull CouponDecoder couponDecoder) {
        this.barcodeReader = barcodeReader;
        this.couponDecoder = couponDecoder;
    }

    @NonNull
    @Override
    public ReadBarcodeResult<Coupon> readCoupon() {
        if (cachedCoupon != null) {
            return new ReadBarcodeResult<>(cachedCoupon);
        }

        ReadBarcodeResult<byte[]> rawResult = readData();
        ReadBarcodeResult<Coupon> result;

        if (rawResult.isSuccess()) {
            cachedCoupon = couponDecoder.decode(rawResult.getData());

            if (cachedCoupon != null) {
                result = new ReadBarcodeResult<>(cachedCoupon);
            } else {
                result = new ReadBarcodeResult<>(ReadBarcodeErrorType.PD_IS_NOT_PARSED);
            }
        } else {
            result = new ReadBarcodeResult<>(rawResult.getReadBarcodeErrorType(), rawResult.getDescription());
        }

        return result;
    }

    @NonNull
    @Override
    public ReadBarcodeResult<byte[]> readData() {
        return barcodeReader.readData();
    }

}
