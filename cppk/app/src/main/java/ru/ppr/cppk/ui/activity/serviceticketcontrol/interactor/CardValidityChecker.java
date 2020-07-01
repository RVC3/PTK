package ru.ppr.cppk.ui.activity.serviceticketcontrol.interactor;

import android.support.annotation.NonNull;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.cppk.logic.SmartCardValidityTimeChecker;
import ru.ppr.security.entity.StopCriteriaType;

/**
 * Валидатор смарт-карты.
 *
 * @author Aleksandr Brazhkin
 */
public class CardValidityChecker {

    private final CardInStopListChecker cardInStopListChecker;
    private final SmartCardValidityTimeChecker smartCardValidityTimeChecker;

    @Inject
    public CardValidityChecker(CardInStopListChecker cardInStopListChecker, SmartCardValidityTimeChecker smartCardValidityTimeChecker) {
        this.cardInStopListChecker = cardInStopListChecker;
        this.smartCardValidityTimeChecker = smartCardValidityTimeChecker;
    }

    @NonNull
    public Result check(CardInformation cardInformation, StopCriteriaType stopCriteriaType, boolean serviceTicket, int nsiVersion) {
        boolean cardTimeValid = smartCardValidityTimeChecker.isCardTimeValid(cardInformation, new Date(), serviceTicket);
        boolean valid = cardTimeValid;

        CardInStopListChecker.Result stopListCheckResult = cardInStopListChecker.check(cardInformation, stopCriteriaType, nsiVersion);
        valid &= !stopListCheckResult.isInStopList();

        return new Result(valid, cardTimeValid, stopListCheckResult);
    }

    public static class Result {

        private final boolean valid;
        private final boolean cardTimeValid;
        private final boolean inStopList;
        private final String stoListReason;
        private final int stopListReasonCode;

        public Result(boolean valid, boolean cardTimeValid, CardInStopListChecker.Result stopListCheckResult) {
            this.valid = valid;
            this.cardTimeValid = cardTimeValid;
            this.inStopList = stopListCheckResult.isInStopList();
            this.stoListReason = stopListCheckResult.getReason();
            this.stopListReasonCode = stopListCheckResult.getReasonCode();
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isCardTimeValid() {
            return cardTimeValid;
        }

        public boolean isInStopList() {
            return inStopList;
        }

        public String getStoListReason() {
            return stoListReason;
        }

        public int getStopListReasonCode() {
            return stopListReasonCode;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "valid=" + valid +
                    ", cardTimeValid=" + cardTimeValid +
                    ", inStopList=" + inStopList +
                    ", stoListReason='" + stoListReason + '\'' +
                    ", stopListReasonCode=" + stopListReasonCode +
                    '}';
        }
    }
}
