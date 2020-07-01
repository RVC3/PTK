package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.TicketTypeRepository;

/**
 * Класс, выполняющий проверку возможности продажи ПД на определенный тип носителя
 *
 * @author Grigoriy Kashka
 */
public class TicketStorageTypeToTicketTypeChecker {

    private final NsiVersionManager nsiVersionManager;
    private final TicketTypeRepository ticketTypeRepository;

    @Inject
    public TicketStorageTypeToTicketTypeChecker(@NonNull NsiVersionManager nsiVersionManager,
                                                @NonNull TicketTypeRepository ticketTypeRepository) {
        this.nsiVersionManager = nsiVersionManager;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    /**
     * Выполняет проверку на возможность записи ПД на конкретный носитель
     *
     * @param ticketStorageType - носитель
     * @param ticketType        - тип билета
     * @return разрешение на запись
     */
    public boolean check(TicketStorageType ticketStorageType, TicketType ticketType) {
        return check(ticketStorageType, ticketType.getCode());
    }

    /**
     * Выполняет проверку на возможность записи ПД на конкретный носитель
     *
     * @param ticketStorageType
     * @param ticketTypeCode
     * @return
     */
    public boolean check(TicketStorageType ticketStorageType, long ticketTypeCode) {
        return ticketTypeRepository.canTicketTypeBeWrittenWithTicketStorageType(ticketStorageType, ticketTypeCode, nsiVersionManager.getCurrentNsiVersionId());
    }
}
