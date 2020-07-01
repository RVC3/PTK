package ru.ppr.cppk.ui.activity.pdSale;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
class PdSaleDi {

    private final Di di;

    PdSaleDi(Di di) {
        this.di = di;
    }

    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    EventBuilder eventBuilder() {
        return di.eventBuilder();
    }

    ShiftManager shiftManager() {
        return di.getShiftManager();
    }

    TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker() {
        return new TicketStorageTypeToTicketTypeChecker(di.nsiVersionManager(), Dagger.appComponent().ticketTypeRepository());
    }
}
