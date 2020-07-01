package ru.ppr.core.dataCarrier.paper.barcodeReader;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.paper.barcodeReader.base.BarcodeReader;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.PdBarcodeReader;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Реализация по-умолчанию для {@link PdBarcodeReader}
 *
 * @author Dmitry Nevolin
 */
public class PdBarcodeReaderImpl implements PdBarcodeReader {

    private final PdDecoderFactory pdDecoderFactory;
    private final BarcodeReader barcodeReader;
    private Pd cachedPd;

    public PdBarcodeReaderImpl(@NonNull BarcodeReader barcodeReader, @NonNull PdDecoderFactory pdDecoderFactory) {
        this.barcodeReader = barcodeReader;
        this.pdDecoderFactory = pdDecoderFactory;
    }

    @NonNull
    @Override
    public ReadBarcodeResult<Pd> readPd() {
        if (cachedPd != null) {
            return new ReadBarcodeResult<>(cachedPd);
        }

        ReadBarcodeResult<byte[]> rawResult = readData();
        ReadBarcodeResult<Pd> result;

        if (rawResult.isSuccess()) {
            PdDecoder pdDecoder = pdDecoderFactory.create(rawResult.getData());
            cachedPd = pdDecoder.decode(rawResult.getData());

            if (cachedPd != null) {
                result = new ReadBarcodeResult<>(cachedPd);
            } else {
                result = new ReadBarcodeResult<>(ReadBarcodeErrorType.PD_IS_NOT_PARSED);
            }
        } else {
            result = new ReadBarcodeResult<>(rawResult.getReadBarcodeErrorType(), rawResult.getDescription());
        }

        return result;
    }

    @NonNull
    @Override
    public ReadBarcodeResult<byte[]> readData() {
        return barcodeReader.readData();
    }

}
