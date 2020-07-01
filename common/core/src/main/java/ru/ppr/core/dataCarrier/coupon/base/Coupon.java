package ru.ppr.core.dataCarrier.coupon.base;

/**
 * Талон ТППД
 *
 * @author Dmitry Nevolin
 */
public interface Coupon {

    /**
     * Возвращает уникальный номер талона (16 цифр)
     */
    long getNumber();

}
