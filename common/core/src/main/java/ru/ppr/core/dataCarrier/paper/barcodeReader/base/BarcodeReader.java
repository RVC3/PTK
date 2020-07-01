package ru.ppr.core.dataCarrier.paper.barcodeReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.paper.barcodeReader.ReadBarcodeResult;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Ридер ШК.
 *
 * @author Aleksandr Brazhkin
 */
public interface BarcodeReader {

    @NonNull
    ReadBarcodeResult<byte[]> readData();

}
