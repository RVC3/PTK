package ru.ppr.chit.ui.activity.readbsc;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.chit.helpers.readbscstorage.PdWithPlaceCardData;
import ru.ppr.chit.helpers.readbscstorage.PdWithPlaceCardDataStorage;
import ru.ppr.chit.rfid.FindCardInteractor;
import ru.ppr.core.dataCarrier.findcardtask.CardNotFoundException;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPlace;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReader;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.ReadCardInformationInteractor;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class ReadBscPresenter extends BaseMvpViewStatePresenter<ReadBscView, ReadBscViewState> {

    private static final String TAG = Logger.makeLogTag(ReadBscPresenter.class);

    private static final int TIMER_VALUE = 4;

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final FindCardInteractor findCardInteractor;
    private final UiThread uiThread;
    private final PdWithPlaceCardDataStorage pdWithPlaceCardDataStorage;
    private final ReadCardInformationInteractor readCardInformationInteractor;
    //endregion
    //region Other
    private Navigator navigator;
    private Disposable timerDisposable = Disposables.disposed();
    private Disposable readCardDisposable = Disposables.disposed();
    private final AtomicBoolean timerTaskIsRunning = new AtomicBoolean();
    private final AtomicBoolean readCardTaskIsRunning = new AtomicBoolean();
    /**
     * Флаг, что информация считана и осуществлен переход на другой экран
     */
    private boolean operationCompleted = false;
    //endregion

    @Inject
    ReadBscPresenter(ReadBscViewState readBscViewState,
                     FindCardInteractor findCardInteractor,
                     UiThread uiThread,
                     PdWithPlaceCardDataStorage pdWithPlaceCardDataStorage,
                     ReadCardInformationInteractor readCardInformationInteractor) {
        super(readBscViewState);
        this.findCardInteractor = findCardInteractor;
        this.uiThread = uiThread;
        this.pdWithPlaceCardDataStorage = pdWithPlaceCardDataStorage;
        this.readCardInformationInteractor = readCardInformationInteractor;
    }

    public void setNavigator(Navigator navigator) {
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
        startReadCard();
    }

    void onRepeatBtnClicked() {
        Logger.trace(TAG, "onRepeatBtnClicked");
        startReadCard();
    }

    void onCancelBtnClicked() {
        Logger.trace(TAG, "onCancelBtnClicked");
        navigator.navigateBack();
    }

    void onScreenClosed() {
        Logger.trace(TAG, "onScreenClosed");
        if (readCardTaskIsRunning.get()) {
            stopReadCard();
            stopTimer();
            uiThread.post(() -> view.setState(ReadBscView.State.UNKNOWN_ERROR));
        }
    }

    private void startReadCard() {
        Logger.trace(TAG, "startReadCard");
        if (operationCompleted) {
            Logger.trace(TAG, "Result is already read, skip reading");
            return;
        }
        if (readCardTaskIsRunning.getAndSet(true)) {
            throw new IllegalStateException("Operation is already running");
        }
        readCardDisposable = Completable
                .fromAction(() -> {
                    pdWithPlaceCardDataStorage.clearCardData();
                    view.setState(ReadBscView.State.SEARCH_CARD);
                    startTimer();
                })
                .observeOn(AppSchedulers.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .andThen(findCardInteractor.findCard())
                .doOnError(throwable -> stopTimer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    stopTimer();
                    view.setState(ReadBscView.State.READ_CARD);
                })
                .observeOn(AppSchedulers.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .flatMap(cardReader -> Single.fromCallable(() -> readCard(cardReader)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> view.setState(ReadBscView.State.PROCESSING_DATA))
                .observeOn(AppSchedulers.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .doOnSuccess(this::processData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> readCardTaskIsRunning.set(false))
                .subscribe(this::onBscResultRead, throwable -> {
                    Logger.error(TAG, throwable);
                    if (throwable instanceof CardNotFoundException) {
                        view.setState(ReadBscView.State.SEARCH_CARD_ERROR);
                    } else {
                        view.setState(ReadBscView.State.UNKNOWN_ERROR);
                    }
                });
    }

    private void stopReadCard() {
        Logger.trace(TAG, "stopReadCard");
        readCardDisposable.dispose();
    }

    @NonNull
    private ReadBscResult readCard(CardReader cardReader) {
        Logger.trace(TAG, "readCard, cardReader = " + cardReader);
        ReadBscResult readBscResult = new ReadBscResult();

        readBscResult.dataRead = true;

        if (cardReader instanceof CppkReader) {
            CppkReader cppkReader = (CppkReader) cardReader;

            ReadCardResult<CardInformation> cardInformationResult = readCardInformationInteractor.readCardInformation(cppkReader);
            if (cardInformationResult.isSuccess()) {
                readBscResult.cardInformation = cardInformationResult.getData();
            } else {
                readBscResult.dataRead = false;
            }

            ReadCardResult<List<Pd>> pdListResult = cppkReader.readPdList();
            if (pdListResult.isSuccess()) {
                List<Pd> pdList = pdListResult.getData();
                if (!pdList.isEmpty()) {
                    Pd pd = pdList.get(0);
                    if (pd instanceof PdWithPlace) {
                        Logger.trace(TAG, "Pd with place read");
                        readBscResult.pdWithPlace = (PdWithPlace) pd;
                    }
                }
            } else {
                readBscResult.dataRead = false;
                Logger.error(TAG, "Could not read pd list");
            }

            ReadCardResult<byte[]> edsResult = cppkReader.readEds();
            if (edsResult.isSuccess()) {
                readBscResult.eds = edsResult.getData();
            } else {
                readBscResult.dataRead = false;
            }
        } else {
            readBscResult.dataRead = false;
        }

        readBscResult.pdWithPlaceRead = readBscResult.dataRead && readBscResult.pdWithPlace != null;

        return readBscResult;
    }

    private void processData(@NonNull ReadBscResult readBscResult) {
        if (readBscResult.pdWithPlaceRead) {
            PdWithPlaceCardData pdWithPlaceCardData = new PdWithPlaceCardData();
            pdWithPlaceCardData.setPdWithPlace(readBscResult.pdWithPlace);
            pdWithPlaceCardData.setEds(readBscResult.eds);
            pdWithPlaceCardData.setCardInformation(readBscResult.cardInformation);
            pdWithPlaceCardDataStorage.putCardData(pdWithPlaceCardData);
        }
    }

    private void onBscResultRead(ReadBscResult readBscResult) {
        Logger.trace(TAG, "onBscResultRead");
        if (readBscResult.dataRead) {
            if (readBscResult.pdWithPlaceRead) {
                operationCompleted = true;
                navigator.navigateToTicketControl();
            } else {
                view.setState(ReadBscView.State.EMPTY_CARD);
            }
        } else {
            view.setState(ReadBscView.State.UNKNOWN_ERROR);
        }
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
                            stopReadCard();
                            view.setState(ReadBscView.State.SEARCH_CARD_ERROR);
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
        readCardDisposable.dispose();
    }

    private static class ReadBscResult {
        private boolean dataRead;
        private boolean pdWithPlaceRead;
        private PdWithPlace pdWithPlace;
        private byte[] eds;
        private CardInformation cardInformation;
    }

    interface Navigator {
        void navigateToTicketControl();

        void navigateBack();
    }
}
