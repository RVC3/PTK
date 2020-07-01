package ru.ppr.core.dataCarrier.coupon;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.coupon.base.Coupon;

/**
 * Декодер талонов ТППД
 *
 * @author Dmitry Nevolin
 */
public interface CouponDecoder {

    /**
     * Декодирует талон
     *
     * @param data данные талона
     * @return талон
     */
    @Nullable
    Coupon decode(@NonNull byte[] data);

}
