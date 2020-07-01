package ru.ppr.cppk.ui.activity.senddocstoofd;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.logger.Logger;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Grigoriy Kashka
 */
class SendDocsToOfdPresenter extends BaseMvpViewStatePresenter<SendDocsToOfdView, SendDocsToOfdViewState> {

    private static final String TAG = Logger.makeLogTag(SendDocsToOfdPresenter.class);

    private final PrinterManager printerManager;

    private boolean mInitialized = false;

    private Navigator navigator;
    private boolean backToWelcomeActivity = false;

    @Inject
    SendDocsToOfdPresenter(SendDocsToOfdViewState ofdSettingsViewState, PrinterManager printerManager) {
        super(ofdSettingsViewState);
        this.printerManager = printerManager;
    }

    void initialize() {
        if (!mInitialized) {
            mInitialized = true;
        }
        startGetData();
    }

    void setBackToWelcomeActivity(boolean backToWelcomeActivity) {
        this.backToWelcomeActivity = backToWelcomeActivity;
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void onBackPressed() {
        if (backToWelcomeActivity) {
            navigator.navigateToWelcomeActivity();
        } else {
            navigator.navigateBack();
        }
    }

    /**
     * Обработчик кнопки Обновить
     */
    void onUpdateBtnClick() {
        startGetData();
    }

    private void startGetData() {
        Logger.trace(TAG, "startGetData()");
        view.showProgress();
        printerManager.getOperationFactory().getOfdDocsStateOperation().call()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    Logger.trace(TAG, "startGetData() doOnNext() -> result=" + result.toString());
                    view.setUnsentDocsCount(result.getUnsentDocumentsCount());
                    view.setFirstUnsentDocNumber(result.getFirstUnsentDocumentNumber());
                    view.setFirstUnsentDocDateTime(result.getFirstUnsentDocumentDateTime());
                })
                .subscribeOn(SchedulersCPPK.printer())
                .subscribe(result -> {
                    Logger.trace(TAG, "startGetData() subscribe() -> result=" + result.toString());
                    view.hideProgress();
                    view.showError(result.getUnsentDocumentsCount() == 0 ? SendDocsToOfdView.Error.NONE : SendDocsToOfdView.Error.EXIST_UNSENT_DOCS);
                }, throwable -> {
                    Logger.error(TAG, throwable);
                    view.showError(SendDocsToOfdView.Error.GET_DATA);
                    view.hideProgress();
                });
    }

    /**
     * Обработчик кнопки Отправить
     */
    void onSendBtnClick() {
        Logger.trace(TAG, "onSendBtnClick()");
        view.showProgress();
        printerManager.getOperationFactory().getSendDocsToOfdOperation(60).call()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    Logger.trace(TAG, "onSendBtnClick() doOnNext() -> result=" + result.toString());
                    view.setUnsentDocsCount(result.getUnsentDocumentsCount());
                    view.setFirstUnsentDocNumber(result.getFirstUnsentDocumentNumber());
                    view.setFirstUnsentDocDateTime(result.getFirstUnsentDocumentDateTime());
                })
                .subscribeOn(SchedulersCPPK.printer())
                .subscribe(result -> {
                    Logger.trace(TAG, "onSendBtnClick() subscribe() -> result=" + result.toString());
                    view.hideProgress();
                    view.showError(result.getUnsentDocumentsCount() == 0 ? SendDocsToOfdView.Error.NONE : SendDocsToOfdView.Error.NOT_ALL_DOCS_SENT);
                }, throwable -> {
                    Logger.error(TAG, throwable);
                    view.showError(SendDocsToOfdView.Error.GET_DATA);
                    view.hideProgress();
                });
    }

    interface Navigator {

        void navigateBack();

        void navigateToWelcomeActivity();

    }

}
