package ru.ppr.core.dataCarrier.paper.barcodeReader.base;

/**
 * Детектер признака ПД
 *
 * @author Dmitry Nevolin
 */
public interface PdBarcodeDetector {

    /**
     * Проверяет ПД ли данные
     *
     * @param data данные
     * @return true если данные ПД, false в противном случае
     */
    boolean isPd(byte[] data);

}
