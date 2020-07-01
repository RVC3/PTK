package ru.ppr.core.dataCarrier.paper.barcodeReader;

import android.support.annotation.NonNull;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.BarcodeReader;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Реализация по-умолчанию для {@link BarcodeReader}
 *
 * @author Dmitry Nevolin
 */
public class BarcodeReaderImpl implements BarcodeReader {

    private static final String TAG = Logger.makeLogTag(BarcodeReaderImpl.class);

    private final IBarcodeReader barcodeReaderDevice;
    private byte[] cachedData;

    public BarcodeReaderImpl(IBarcodeReader barcodeReaderDevice) {
        this.barcodeReaderDevice = barcodeReaderDevice;
    }

    @NonNull
    @Override
    public ReadBarcodeResult<byte[]> readData() {
        Logger.trace(TAG, "readData START");

        if (cachedData != null) {
            Logger.trace(TAG, "readData END (from cache)");

            return new ReadBarcodeResult<>(cachedData);
        }

        cachedData = barcodeReaderDevice.scan();

        ReadBarcodeResult<byte[]> result;

        if (cachedData != null) {
            Logger.trace(TAG, "readData OK - " + CommonUtils.bytesToHexWithoutSpaces(cachedData));

            result = new ReadBarcodeResult<>(cachedData);
        } else {
            result = new ReadBarcodeResult<>(ReadBarcodeErrorType.OTHER);
        }

        Logger.trace(TAG, "readData END");

        return result;
    }

}
