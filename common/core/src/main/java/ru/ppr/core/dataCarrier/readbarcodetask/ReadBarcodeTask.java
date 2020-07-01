package ru.ppr.core.dataCarrier.readbarcodetask;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicReference;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.core.dataCarrier.coupon.CouponDecoder;
import ru.ppr.core.dataCarrier.coupon.base.Coupon;
import ru.ppr.core.dataCarrier.paper.barcodeReader.BarcodeReaderImpl;
import ru.ppr.core.dataCarrier.paper.barcodeReader.CouponBarcodeReaderImpl;
import ru.ppr.core.dataCarrier.paper.barcodeReader.PdBarcodeReaderImpl;
import ru.ppr.core.dataCarrier.paper.barcodeReader.ReadBarcodeResult;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.BarcodeReader;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.CouponBarcodeDetector;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.PdBarcodeDetector;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Команда чтения данных из ШК
 *
 * @author Dmitry Nevolin
 */
public class ReadBarcodeTask {

    private static final String TAG = Logger.makeLogTag(ReadBarcodeTask.class);

    /**
     * Флаг, что команда отменена извне.
     */
    private volatile boolean canceled;
    /**
     * Поток, выполняющий задачу
     */
    private final AtomicReference<Thread> runner = new AtomicReference<>();
    private final IBarcodeReader barcodeReaderDevice;
    private final CouponDecoder couponDecoder;
    private final CouponBarcodeDetector couponBarcodeDetector;
    private final PdDecoderFactory pdDecoderFactory;
    private final PdBarcodeDetector pdBarcodeDetector;

    public ReadBarcodeTask(@NonNull IBarcodeReader barcodeReaderDevice,
                           @NonNull CouponDecoder couponDecoder,
                           @NonNull CouponBarcodeDetector couponBarcodeDetector,
                           @NonNull PdDecoderFactory pdDecoderFactory,
                           @NonNull PdBarcodeDetector pdBarcodeDetector) {
        this.barcodeReaderDevice = barcodeReaderDevice;
        this.couponDecoder = couponDecoder;
        this.couponBarcodeDetector = couponBarcodeDetector;
        this.pdDecoderFactory = pdDecoderFactory;
        this.pdBarcodeDetector = pdBarcodeDetector;
    }

    /**
     * Выполняет чтение ШК
     */
    @Nullable
    public BarcodeReader read() {
        return readAndResetState();
    }

    @Nullable
    private BarcodeReader readAndResetState() {
        try {
            if (!runner.compareAndSet(null, Thread.currentThread())) {
                Logger.error(TAG, "Task is already running");
                return null;
            }
            return readInternal();
        } finally {
            canceled = false;
            runner.set(null);
        }
    }

    @Nullable
    private BarcodeReader readInternal() {
        Logger.trace(TAG, "read started");

        BarcodeReader barcodeReader = new BarcodeReaderImpl(barcodeReaderDevice);

        while (!isCanceled()) {

            ReadBarcodeResult<byte[]> data = barcodeReader.readData();

            if (!data.isSuccess()) {
                Logger.error(TAG, "Could not read barcode data: " + data.getDescription());
                continue;
            }

            if (isCanceled()) {
                Logger.error(TAG, "Task was canceled");
                return null;
            }

            byte[] rawData = data.getData();

            if (couponBarcodeDetector.isCoupon(rawData)) {
                CouponBarcodeReaderImpl couponBarcodeReader = new CouponBarcodeReaderImpl(barcodeReader, couponDecoder);
                ReadBarcodeResult<Coupon> couponData = couponBarcodeReader.readCoupon();

                if (couponData.isSuccess()) {
                    Logger.trace(TAG, "CouponBarcodeReaderImpl created");
                    return couponBarcodeReader;
                } else {
                    Logger.error(TAG, "Could not read coupon data: " + CommonUtils.bytesToHexWithoutSpaces(rawData));
                    return null;
                }
            } else if (pdBarcodeDetector.isPd(rawData)) {
                PdBarcodeReaderImpl pdBarcodeReader = new PdBarcodeReaderImpl(barcodeReader, pdDecoderFactory);
                ReadBarcodeResult<Pd> pdData = pdBarcodeReader.readPd();

                if (pdData.isSuccess()) {
                    Logger.trace(TAG, "PdBarcodeReaderImpl created");
                    return pdBarcodeReader;
                } else {
                    Logger.error(TAG, "Could not read pd data: " + CommonUtils.bytesToHexWithoutSpaces(rawData));
                    return null;
                }
            } else {
                Logger.warning(TAG, "Reader is not implemented for barcode data: " + CommonUtils.bytesToHexWithoutSpaces(rawData));
                return barcodeReader;
            }
        }
        Logger.error(TAG, "Task was canceled");
        return null;
    }

    /**
     * Проверяет, прервана ли комада чтения ШК извне.
     *
     * @return {@code true}, если прервана, {@code false} иначе
     */
    private boolean isCanceled() {
        return canceled || Thread.currentThread().isInterrupted();
    }

    /**
     * Прерывает команду чтения ШК.
     */
    public void cancel() {
        Logger.trace(TAG, "cancel");
        canceled = true;
        Thread thread = runner.get();
        if (thread != null) {
            thread.interrupt();
        }
    }

}
