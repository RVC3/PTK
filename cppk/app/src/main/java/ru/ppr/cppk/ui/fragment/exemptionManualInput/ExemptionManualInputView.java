package ru.ppr.cppk.ui.fragment.exemptionManualInput;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.logic.exemptionChecker.ExemptionChecker;

/**
 * @author Aleksandr Brazhkin
 */
interface ExemptionManualInputView extends MvpView {

    void setReadFromCardBtnVisible(boolean visible);

    void showExemptionNotFoundMessage(int exemptionExpressCode);

    void showExemptionUsageDisabledMessage(ExemptionUsageDisabledMessage exemptionUsageDisabledMessage);

    class ExemptionUsageDisabledMessage {
        ExemptionChecker.CheckResult checkResult;
        int exemptionExpressCode;
        String ticketTypeName;
    }
}
