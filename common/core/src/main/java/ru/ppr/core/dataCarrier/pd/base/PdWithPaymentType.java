package ru.ppr.core.dataCarrier.pd.base;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ПД с типом оплаты.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithPaymentType extends Pd {

    /**
     * Тип оплаты
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PAYMENT_TYPE_CASH,
            PAYMENT_TYPE_CARD})
    @interface PaymentType {
    }

    int PAYMENT_TYPE_CASH = 1;
    int PAYMENT_TYPE_CARD = 2;

    /**
     * Возвращает тип оплаты.
     *
     * @return Тип оплаты
     */
    @PdWithPaymentType.PaymentType
    int getPaymentType();
}
