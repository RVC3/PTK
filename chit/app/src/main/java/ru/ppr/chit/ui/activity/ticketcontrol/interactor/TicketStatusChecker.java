package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.model.local.TicketState;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.domain.repository.local.TicketRepository;
import ru.ppr.core.exceptions.UserCriticalException;

/**
 * Определитель статуса ПД.
 *
 * @author Dmitry Nevolin
 */
class TicketStatusChecker {

    private final TicketIdRepository ticketIdRepository;
    private final TicketRepository ticketRepository;

    @Inject
    TicketStatusChecker(TicketIdRepository ticketIdRepository,
                        TicketRepository ticketRepository) {
        this.ticketIdRepository = ticketIdRepository;
        this.ticketRepository = ticketRepository;
    }

    Result check(@NonNull TicketId ticketId) {
        Preconditions.checkNotNull(ticketId.getSaleDate());
        Preconditions.checkNotNull(ticketId.getDeviceId());

        TicketId storedTicketId = ticketIdRepository.loadByIdentity(ticketId.getTicketNumber(), ticketId.getSaleDate(), ticketId.getDeviceId());
        if (storedTicketId != null) {
            // Если нашли такой TicketId, ищем билет
            Ticket ticket = ticketRepository.loadByTicket(storedTicketId.getId());
            if (ticket != null) {
                // Проверяем состояние
                TicketState ticketState = ticket.getTicketState();
                if (ticketState == null) {
                    throw new UserCriticalException("Некорректный статус билета");
                }
                // Возвращаем результат в соответствии с состоянием
                switch (ticketState) {
                    case CANCELLED:
                        return Result.CANCELLED;
                    case RETURNED:
                        return Result.RETURNED;
                    case VALID:
                        return Result.VALID;
                    default:
                        throw new UserCriticalException("Некорректный статус билета");
                }
            } else {
                // Раз в списке такого билета нет, значит информации о состоянии билета нет
                return Result.NOT_FOUND;
            }
        } else {
            // Раз даже TicketId не нашли, значит информации о состоянии билета нет
            return Result.NOT_FOUND;
        }
    }

    enum Result {
        /**
         * Не найден
         */
        NOT_FOUND,
        /**
         * Действительный, т.е. не аннулированный и не возвращенный
         */
        VALID,
        /**
         * Аннулирован
         */
        CANCELLED,
        /**
         * Возвращён
         */
        RETURNED
    }
}
