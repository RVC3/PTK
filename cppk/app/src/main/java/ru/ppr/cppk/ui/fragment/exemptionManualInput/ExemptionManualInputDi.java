package ru.ppr.cppk.ui.fragment.exemptionManualInput;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.logic.exemptionChecker.ExemptionChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;

/**
 * @author Aleksandr Brazhkin
 */
class ExemptionManualInputDi {

    private final Di di;

    ExemptionManualInputDi(Di di) {
        this.di = di;
    }

    private LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    private NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    ExemptionChecker exemptionChecker() {
        return new ExemptionChecker(localDaoSession(), nsiDaoSession(), new TicketCategoryChecker(), Dagger.appComponent().prohibitedTicketTypeForExemptionCategoryRepository(), exemptionRepository());
    }

    NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    TicketTypeRepository ticketTypeRepository() {
        return Dagger.appComponent().ticketTypeRepository();
    }

    ExemptionRepository exemptionRepository(){
        return Dagger.appComponent().exemptionRepository();
    }

}
