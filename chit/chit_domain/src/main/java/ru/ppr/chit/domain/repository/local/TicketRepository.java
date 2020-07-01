package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.Ticket;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * @author Dmitry Nevolin
 */
public interface TicketRepository extends CrudLocalDbRepository<Ticket, Long> {

    @Nullable
    Ticket loadByTicket(@NonNull Long ticketId);

}
