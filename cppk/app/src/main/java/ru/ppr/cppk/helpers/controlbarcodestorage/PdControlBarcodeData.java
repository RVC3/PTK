package ru.ppr.cppk.helpers.controlbarcodestorage;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Данные, считанные с карты при контроле штрихкода с ПД.
 *
 * @author Dmitry Vinogradov
 */
public class PdControlBarcodeData {

    private Pd pd;

    public PdControlBarcodeData() {

    }

    @NonNull
    public Pd getPd() {
        return pd;
    }

    public void setPd(Pd pd) {
        this.pd = pd;
    }
}
