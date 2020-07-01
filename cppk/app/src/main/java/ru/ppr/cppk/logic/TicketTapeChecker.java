package ru.ppr.cppk.logic;

import javax.inject.Inject;

import ru.ppr.core.helper.Resources;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.exceptions.PrettyException;
import rx.Completable;

/**
 * Класс-помощник для проверки, установлена ли билетная лента.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTapeChecker {

    private final LocalDaoSession mLocalDaoSession;
    private final Resources mResources;

    @Inject
    TicketTapeChecker(LocalDaoSession localDaoSession, Resources resources) {
        this.mLocalDaoSession = localDaoSession;
        this.mResources = resources;
    }

    public boolean check() {
        return mLocalDaoSession.getTicketTapeEventDao().isTicketTapeSet();
    }

    public Completable checkOrThrow() {
        return Completable.fromCallable(() -> {
            if (check()) {
                return true;
            } else {
                throw new TicketTapeIsNotSetException(mResources.getString(R.string.error_msg_ticket_tape_is_not_set));
            }
        });
    }

    public static class TicketTapeIsNotSetException extends PrettyException {
        public TicketTapeIsNotSetException(String detailMessage) {
            super(detailMessage);
        }
    }
}
