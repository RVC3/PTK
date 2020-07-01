package ru.ppr.barcode;

import android.support.annotation.Nullable;

/**
 * Считыватель ШК.
 *
 * @author Artem Ushakov
 */
public interface IBarcodeReader {

    /**
     * Получает данные со сканера ШК
     */
    @Nullable
    byte[] scan();

    boolean open();

    /**
     * Закрывает порт сканирования ШК
     */
    void close();

    /**
     * @return
     */
    boolean getFirmwareVersion(StringBuilder stringBuilder);

    /**
     * Вернет модель устройства
     *
     * @param model
     * @return
     */
    boolean getModel(String[] model);

}
