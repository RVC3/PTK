package ru.ppr.cppk.logic.servicedatacontrol;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.cppk.db.local.repository.ServiceTicketControlEventRepository;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.localdb.model.ServiceTicketPassageResult;

/**
 * Класс для обновления события контроля СТУ в соответствии с результатом проверки.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceTicketControlEventUpdater {

    private final ServiceTicketControlEventRepository serviceTicketControlEventRepository;

    @Inject
    ServiceTicketControlEventUpdater(ServiceTicketControlEventRepository serviceTicketControlEventRepository) {
        this.serviceTicketControlEventRepository = serviceTicketControlEventRepository;
    }

    /**
     * Обновляет событие контроля СТУ в соответствии с результатом проверки.
     *
     * @param serviceTicketControlEvent Событие контроля СТУ
     * @param checkResult               Результат валидации СТУ
     */
    public void update(@NonNull ServiceTicketControlEvent serviceTicketControlEvent,
                       @NonNull ValidityChecker.Result checkResult) {
        serviceTicketControlEvent.setTicketDeviceId(checkResult.getDeviceId());
        if (checkResult.isValid()) {
            if (checkResult.isForAllAreas() && !serviceTicketControlEvent.isRequireCheckDocument()) {
                serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.SUCCESS_PASSAGE);
                serviceTicketControlEvent.setPassageSign(true);
                serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
            }
        } else if (checkResult.getCheckSignResultState() == CheckSignResultState.INVALID) {
            serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.INVALID_SIGN);
            serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
        } else if (checkResult.getCheckSignResultState() == CheckSignResultState.KEY_REVOKED) {
            serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.SIGN_KEY_REVOKED);
            serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
        } else if (checkResult.isInStopList()) {
            serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.BANNED_BY_STOP_LIST_CARDS);
            serviceTicketControlEvent.setStopListId(checkResult.getStopListReasonCode());
            serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
        } else if (!checkResult.isTravelAllowed()) {
            serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.NO_VALID_ZONE);
            serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
        } else if (!checkResult.isStartDateValid() || !checkResult.isEndDateValid()) {
            serviceTicketControlEvent.setValidationResult(ServiceTicketPassageResult.TOO_LATE);
            serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.COMPLETED);
        }
        serviceTicketControlEventRepository.update(serviceTicketControlEvent);
    }
}
