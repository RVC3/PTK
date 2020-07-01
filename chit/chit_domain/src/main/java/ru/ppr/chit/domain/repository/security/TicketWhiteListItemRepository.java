package ru.ppr.chit.domain.repository.security;

import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.model.security.TicketWhiteListItem;
import ru.ppr.chit.domain.repository.security.base.SecurityDbRepository;

/**
 * @author Aleksandr Brazhkin
 */
public interface TicketWhiteListItemRepository extends SecurityDbRepository {
    TicketWhiteListItem loadByTicketId(TicketId ticketId);
}
