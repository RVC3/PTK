package ru.ppr.core.dataCarrier.paper.barcodeReader.base;

/**
 * Детектер признака талона ТППД
 *
 * @author Dmitry Nevolin
 */
public interface CouponBarcodeDetector {

    /**
     * Проверяет талон ли данные
     *
     * @param data данные
     * @return true если данные талон, false в противном случае
     */
    boolean isCoupon(byte[] data);

}
