package ru.ppr.cppk.ui.activity.selectExemption;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
class SelectExemptionDi {

    private final Di di;

    SelectExemptionDi(Di di) {
        this.di = di;
    }

    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }


    TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker() {
        return new TicketStorageTypeToTicketTypeChecker(di.nsiVersionManager(), Dagger.appComponent().ticketTypeRepository());
    }
}
