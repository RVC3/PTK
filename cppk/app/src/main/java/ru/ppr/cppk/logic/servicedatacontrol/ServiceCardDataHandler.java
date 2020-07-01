package ru.ppr.cppk.logic.servicedatacontrol;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardData;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;

/**
 * Обработчик данных при контроле БСК со служебными данными.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceCardDataHandler {

    private static final String TAG = Logger.makeLogTag(ServiceCardDataHandler.class);

    private final ServiceTicketControlEventCreator serviceTicketControlEventCreator;
    private final NsiVersionManager nsiVersionManager;
    private final ValidityChecker validityChecker;
    private final ServiceTicketControlEventUpdater serviceTicketControlEventUpdater;

    @Inject
    ServiceCardDataHandler(ServiceTicketControlEventCreator serviceTicketControlEventCreator,
                           NsiVersionManager nsiVersionManager,
                           ValidityChecker validityChecker, ServiceTicketControlEventUpdater serviceTicketControlEventUpdater) {
        this.serviceTicketControlEventCreator = serviceTicketControlEventCreator;
        this.validityChecker = validityChecker;
        this.nsiVersionManager = nsiVersionManager;
        this.serviceTicketControlEventUpdater = serviceTicketControlEventUpdater;
    }

    /**
     * Выполняет обработку считанной информации.
     * Создает событие в БД.
     * Модифицирует входящий параметр {@code serviceTicketControlCardData}
     *
     * @param serviceTicketControlCardData Данные, считанные с карты при контроле БСК с СТУ
     */
    public void handle(@NonNull ServiceTicketControlCardData serviceTicketControlCardData) {
        // Создаем событие контроля
        ServiceTicketControlEvent serviceTicketControlEvent = serviceTicketControlEventCreator.create(
                serviceTicketControlCardData.getCardInformation(),
                serviceTicketControlCardData.getServiceData()
        );
        serviceTicketControlCardData.setServiceTicketControlEvent(serviceTicketControlEvent);
        // Выполняем проверку
        ValidityChecker.Result checkResult = validityChecker.check(serviceTicketControlCardData, nsiVersionManager.getCurrentNsiVersionId());
        Logger.trace(TAG, "CheckResult: " + checkResult);
        serviceTicketControlCardData.setCheckResult(checkResult);

        if (!checkResult.isDeviceIdValid()) {
            // http://agile.srvdev.ru/browse/CPPKPP-39304
            // http://agile.srvdev.ru/browse/CPPKPP-44193
            // ЦОДу не нужны такие события
            // Оставляем в статусе STARTED
            Logger.trace(TAG, "Skipping event update: invalid device id");
            return;
        }
        // Обновляем событие контроля
        serviceTicketControlEventUpdater.update(serviceTicketControlEvent, checkResult);
    }
}
