package ru.ppr.core.dataCarrier.paper.barcodeReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.paper.barcodeReader.ReadBarcodeResult;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Ридер ПД
 *
 * @author Dmitry Nevolin
 */
public interface PdBarcodeReader extends BarcodeReader {

    /**
     * Считывает ПД со штрихкода.
     */
    @NonNull
    ReadBarcodeResult<Pd> readPd();

}
