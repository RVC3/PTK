package ru.ppr.chit.barcode;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.BarcodeReader;
import ru.ppr.core.dataCarrier.readbarcodetask.BarcodeNotReadException;
import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTask;
import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTaskFactory;

/**
 * Операция чтения ШК.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadBarcodeInteractor {

    private final ReadBarcodeTaskFactory readBarcodeTaskFactory;

    @Inject
    ReadBarcodeInteractor(ReadBarcodeTaskFactory readBarcodeTaskFactory) {
        this.readBarcodeTaskFactory = readBarcodeTaskFactory;
    }

    /**
     * Выполняет чтение ШК
     */
    public Single<BarcodeReader> readBarcode() {
        return Single.create(e -> {
            ReadBarcodeTask readBarcodeTask = readBarcodeTaskFactory.create();
            e.setDisposable(new Disposable() {

                private boolean disposed;

                @Override
                public void dispose() {
                    readBarcodeTask.cancel();
                    disposed = true;
                }

                @Override
                public boolean isDisposed() {
                    return disposed;
                }
            });

            BarcodeReader barcodeReader = readBarcodeTask.read();

            if (barcodeReader == null) {
                if (!e.isDisposed()) {
                    e.onError(new BarcodeNotReadException());
                }
            } else {
                e.onSuccess(barcodeReader);
            }
        });
    }
}
