package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.repository.security.TicketWhiteListItemRepository;

/**
 * Класс для проверки наличия билета в белом списке.
 *
 * @author Aleksandr Brazhkin
 */
public class WhiteListChecker {

    private final TicketWhiteListItemRepository ticketWhiteListItemRepository;

    @Inject
    WhiteListChecker(TicketWhiteListItemRepository ticketWhiteListItemRepository) {
        this.ticketWhiteListItemRepository = ticketWhiteListItemRepository;
    }

    /**
     * Выполняет проверку наличия ПД в белом списке.
     *
     * @return {@code true} если ПД в белом списке, {@code false} - иначе
     */
    public boolean isInWhiteList(TicketId ticketId) {
        return ticketWhiteListItemRepository.loadByTicketId(ticketId) != null;
    }
}
