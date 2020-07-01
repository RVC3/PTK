package ru.ppr.cppk.ui.activity.serviceticketcontrol.interactor;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.EnumSet;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.cppk.logic.SmartCardStopListChecker;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.security.entity.StopCriteriaType;

/**
 * Валидатор на наличие карты с СТУ в стоп-листе.
 *
 * @author Aleksandr Brazhkin
 */
public class CardInStopListChecker {

    private final SmartCardStopListChecker smartCardStopListChecker;

    @Inject
    public CardInStopListChecker(SmartCardStopListChecker smartCardStopListChecker) {
        this.smartCardStopListChecker = smartCardStopListChecker;
    }

    @NonNull
    public Result check(CardInformation cardInformation, StopCriteriaType stopCriteriaType, int nsiVersion) {
        Pair<SmartCardStopListItem, String> stopItemResult = smartCardStopListChecker.findSmartCardStopListItem(cardInformation, EnumSet.of(stopCriteriaType), nsiVersion);
        if (stopItemResult == null) {
            return new Result(null, -1, false);
        } else {
            return new Result(stopItemResult.second, stopItemResult.first.getReasonCode(), true);
        }
    }

    public static class Result {

        private final String reason;
        private final int reasonCode;
        private final boolean inStopList;

        public Result(String reason, int reasonCode, boolean inStopList) {
            this.reason = reason;
            this.reasonCode = reasonCode;
            this.inStopList = inStopList;
        }

        public String getReason() {
            return reason;
        }

        public int getReasonCode() {
            return reasonCode;
        }

        public boolean isInStopList() {
            return inStopList;
        }
    }

}
