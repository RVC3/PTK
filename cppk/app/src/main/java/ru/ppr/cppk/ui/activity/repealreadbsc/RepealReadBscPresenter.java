package ru.ppr.cppk.ui.activity.repealreadbsc;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ReadEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.interactor.FindCardInteractor;
import ru.ppr.cppk.logic.interactor.ReadLegacyBscInformationInteractor;
import ru.ppr.cppk.logic.interactor.ToLegacyPdListConverter;
import ru.ppr.cppk.logic.pd.PdHandler;
import ru.ppr.logger.Logger;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;


/**
 * @author Aleksandr Brazhkin
 */
class RepealReadBscPresenter extends BaseMvpViewStatePresenter<RepealReadBscView, RepealReadBscViewState> {

    private static final String TAG = Logger.makeLogTag(RepealReadBscPresenter.class);

    private static final int TIMER_VALUE = 4;

    // Common fields start
    private boolean initialized = false;
    // Common fields end
    private final FindCardInteractor findCardInteractor;
    private final ReadLegacyBscInformationInteractor readLegacyBscInformationInteractor;
    private final UiThread uiThread;
    private final PdHandler pdHandler;
    private Navigator navigator;
    private Subscription timerSubscription = Subscriptions.unsubscribed();
    private Subscription readCardSubscription = Subscriptions.unsubscribed();

    @Inject
    RepealReadBscPresenter(RepealReadBscViewState viewState,
                           FindCardInteractor findCardInteractor,
                           ReadLegacyBscInformationInteractor readLegacyBscInformationInteractor,
                           UiThread uiThread,
                           PdHandler pdHandler) {
        super(viewState);
        this.findCardInteractor = findCardInteractor;
        this.readLegacyBscInformationInteractor = readLegacyBscInformationInteractor;
        this.uiThread = uiThread;
        this.pdHandler = pdHandler;
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
        startReadCard();
    }

    void onRepeatBtnClicked() {
        startReadCard();
    }

    void onRfidBtnClicked() {
        if (readCardSubscription.isUnsubscribed()) {
            startReadCard();
        }
    }

    private void startReadCard() {
        if (!readCardSubscription.isUnsubscribed()) {
            throw new IllegalStateException("Operation is already running");
        }
        readCardSubscription = Completable
                .fromAction(() -> {
                    view.setState(RepealReadBscView.State.SEARCH_CARD);
                    startTimer();
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .andThen(findCardInteractor.findCard())
                .onErrorResumeNext(throwable -> Single.error(new CardNotFoundException()))
                .doOnError(throwable -> stopTimer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    stopTimer();
                    view.setState(RepealReadBscView.State.READ_CARD);
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .flatMap(cardReader -> Single.fromCallable(() -> readCard(cardReader)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(cardReader -> {
                    view.setState(RepealReadBscView.State.PROCESSING_DATA);
                })
                .observeOn(SchedulersCPPK.rfid())
                //.delay(2, TimeUnit.SECONDS)
                .flatMap(readBscResult -> Single.fromCallable(() -> {
                    if (handleResult(readBscResult)) {
                        return readBscResult;
                    } else {
                        throw new Exception();
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onBscResultRead, throwable -> {
                    Logger.error(TAG, throwable);
                    if (throwable instanceof CardNotFoundException) {
                        view.setState(RepealReadBscView.State.SEARCH_CARD_ERROR);
                    } else {
                        view.setState(RepealReadBscView.State.UNKNOWN_ERROR);
                    }
                });
    }

    private void stopReadCard() {
        readCardSubscription.unsubscribe();
    }

    @NonNull
    private ReadBscResult readCard(CardReader cardReader) {
        if (cardReader instanceof ReadPdReader) {
            return readCardWithPd((ReadPdReader) cardReader);
        } else {
            return new ReadBscResult();
        }
    }

    private ReadBscResult readCardWithPd(ReadPdReader cardReader) {
        ReadBscResult readBscResult = new ReadBscResult();

        readBscResult.legacyBscInformation = readLegacyBscInformationInteractor.read(cardReader);

        if (readBscResult.legacyBscInformation == null) {
            return readBscResult;
        }

        List<Pd> pdList = readPdList(cardReader);
        if (pdList == null) {
            return readBscResult;
        } else {
            List<PD> legacyPdList = new ToLegacyPdListConverter().convert(pdList, readBscResult.legacyBscInformation, null);
            readBscResult.legacyPdList = new ArrayList<>(legacyPdList);
        }

        if (!readEds(cardReader, readBscResult.legacyPdList)) {
            return readBscResult;
        }

        readBscResult.pdListRead = true;

        return readBscResult;
    }

    private List<Pd> readPdList(CardReader cardReader) {
        if (!(cardReader instanceof ReadPdReader)) {
            // Если ридер не поддерживает чтение ПД, завершаем работу
            // http://agile.srvdev.ru/browse/CPPKPP-35253
            return null;
        }

        ReadCardResult<List<Pd>> readPdListResult = ((ReadPdReader) cardReader).readPdList();

        if (readPdListResult.isSuccess()) {
            // Если удалось прочитать список ПД, получаем его
            return readPdListResult.getData();
        } else {
            // Если не удалось прочитать список ПД, завершаем работу
            return null;
        }
    }

    private boolean readEds(CardReader cardReader, @NonNull List<PD> legacyPdList) {
        if (!(cardReader instanceof ReadEdsReader)) {
            // Если ридер не поддерживает чтение ЭЦП, завершаем работу
            return false;
        }

        ReadCardResult<byte[]> readEdsResult = ((ReadEdsReader) cardReader).readEds();

        if (readEdsResult.isSuccess()) {
            // Если удалось прочитать ЭЦП, получаем её
            byte[] eds = readEdsResult.getData();
            for (PD legacyPd : legacyPdList) {
                legacyPd.ecp = eds;
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean handleResult(ReadBscResult readBscResult) {
        if (readBscResult.pdListRead) {
            pdHandler.handle(readBscResult.legacyPdList);
            return true;
        } else {
            return false;
        }
    }

    private void onBscResultRead(ReadBscResult readBscResult) {
        List<PD> pdList = readBscResult.legacyPdList;
        //если вернулся хотя бы 1 билет, запускаем активити для аннулирвоания Пд
        if (pdList != null && !pdList.isEmpty()) {
            navigator.navigateToRepealFinishActivity(pdList, 0);
        } else {
            navigator.navigateToRepealBSCReadErrorActivity();
        }
    }

    private void startTimer() {
        if (!timerSubscription.isUnsubscribed()) {
            throw new IllegalStateException("Operation is already running");
        }
        timerSubscription = Observable
                .interval(0, 1, TimeUnit.SECONDS, SchedulersCPPK.background())
                .take(TIMER_VALUE + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        stopReadCard();
                        view.setState(RepealReadBscView.State.SEARCH_CARD_ERROR);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(TAG, e);
                    }

                    @Override
                    public void onNext(Long second) {
                        view.setTimerValue((int) (TIMER_VALUE - second));
                    }
                });
    }

    private void stopTimer() {
        timerSubscription.unsubscribe();
    }

    @Override
    public void destroy() {
        timerSubscription.unsubscribe();
        readCardSubscription.unsubscribe();
        super.destroy();
    }

    void onScreenClosed() {
        if (!readCardSubscription.isUnsubscribed()) {
            stopReadCard();
            stopTimer();
            uiThread.post(() -> view.setState(RepealReadBscView.State.UNKNOWN_ERROR));
        }
    }

    private static class ReadBscResult {
        private boolean pdListRead = false;
        ru.ppr.cppk.dataCarrier.entity.BscInformation legacyBscInformation;
        ArrayList<PD> legacyPdList;
    }

    interface Navigator {
        void navigateToRepealFinishActivity(List<PD> pdList, long id);

        void navigateToRepealBSCReadErrorActivity();
    }

    private class CardNotFoundException extends Exception {
    }
}
