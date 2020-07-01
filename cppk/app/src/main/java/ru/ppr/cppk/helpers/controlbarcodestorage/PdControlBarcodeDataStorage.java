package ru.ppr.cppk.helpers.controlbarcodestorage;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory хранилище данных, считанных из штрихкода при контроле БСК.
 * Используется для передачи данных на экран с отображением информации.
 *
 * @author Dmitry Vinogradov
 */
@Singleton
public class PdControlBarcodeDataStorage {

    private PdControlBarcodeData pdControlBarcodeData;

    @Inject
    PdControlBarcodeDataStorage() {

    }

    public void putBarcodeData(PdControlBarcodeData pdControlCardData) {
        this.pdControlBarcodeData = pdControlCardData;
    }

    public void clearBarcodeData() {
        this.pdControlBarcodeData = null;
    }

    public PdControlBarcodeData getLastBarcodeData() {
        return pdControlBarcodeData;
    }
}
