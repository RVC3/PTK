package ru.ppr.cppk.ui.fragment.exemptionManualInput;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.logic.exemptionChecker.ExemptionChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;

/**
 * @author Aleksandr Brazhkin
 */
public class ExemptionManualInputPresenter extends BaseMvpViewStatePresenter<ExemptionManualInputView, ExemptionManualInputViewState> {

    private static final String TAG = Logger.makeLogTag(ExemptionManualInputPresenter.class);

    private static final int EXEMPTION_CODE_LENGTH = 4;

    private InteractionListener mInteractionListener;

    private boolean initialized = false;
    private ExemptionChecker exemptionChecker;
    private NsiVersionManager nsiVersionManager;
    private SelectExemptionParams selectExemptionParams;
    private TicketTypeRepository ticketTypeRepository;
    private ExemptionRepository exemptionRepository;

    public ExemptionManualInputPresenter() {

    }

    @Override
    protected ExemptionManualInputViewState provideViewState() {
        return new ExemptionManualInputViewState();
    }

    void bindInteractionListener(@NonNull final InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    void initialize(SelectExemptionParams selectExemptionParams,
                    ExemptionChecker exemptionChecker,
                    NsiVersionManager nsiVersionManager,
                    TicketTypeRepository ticketTypeRepository,
                    ExemptionRepository exemptionRepository) {
        if (!initialized) {
            initialized = true;
            this.selectExemptionParams = selectExemptionParams;
            this.exemptionChecker = exemptionChecker;
            this.nsiVersionManager = nsiVersionManager;
            this.ticketTypeRepository = ticketTypeRepository;
            this.exemptionRepository = exemptionRepository;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        view.setReadFromCardBtnVisible(selectExemptionParams.isAllowReadFromBsc());
    }

    void onReadFromCardClicked() {
        mInteractionListener.navigateToReadFromCard();
    }

    void onCodeEntered(String code) {
        List<ExemptionForEvent> exemptionsForEvent = new ArrayList<>();
        if (code.length() == EXEMPTION_CODE_LENGTH) {

            int expressCode = Integer.valueOf(code);

            for (int regionCode : selectExemptionParams.getRegionCodes()) {

                ExemptionForEvent exemptionForEvent = new ExemptionForEvent();
                exemptionForEvent.setExpressCode(expressCode);
                exemptionForEvent.setManualInput(true);

                List<Exemption> exemptions = exemptionRepository.getActualExemptionsForRegion(
                        exemptionForEvent.getExpressCode(), regionCode, new Date(), nsiVersionManager.getCurrentNsiVersionId());
                Exemption exemption = exemptions.isEmpty() ? null : exemptions.get(0);

                if (exemption != null) {
                    ExemptionChecker.CheckResult checkResult = exemptionChecker.check(selectExemptionParams, exemptionForEvent, exemption, regionCode, null);
                    if (checkResult == ExemptionChecker.CheckResult.SUCCESS) {
                        exemptionForEvent.fillFromExemption(exemption);
                        exemptionsForEvent.add(exemptionForEvent);
                    } else {
                        ExemptionManualInputView.ExemptionUsageDisabledMessage exemptionUsageDisabledMessage = new ExemptionManualInputView.ExemptionUsageDisabledMessage();
                        exemptionUsageDisabledMessage.checkResult = checkResult;
                        exemptionUsageDisabledMessage.exemptionExpressCode = exemptionForEvent.getExpressCode();
                        TicketType ticketType = ticketTypeRepository.load(selectExemptionParams.getTicketTypeCode(), selectExemptionParams.getVersionNsi());
                        exemptionUsageDisabledMessage.ticketTypeName = ticketType.getShortName();
                        view.showExemptionUsageDisabledMessage(exemptionUsageDisabledMessage);
                        return;
                    }
                } else {
                    view.showExemptionNotFoundMessage(expressCode);
                    return;
                }
            }

            mInteractionListener.navigateToEnterSurname(exemptionsForEvent);
        }
    }

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void navigateToReadFromCard();

        void navigateToEnterSurname(List<ExemptionForEvent> exemptionsForEvent);
    }

}
