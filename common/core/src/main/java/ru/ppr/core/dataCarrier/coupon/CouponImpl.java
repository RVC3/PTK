package ru.ppr.core.dataCarrier.coupon;

import ru.ppr.core.dataCarrier.coupon.base.Coupon;

/**
 * Реализация по-умолчанию для {@link Coupon}
 *
 * @author Dmitry Nevolin
 */
public class CouponImpl implements Coupon {

    private long number;

    @Override
    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

}
