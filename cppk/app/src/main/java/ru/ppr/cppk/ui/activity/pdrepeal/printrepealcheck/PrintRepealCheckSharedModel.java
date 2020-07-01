package ru.ppr.cppk.ui.activity.pdrepeal.printrepealcheck;

import javax.inject.Inject;

import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.logic.PdRepealDocument;
import ru.ppr.cppk.logic.pdRepeal.PdRepealData;

/**
 * @author Aleksandr Brazhkin
 */
@ActivityScope
public class PrintRepealCheckSharedModel {

    private PdRepealDocument pdRepealDocument;
    private PdRepealData pdRepealData;

    private Callback callback;

    @Inject
    PrintRepealCheckSharedModel(){

    }

    public PdRepealDocument getPdRepealDocument() {
        return pdRepealDocument;
    }

    public void setPdRepealDocument(PdRepealDocument pdRepealDocument) {
        this.pdRepealDocument = pdRepealDocument;
    }

    public PdRepealData getPdRepealData() {
        return pdRepealData;
    }

    public void setPdRepealData(PdRepealData pdRepealData) {
        this.pdRepealData = pdRepealData;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void onOperationCanceled() {
        if (callback != null) {
            callback.onOperationCanceled();
        }
    }

    public void onOperationCompleted() {
        if (callback != null) {
            callback.onOperationCompleted();
        }
    }

    public interface Callback {
        void onOperationCanceled();

        void onOperationCompleted();
    }
}
