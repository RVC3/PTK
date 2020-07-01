package ru.ppr.cppk.logic;

import javax.inject.Inject;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.TicketId;

/**
 * Выыполняет поиск билета в белом списке
 *
 * @author Grigoriy Kashka
 */
public class WhiteListChecker {

    private final SecurityDaoSession securityDaoSession;

    @Inject
    WhiteListChecker(SecurityDaoSession securityDaoSession) {
        this.securityDaoSession = securityDaoSession;
    }

    /**
     * Выполняет проверку наличия ПД в белом списке.
     *
     * @return {@code true} если ПД в белом списке, {@code false} - иначе
     */
    public boolean isInWhiteList(TicketId ticketId) {
        return securityDaoSession.getTicketWhiteListItemDao().isTicketInWhiteList(ticketId);
    }
}
