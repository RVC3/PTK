package ru.ppr.chit.ui.activity.readbarcode;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import ru.ppr.chit.barcode.ReadBarcodeInteractor;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.PdWithPlaceBarcodeStorage;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.chit.ui.activity.readbsc.ReadBscView;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.BarcodeReader;
import ru.ppr.core.dataCarrier.paper.barcodeReader.base.PdBarcodeReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPlace;
import ru.ppr.core.dataCarrier.readbarcodetask.BarcodeNotReadException;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Grigoriy Kashka
 */
class ReadBarcodePresenter extends BaseMvpViewStatePresenter<ReadBarcodeView, ReadBarcodeViewState> {

    private static final String TAG = Logger.makeLogTag(ReadBarcodePresenter.class);

    private static final int TIMER_VALUE = 4;

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final UiThread uiThread;
    private final ReadBarcodeInteractor readBarcodeInteractor;
    private final PdWithPlaceBarcodeStorage pdWithPlaceBarcodeStorage;
    //endregion
    //region Other
    private ReadBarcodePresenter.Navigator navigator;
    private Disposable timerDisposable = Disposables.disposed();
    private Disposable readBarcodeDisposable = Disposables.disposed();
    private final AtomicBoolean timerTaskIsRunning = new AtomicBoolean();
    private final AtomicBoolean readBarcodeTaskIsRunning = new AtomicBoolean();
    /**
     * Флаг, что информация считана и осуществлен переход на другой экран
     */
    private boolean barcodeResultRead = false;
    //endregion

    @Inject
    ReadBarcodePresenter(ReadBarcodeViewState readBarcodeViewState,
                         ReadBarcodeInteractor readBarcodeInteractor,
                         UiThread uiThread,
                         PdWithPlaceBarcodeStorage pdWithPlaceBarcodeStorage) {
        super(readBarcodeViewState);
        this.readBarcodeInteractor = readBarcodeInteractor;
        this.uiThread = uiThread;
        this.pdWithPlaceBarcodeStorage = pdWithPlaceBarcodeStorage;
    }

    public void setNavigator(ReadBarcodePresenter.Navigator navigator) {
        this.navigator = navigator;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        startReadBarcode();
    }

    void onRepeatBtnClicked() {
        Logger.trace(TAG, "onRepeatBtnClicked");
        startReadBarcode();
    }

    void onCancelBtnClicked() {
        Logger.trace(TAG, "onCancelBtnClicked");
        navigator.navigateBack();
    }

    private void startReadBarcode() {
        Logger.trace(TAG, "startReadBarcode()");
        if (barcodeResultRead) {
            Logger.trace(TAG, "Result is already read, skip reading");
            return;
        }
        if (readBarcodeTaskIsRunning.getAndSet(true)) {
            throw new IllegalStateException("Operation is already running");
        }
        readBarcodeDisposable = Completable
                .fromAction(() -> {
                    pdWithPlaceBarcodeStorage.clearData();
                    view.setState(ReadBarcodeView.State.SEARCH_BARCODE);
                    startTimer();
                })
                .observeOn(AppSchedulers.background())
                .andThen(readBarcodeInteractor.readBarcode())
                .doOnError(throwable -> stopTimer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(barcodeReader -> {
                    stopTimer();
                    view.setState(ReadBarcodeView.State.PROCESSING_DATA);
                })
                .observeOn(AppSchedulers.background())
                .flatMap(barcodeReader -> Single.fromCallable(() -> readBarcode(barcodeReader)))
                .doOnSuccess(this::processData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnDispose(() -> Logger.trace(TAG, "startReadBarcode() doOnDispose()"))
                .doFinally(() -> {
                    Logger.trace(TAG, "startReadBarcode() doFinally()");
                    readBarcodeTaskIsRunning.set(false);
                })
                .subscribe(this::onBarcodeResultRead, throwable -> {
                    Logger.error(TAG, throwable);
                    if (throwable instanceof BarcodeNotReadException) {
                        view.setState(ReadBarcodeView.State.SEARCH_BARCODE_ERROR);
                    } else {
                        view.setState(ReadBarcodeView.State.UNKNOWN_ERROR);
                    }
                });
    }

    private void stopReadBarcode() {
        Logger.trace(TAG, "stopReadBarcode()");
        readBarcodeDisposable.dispose();
    }

    @NonNull
    private ReadBarcodeResult readBarcode(BarcodeReader barcodeReader) {
        Logger.trace(TAG, "readBarcode, barcodeReader = " + barcodeReader);
        ReadBarcodeResult readBarcodeResult = new ReadBarcodeResult();
        if (barcodeReader != null && barcodeReader instanceof PdBarcodeReader) {
            ru.ppr.core.dataCarrier.paper.barcodeReader.ReadBarcodeResult<Pd> res = ((PdBarcodeReader) barcodeReader).readPd();
            if (res.isSuccess()) {
                if (res.getData() instanceof PdWithPlace) {
                    Logger.trace(TAG, "Pd with place read from barcode");
                    readBarcodeResult.pdWithPlace = (PdWithPlace) res.getData();
                    readBarcodeResult.pdWithPlaceRead = true;
                }
            }
        }
        return readBarcodeResult;
    }

    private void processData(@NonNull ReadBarcodeResult readBarcodeResult) {
        if (readBarcodeResult.pdWithPlaceRead) {
            pdWithPlaceBarcodeStorage.putData(readBarcodeResult.pdWithPlace);
        }
    }

    private void onBarcodeResultRead(ReadBarcodeResult readBarcodeResult) {
        Logger.trace(TAG, "onBarcodeResultRead");
        if (readBarcodeResult.pdWithPlaceRead) {
            barcodeResultRead = true;
            navigator.navigateToTicketControl();
        } else {
            view.setState(ReadBarcodeView.State.UNKNOWN_ERROR);
        }
    }

    private void startTimer() {
        Logger.trace(TAG, "startTimer()");
        if (timerTaskIsRunning.getAndSet(true)) {
            throw new IllegalStateException("Operation is already running");
        }
        timerDisposable = Observable
                .interval(0, 1, TimeUnit.SECONDS, AppSchedulers.background())
                .take(TIMER_VALUE + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> timerTaskIsRunning.set(false))
                .subscribe(
                        value -> view.setTimerValue((int) (TIMER_VALUE - value)),
                        throwable -> Logger.error(TAG, throwable),
                        () -> {
                            stopReadBarcode();
                            view.setState(ReadBarcodeView.State.SEARCH_BARCODE_ERROR);
                        }
                );
    }

    private void stopTimer() {
        Logger.trace(TAG, "stopTimer");
        timerDisposable.dispose();
    }

    @Override
    public void destroy() {
        super.destroy();
        timerDisposable.dispose();
        readBarcodeDisposable.dispose();
    }

    void onScreenClosed() {
        Logger.trace(TAG, "onScreenClosed");
        if (readBarcodeTaskIsRunning.get()) {
            stopReadBarcode();
            stopTimer();
            uiThread.post(() -> view.setState(ReadBarcodeView.State.UNKNOWN_ERROR));
        }
    }

    private static class ReadBarcodeResult {
        private boolean pdWithPlaceRead;
        private PdWithPlace pdWithPlace;
    }

    interface Navigator {
        void navigateToTicketControl();

        void navigateBack();
    }
}
