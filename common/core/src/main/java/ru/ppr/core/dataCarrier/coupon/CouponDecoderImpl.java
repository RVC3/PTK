package ru.ppr.core.dataCarrier.coupon;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.coupon.base.Coupon;

/**
 * Реализация по-умолчанию для {@link CouponDecoder}
 *
 * @author Dmitry Nevolin
 */
public class CouponDecoderImpl implements CouponDecoder {

    private static final int COUPON_SIZE = 8;

    @Inject
    CouponDecoderImpl() {
    }

    @Nullable
    @Override
    public Coupon decode(@NonNull byte[] data) {
        if (data.length < COUPON_SIZE) {
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        long couponNumber = byteBuffer.getLong() & 0x1FFFFFFFFFFFFFL; // Маска на 53 бита, т.к. в соответствии со схемой размер номера имнно 53 бита

        CouponImpl coupon = new CouponImpl();
        coupon.setNumber(couponNumber);

        return coupon;
    }

}
