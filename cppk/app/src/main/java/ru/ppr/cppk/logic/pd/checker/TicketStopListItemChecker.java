package ru.ppr.cppk.logic.pd.checker;

import javax.inject.Inject;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.TicketId;

/**
 * Проверка по стоплисту билетов.
 *
 * @author Grigoriy Kashka
 */
public class TicketStopListItemChecker {

    private final SecurityDaoSession mSecurityDaoSession;

    @Inject
    public TicketStopListItemChecker(SecurityDaoSession mSecurityDaoSession) {
        this.mSecurityDaoSession = mSecurityDaoSession;
    }

    public boolean check(TicketId ticketId) {
        return mSecurityDaoSession.getTicketStopListItemDao().isTicketInStopList(ticketId);
    }
}
