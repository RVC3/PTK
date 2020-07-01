package ru.ppr.cppk.ui.fragment.exemptionReadFromCard;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.cppk.logic.exemptionChecker.ExemptionChecker;

/**
 * @author Aleksandr Brazhkin
 */
interface ExemptionReadFromCardView extends MvpView {

    void showCardNotFoundError();

    void showReadCardError();

    void showNoExemptionOnCardError();

    void showCardInStopListError(String reason);

    void setRetryBtnVisible(boolean visible);

    void showCardValidityTimeError();

    void setBscType(String bscType);

    void setBscNumber(String bscNumber);

    void showUnknownError();

    void showReadCardState();

    void showSearchCardState();

    void showReadCompletedState();

    void setTimerValue(String value);

    void setFio(String fio);

    void setExemptionInfo(ExemptionInfo exemptionInfo);

    void showExemptionNotFoundMessage(int exemptionExpressCode);

    void showExemptionUsageDisabledMessage(ExemptionUsageDisabledMessage exemptionUsageDisabledMessage);

    class ExemptionInfo {
        Integer exemptionExpressCode;
        Integer percentage;
        String groupName;
    }

    class ExemptionUsageDisabledMessage {
        ExemptionChecker.CheckResult checkResult;
        int exemptionExpressCode;
        String ticketTypeName;
    }
}
