package ru.ppr.chit.ui.activity.readbsqrcode;

import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.domain.repository.local.AuthInfoRepository;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.AuthInfoReader;
import ru.ppr.core.dataCarrier.readbarcodetask.BarcodeNotReadException;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class ReadBsQrCodePresenter extends BaseMvpViewStatePresenter<ReadBsQrCodeView, ReadBsQrCodeViewState> {

    private static final String TAG = Logger.makeLogTag(ReadBsQrCodePresenter.class);

    private static final int TIMER_VALUE = 4;

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final AuthInfoReader authInfoReader;
    private final AuthInfoRepository authInfoRepository;
    private final UiThread uiThread;
    //endregion
    //region Other
    private Navigator navigator;
    private Disposable timerDisposable = Disposables.disposed();
    private Disposable readBarcodeDisposable = Disposables.disposed();
    private final AtomicBoolean timerTaskIsRunning = new AtomicBoolean();
    private final AtomicBoolean readBarcodeTaskIsRunning = new AtomicBoolean();

    @Inject
    ReadBsQrCodePresenter(ReadBsQrCodeViewState readBsQrCodeViewState,
                          AuthInfoReader authInfoReader,
                          AuthInfoRepository authInfoRepository,
                          UiThread uiThread) {
        super(readBsQrCodeViewState);
        this.authInfoReader = authInfoReader;
        this.authInfoRepository = authInfoRepository;
        this.uiThread = uiThread;
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
        navigator.navigateBack(null);
    }

    private void startReadBarcode() {
        Logger.trace(TAG, "startReadBarcode");
        if (readBarcodeTaskIsRunning.getAndSet(true)) {
            throw new IllegalStateException("Operation is already running");
        }
        readBarcodeDisposable = Completable
                .fromAction(() -> {
                    view.setState(ReadBsQrCodeView.State.SEARCH_BARCODE);
                    startTimer();
                })
                .observeOn(AppSchedulers.background())
                .andThen(authInfoReader.readAuthInfo())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(authInfo -> {
                    stopTimer();
                    view.setState(ReadBsQrCodeView.State.PROCESSING_DATA);
                })
                .observeOn(AppSchedulers.background())
                .doOnSuccess(authInfoRepository::insert)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> readBarcodeTaskIsRunning.set(false))
                .subscribe(authInfo -> navigator.navigateBack(authInfo.getId()),
                        throwable -> {
                            Logger.error(TAG, throwable);
                            if (throwable instanceof BarcodeNotReadException) {
                                view.setState(ReadBsQrCodeView.State.SEARCH_BARCODE_ERROR);
                            } else {
                                view.setState(ReadBsQrCodeView.State.UNKNOWN_ERROR);
                            }
                        });
    }

    private void stopReadBarcode() {
        Logger.warning(TAG, "stopReadBarcode");
        readBarcodeDisposable.dispose();
    }

    private void startTimer() {
        Logger.trace(TAG, "startTimer");
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
                            view.setState(ReadBsQrCodeView.State.SEARCH_BARCODE_ERROR);
                        }
                );
    }

    private void stopTimer() {
        Logger.trace(TAG, "stopTimer");
        timerDisposable.dispose();
    }

    void onScreenClosed() {
        Logger.trace(TAG, "onScreenClosed");
        if (readBarcodeTaskIsRunning.get()) {
            stopReadBarcode();
            stopTimer();
            uiThread.post(() -> view.setState(ReadBsQrCodeView.State.UNKNOWN_ERROR));
        }
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void destroy() {
        readBarcodeDisposable.dispose();
        super.destroy();
    }

    interface Navigator {

        void navigateBack(@Nullable Long authInfoId);

    }

}
