package ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.logger.Logger;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Aleksandr Brazhkin
 */
public class PosCancelPrintSlipPresenter extends BaseMvpViewStatePresenter<PosCancelPrintSlipView, PosCancelPrintSlipViewState> {

    private static final String TAG = Logger.makeLogTag(PosCancelPrintSlipPresenter.class);

    private Navigator navigator;

    private boolean initialized = false;
    private final PrinterManager printerManager;
    private final TicketTapeChecker ticketTapeChecker;
    private final PosCancelPrintSlipSharedModel posCancelPrintSlipSharedModel;
    ///////////////////////////////////////////////////
    private List<String> slipReceipt;

    @Inject
    PosCancelPrintSlipPresenter(PosCancelPrintSlipViewState posCancelViewState,
                                PrinterManager printerManager,
                                TicketTapeChecker ticketTapeChecker,
                                PosCancelPrintSlipSharedModel posCancelPrintSlipSharedModel) {
        super(posCancelViewState);
        this.printerManager = printerManager;
        this.ticketTapeChecker = ticketTapeChecker;
        this.posCancelPrintSlipSharedModel = posCancelPrintSlipSharedModel;
    }

    void bindNavigator(@NonNull final Navigator navigator) {
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

        slipReceipt = posCancelPrintSlipSharedModel.getSlipReceipt();

        if (slipReceipt != null && slipReceipt.isEmpty()) {
            throw new IllegalArgumentException("Nothing to print");
        }

        printSlip(slipReceipt);
    }

    void onSetTicketTapeFinished() {
        Logger.trace(TAG, "onSetTicketTapeFinished");
        view.showFailState();
    }

    void onSuccessRepeatBtnClicked() {
        Logger.trace(TAG, "onSuccessRepeatBtnClicked");
        printSlip(slipReceipt);
    }

    void onSuccessContinueBtnClicked() {
        Logger.trace(TAG, "onSuccessContinueBtnClicked");
        posCancelPrintSlipSharedModel.onOperationCompleted();
    }

    void onFailRepeatBtnClicked() {
        Logger.trace(TAG, "onFailRepeatBtnClicked");
        printSlip(slipReceipt);
    }

    void onFailAbortBtnClicked() {
        Logger.trace(TAG, "onFailAbortBtnClicked");
        posCancelPrintSlipSharedModel.onOperationFailed();
    }

    private void printSlip(List<String> slipReceipt) {
        Logger.trace(TAG, "printSlip");
        ticketTapeChecker.checkOrThrow()
                .andThen(Single.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();
                    params.tplParams.slipLines = slipReceipt;
                    return params;
                }))
                .flatMapCompletable(params -> printerManager.getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call()
                        .toCompletable())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(subscription -> view.showPrintingState())
                .subscribe(view::showSuccessState, throwable -> {
                    if (throwable instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                        navigator.navigateToTicketTapeIsNotSet();
                    } else {
                        view.showFailState();
                    }
                });
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface Navigator {
        void navigateToTicketTapeIsNotSet();
    }

}
