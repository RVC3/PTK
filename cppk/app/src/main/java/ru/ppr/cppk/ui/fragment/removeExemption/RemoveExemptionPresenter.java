package ru.ppr.cppk.ui.fragment.removeExemption;

import android.support.annotation.NonNull;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.model.RemoveExemptionParams;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class RemoveExemptionPresenter extends BaseMvpViewStatePresenter<RemoveExemptionView, RemoveExemptionViewState> {

    private static final String TAG = Logger.makeLogTag(RemoveExemptionPresenter.class);

    private InteractionListener mInteractionListener;

    private boolean mInitialized = false;
    // Ext
    private RemoveExemptionParams mRemoveExemptionParams;

    public RemoveExemptionPresenter() {

    }

    @Override
    protected RemoveExemptionViewState provideViewState() {
        return new RemoveExemptionViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    void initialize(RemoveExemptionParams removeExemptionParams) {
        if (!mInitialized) {
            mInitialized = true;
            mRemoveExemptionParams = removeExemptionParams;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        RemoveExemptionView.ExemptionInfo exemptionInfo = new RemoveExemptionView.ExemptionInfo();
        exemptionInfo.expressCode = mRemoveExemptionParams.getExpressCode();
        exemptionInfo.groupName = mRemoveExemptionParams.getGroupName();
        exemptionInfo.percentage = mRemoveExemptionParams.getPercentage();
        exemptionInfo.fio = mRemoveExemptionParams.getFio();
        exemptionInfo.documentNumber = mRemoveExemptionParams.getDocumentNumber();
        exemptionInfo.documentIssueDate = mRemoveExemptionParams.getDocumentIssueDate();
        exemptionInfo.bscType = mRemoveExemptionParams.getBscType();
        exemptionInfo.bscNumber = mRemoveExemptionParams.getBscNumber();
        view.setExemptionInfo(exemptionInfo);
        view.setSnilsFieldVisible(mRemoveExemptionParams.isRequireSnilsNumber());
        view.setDocumentNumberFieldVisible(!mRemoveExemptionParams.isRequireSnilsNumber());
    }

    void onRemoveBtnClicked() {
        mInteractionListener.navigateToPreviousScreen(true);
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void navigateToPreviousScreen(boolean exemptionRemoved);
    }

}
