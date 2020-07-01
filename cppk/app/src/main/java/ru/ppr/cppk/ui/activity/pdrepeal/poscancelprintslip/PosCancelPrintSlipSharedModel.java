package ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.dagger.ActivityScope;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
public class PosCancelPrintSlipSharedModel {
    /**
     * Слип банковского терминала об отмене банковской транзакции
     */
    private List<String> slipReceipt;

    private Callback callback;

    @Inject
    PosCancelPrintSlipSharedModel() {

    }

    public List<String> getSlipReceipt() {
        return slipReceipt;
    }

    public void setSlipReceipt(List<String> slipReceipt) {
        this.slipReceipt = slipReceipt;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void onOperationFailed() {
        if (callback != null) {
            callback.onOperationFailed();
        }
    }

    public void onOperationCompleted() {
        if (callback != null) {
            callback.onOperationCompleted();
        }
    }

    public interface Callback {
        void onOperationFailed();

        void onOperationCompleted();
    }

}
