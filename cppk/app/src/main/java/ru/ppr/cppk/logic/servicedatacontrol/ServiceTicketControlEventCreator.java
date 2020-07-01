package ru.ppr.cppk.logic.servicedatacontrol;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithFlags;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithOrderNumber;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithValidityTime;
import ru.ppr.cppk.db.local.repository.ServiceTicketControlEventRepository;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.legacy.CardTypeToTicketStorageTypeMapper;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.localdb.model.ServiceZoneType;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.creator.EventCreator;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Создатель события контроля СТУ в статусе {@link ServiceTicketControlEvent.Status#CREATED}.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceTicketControlEventCreator {

    private static final String TAG = Logger.makeLogTag(ServiceTicketControlEventCreator.class);

    private final LocalDbTransaction localDbTransaction;
    private final ShiftManager shiftManager;
    private final EventCreator eventCreator;
    private final ServiceTicketControlEventRepository serviceTicketControlEventRepository;

    @Inject
    ServiceTicketControlEventCreator(LocalDbTransaction localDbTransaction,
                                     ShiftManager shiftManager,
                                     EventCreator eventCreator,
                                     ServiceTicketControlEventRepository serviceTicketControlEventRepository) {
        this.localDbTransaction = localDbTransaction;
        this.shiftManager = shiftManager;
        this.eventCreator = eventCreator;
        this.serviceTicketControlEventRepository = serviceTicketControlEventRepository;
    }

    /**
     * Добавляет событие {@link ServiceTicketControlEvent} в БД.
     *
     * @param cardInformation Информация о карте
     * @param serviceData     Служебные данные
     * @return Созданное событие
     */
    public ServiceTicketControlEvent create(@NonNull CardInformation cardInformation, @NonNull ServiceData serviceData) {
        localDbTransaction.begin();
        try {
            ShiftEvent shiftEvent = shiftManager.getCurrentShiftEvent();
            // Пишем в БД Event
            Event event = eventCreator.create();
            // ServiceTicketControlEvent
            ServiceTicketControlEvent serviceTicketControlEvent = new ServiceTicketControlEvent();
            serviceTicketControlEvent.setControlDateTime(new Date());
            serviceTicketControlEvent.setEdsKeyNumber(serviceData.getEdsKeyNumber());
            serviceTicketControlEvent.setCardNumber(cardInformation.getOuterNumberAsString());
            serviceTicketControlEvent.setCardCristalId(cardInformation.getCrystalSerialNumberAsString());
            TicketStorageType ticketStorageType = new CardTypeToTicketStorageTypeMapper().map(cardInformation.getCardType());
            serviceTicketControlEvent.setTicketStorageType(ticketStorageType);
            serviceTicketControlEvent.setValidFrom(serviceData.getInitDateTime());
            serviceTicketControlEvent.setValidTo(getValidToDateTime(serviceData));
            // ПТК не может предоставить данную информацию
            // http://agile.srvdev.ru/browse/CPPKPP-35753
            serviceTicketControlEvent.setZoneType(ServiceZoneType.None);
            // ПТК не может предоставить данную информацию
            // http://agile.srvdev.ru/browse/CPPKPP-35753
            serviceTicketControlEvent.setZoneValue(0);
            serviceTicketControlEvent.setCanTravel(getCanTravel(serviceData));
            serviceTicketControlEvent.setRequirePersonification(getRequirePersonification(serviceData));
            serviceTicketControlEvent.setRequireCheckDocument(getRequireCheckDocument(serviceData));
            serviceTicketControlEvent.setTicketNumber(getTicketNumber(serviceData));
            // При контроле обычных ПД значение берется из метки прохода
            // В контроле СТУ метка прохода неучаствует, запишем 0
            serviceTicketControlEvent.setSmartCardUsageCount(0);
            serviceTicketControlEvent.setTicketWriteDateTime(serviceData.getInitDateTime());
            serviceTicketControlEvent.setStatus(ServiceTicketControlEvent.Status.CREATED);
            serviceTicketControlEvent.setEventId(event.getId());
            if (shiftEvent != null) {
                serviceTicketControlEvent.setCashRegisterWorkingShiftId(shiftEvent.getId());
            }
            serviceTicketControlEventRepository.insert(serviceTicketControlEvent);
            localDbTransaction.commit();
            Logger.trace(TAG, "Event created, id: " + serviceTicketControlEvent.getId());
            return serviceTicketControlEvent;
        } finally {
            localDbTransaction.end();
        }
    }

    private boolean getCanTravel(ServiceData serviceData) {
        if (serviceData instanceof ServiceDataWithFlags) {
            ServiceDataWithFlags serviceDataWithFlags = (ServiceDataWithFlags) serviceData;
            return serviceDataWithFlags.getCardType() == ServiceDataWithFlags.CardType.TRIP;
        } else {
            return false;
        }
    }

    private boolean getRequirePersonification(ServiceData serviceData) {
        if (serviceData instanceof ServiceDataWithFlags) {
            ServiceDataWithFlags serviceDataWithFlags = (ServiceDataWithFlags) serviceData;
            return serviceDataWithFlags.getPersonalizedFlag() == ServiceDataWithFlags.PersonalizedFlag.PERSONALIZED;
        } else {
            return false;
        }
    }

    private boolean getRequireCheckDocument(ServiceData serviceData) {
        if (serviceData instanceof ServiceDataWithFlags) {
            ServiceDataWithFlags serviceDataWithFlags = (ServiceDataWithFlags) serviceData;
            return serviceDataWithFlags.getMandatoryOfDocVerification() == ServiceDataWithFlags.MandatoryOfDocVerification.REQUIRED;
        } else {
            return false;
        }
    }

    private Date getValidToDateTime(ServiceData serviceData) {
        if (serviceData instanceof ServiceDataWithValidityTime) {
            ServiceDataWithValidityTime serviceDataWithValidityTime = (ServiceDataWithValidityTime) serviceData;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(serviceDataWithValidityTime.getInitDateTime());
            calendar.add(Calendar.DAY_OF_MONTH, serviceDataWithValidityTime.getValidityTime());
            return calendar.getTime();
        } else {
            return null;
        }
    }

    private int getTicketNumber(ServiceData serviceData) {
        if (serviceData instanceof ServiceDataWithOrderNumber) {
            ServiceDataWithOrderNumber serviceDataWithOrderNumber = (ServiceDataWithOrderNumber) serviceData;
            return serviceDataWithOrderNumber.getOrderNumber();
        } else {
            return 0;
        }
    }
}
