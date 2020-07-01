package ru.ppr.cppk.helpers.controlbscstorage;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory хранилище данных, считанных с карты при контроле БСК.
 * Используется для передачи данных на экран с отображением информации.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class ServiceTicketControlCardDataStorage {

    private ServiceTicketControlCardData serviceTicketControlCardData;

    @Inject
    ServiceTicketControlCardDataStorage() {

    }

    public void putCardData(ServiceTicketControlCardData serviceTicketControlCardData) {
        this.serviceTicketControlCardData = serviceTicketControlCardData;
    }

    public void clearCardData() {
        this.serviceTicketControlCardData = null;
    }

    public ServiceTicketControlCardData getLastCardData() {
        return serviceTicketControlCardData;
    }

}
