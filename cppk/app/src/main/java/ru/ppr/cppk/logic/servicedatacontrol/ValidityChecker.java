package ru.ppr.cppk.logic.servicedatacontrol;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.logic.interactor.DeviceIdChecker;
import ru.ppr.core.manager.eds.CheckSignResult;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardData;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.interactor.CardInStopListChecker;
import ru.ppr.security.entity.StopCriteriaType;

/**
 * Валидатор события контроля СТУ.
 *
 * @author Aleksandr Brazhkin
 */
public class ValidityChecker {

    private final CardInStopListChecker cardInStopListChecker;
    private final ValidityStartDateChecker validityStartDateChecker;
    private final ValidityEndDateChecker validityEndDateChecker;
    private final EdsChecker edsChecker;
    private final DeviceIdChecker deviceIdChecker;
    private final AllAreasChecker allAreasChecker;

    @Inject
    ValidityChecker(CardInStopListChecker cardInStopListChecker,
                    ValidityStartDateChecker validityStartDateChecker,
                    ValidityEndDateChecker validityEndDateChecker,
                    EdsChecker edsChecker,
                    DeviceIdChecker deviceIdChecker,
                    AllAreasChecker allAreasChecker) {
        this.cardInStopListChecker = cardInStopListChecker;
        this.validityStartDateChecker = validityStartDateChecker;
        this.validityEndDateChecker = validityEndDateChecker;
        this.edsChecker = edsChecker;
        this.deviceIdChecker = deviceIdChecker;
        this.allAreasChecker = allAreasChecker;
    }

    /**
     * Выполняет валидацию СТУ.
     *
     * @param serviceTicketControlCardData Данные, считанные с карты
     * @param nsiVersion                   Версия НСИ
     * @return Результат проверки
     */
    public Result check(@NonNull ServiceTicketControlCardData serviceTicketControlCardData, int nsiVersion) {
        ServiceTicketControlEvent serviceTicketControlEvent = serviceTicketControlCardData.getServiceTicketControlEvent();

        CardInStopListChecker.Result stopListCheckResult = cardInStopListChecker.check(serviceTicketControlCardData.getCardInformation(), StopCriteriaType.SERVICE_TICKET_USAGE, nsiVersion);
        boolean valid = !stopListCheckResult.isInStopList();

        boolean travelAllowed = serviceTicketControlEvent.isCanTravel();
        valid &= travelAllowed;

        boolean startDateValid = validityStartDateChecker.isStartDateValid(serviceTicketControlEvent);
        valid &= startDateValid;

        boolean endDateValid = validityEndDateChecker.isEndDateValid(serviceTicketControlEvent);
        valid &= endDateValid;

        CheckSignResult checkSignResult = edsChecker.check(serviceTicketControlEvent, serviceTicketControlCardData);
        valid &= checkSignResult.getState() == CheckSignResultState.VALID;

        boolean deviceIdValid = deviceIdChecker.isDeviceIdValid(checkSignResult.getDeviceId());
        valid &= deviceIdValid;

        boolean forAllAreas = allAreasChecker.isForAllAreas(serviceTicketControlCardData.getCoverageAreaList());

        return new Result(valid, stopListCheckResult, startDateValid, endDateValid, travelAllowed, checkSignResult, deviceIdValid, forAllAreas);
    }

    public static class Result {
        private final boolean valid;
        private final boolean inStopList;
        private final int stopListReasonCode;
        private final String stopListReason;
        private final boolean startDateValid;
        private final boolean endDateValid;
        private final boolean travelAllowed;
        private final CheckSignResultState checkSignResultState;
        private final long deviceId;
        private final boolean deviceIdValid;
        private final boolean forAllAreas;

        Result(boolean valid,
               CardInStopListChecker.Result stopListCheckResult,
               boolean startDateValid, boolean endDateValid,
               boolean travelAllowed, CheckSignResult checkSignResult,
               boolean deviceIdValid, boolean forAllAreas) {
            this.valid = valid;
            this.inStopList = stopListCheckResult.isInStopList();
            this.stopListReasonCode = stopListCheckResult.getReasonCode();
            this.stopListReason = stopListCheckResult.getReason();
            this.startDateValid = startDateValid;
            this.endDateValid = endDateValid;
            this.travelAllowed = travelAllowed;
            this.checkSignResultState = checkSignResult.getState();
            this.deviceId = checkSignResult.getDeviceId();
            this.deviceIdValid = deviceIdValid;
            this.forAllAreas = forAllAreas;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isInStopList() {
            return inStopList;
        }

        public int getStopListReasonCode() {
            return stopListReasonCode;
        }

        public String getStopListReason() {
            return stopListReason;
        }

        public boolean isStartDateValid() {
            return startDateValid;
        }

        public boolean isEndDateValid() {
            return endDateValid;
        }

        public boolean isTravelAllowed() {
            return travelAllowed;
        }

        public CheckSignResultState getCheckSignResultState() {
            return checkSignResultState;
        }

        public long getDeviceId() {
            return deviceId;
        }

        public boolean isDeviceIdValid() {
            return deviceIdValid;
        }

        public boolean isForAllAreas() {
            return forAllAreas;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "valid=" + valid +
                    ", inStopList=" + inStopList +
                    ", stopListReasonCode=" + stopListReasonCode +
                    ", stopListReason='" + stopListReason + '\'' +
                    ", startDateValid=" + startDateValid +
                    ", endDateValid=" + endDateValid +
                    ", travelAllowed=" + travelAllowed +
                    ", checkSignResultState=" + checkSignResultState +
                    ", deviceId=" + deviceId +
                    ", deviceIdValid=" + deviceIdValid +
                    ", forAllAreas=" + forAllAreas +
                    '}';
        }
    }

}
