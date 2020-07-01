package ru.ppr.cppk.ui.activity.pdrepeal.poscancel;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.dagger.ActivityScope;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
public class PosCancelSharedModel {

    private Callback callback;

    @Inject
    PosCancelSharedModel() {

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void onOperationFailed(long bankTransactionEventId, List<String> receipt, String message) {
        if (callback != null) {
            callback.onOperationFailed(bankTransactionEventId, receipt, message);
        }
    }

    public void onOperationCompleted(long bankTransactionEventId, List<String> receipt, String message) {
        if (callback != null) {
            callback.onOperationCompleted(bankTransactionEventId, receipt, message);
        }
    }

    public void onOperationCanceled() {
        if (callback != null) {
            callback.onOperationCanceled();
        }
    }

    /**
     * Колбек экрана отмены трназакции.
     */
    public interface Callback {

        void onOperationCompleted(long bankTransactionEventId, List<String> receipt, String message);

        void onOperationFailed(long bankTransactionEventId, List<String> receipt, String message);

        void onOperationCanceled();
    }

}
