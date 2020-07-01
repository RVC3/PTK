package ru.ppr.cppk.ui.activity.selectExemption;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.core.ui.mvp.presenter.BaseMvpPresenter;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.ui.activity.extraPayment.ExtraPaymentPresenter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
public class SelectExemptionPresenter extends BaseMvpPresenter<SelectExemptionView> {

    private static final String TAG = Logger.makeLogTag(ExtraPaymentPresenter.class);

    private InteractionListener mInteractionListener;

    private boolean mInitialized = false;
    private SelectExemptionParams mSelectExemptionParams;
    private TicketStorageTypeToTicketTypeChecker mTicketStorageTypeToTicketTypeChecker;

    public SelectExemptionPresenter() {

    }

    void initialize(SelectExemptionParams selectExemptionParams, TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker) {
        if (!mInitialized) {
            mInitialized = true;
            mSelectExemptionParams = selectExemptionParams;
            mTicketStorageTypeToTicketTypeChecker = ticketStorageTypeToTicketTypeChecker;
            if (mTicketStorageTypeToTicketTypeChecker.check(TicketStorageType.Paper, mSelectExemptionParams.getTicketTypeCode())) {
                mInteractionListener.transferDataToChildManualInput(
                        mSelectExemptionParams
                );
            } else {
                mInteractionListener.transferDataToChildReadFromBsk(
                        mSelectExemptionParams
                );
            }
        }
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        this.mInteractionListener = interactionListener;
    }

    void onCancelSelectExemption() {
        mInteractionListener.navigateToPreviousScreen(null);
    }

    void onExemptionSelected(@NonNull List<ExemptionForEvent> exemptionsForEvent, @Nullable AdditionalInfoForEtt additionalInfoForEtt) {
        SelectExemptionResult selectExemptionResult = new SelectExemptionResult(exemptionsForEvent, additionalInfoForEtt);
        mInteractionListener.navigateToPreviousScreen(selectExemptionResult);
    }

    void onNavigateToReadFromCard() {
        mInteractionListener.navigateToReadFromCard(mSelectExemptionParams);
    }

    void onNavigateToEnterSurname(List<ExemptionForEvent> exemptionForEvents) {
        mInteractionListener.navigateToEnterSurname(exemptionForEvents);
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void transferDataToChildManualInput(SelectExemptionParams selectExemptionParams);

        void transferDataToChildReadFromBsk(SelectExemptionParams selectExemptionParams);

        void navigateToReadFromCard(SelectExemptionParams selectExemptionParams);

        void navigateToEnterSurname(List<ExemptionForEvent> exemptionForEvents);

        void navigateToPreviousScreen(SelectExemptionResult selectExemptionResult);
    }
}
