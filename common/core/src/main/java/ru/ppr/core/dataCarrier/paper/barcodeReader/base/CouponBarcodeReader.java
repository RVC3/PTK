package ru.ppr.core.dataCarrier.paper.barcodeReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.coupon.base.Coupon;
import ru.ppr.core.dataCarrier.paper.barcodeReader.ReadBarcodeResult;

/**
 * Ридер талонов ТППД
 *
 * @author Dmitry Nevolin
 */
public interface CouponBarcodeReader extends BarcodeReader {

    /**
     * Считывает талон со штрихкода
     */
    @NonNull
    ReadBarcodeResult<Coupon> readCoupon();

}
