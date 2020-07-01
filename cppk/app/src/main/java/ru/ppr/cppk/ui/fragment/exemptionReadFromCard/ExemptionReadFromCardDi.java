package ru.ppr.cppk.ui.fragment.exemptionReadFromCard;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.core.logic.FioFormatter;
import ru.ppr.cppk.logic.exemptionChecker.ExemptionChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.security.repository.SmartCardStopListItemRepository;

/**
 * @author Aleksandr Brazhkin
 */
class ExemptionReadFromCardDi {

    private final Di di;

    ExemptionReadFromCardDi(Di di) {
        this.di = di;
    }

    UiThread uiThread() {
        return di.uiThread();
    }

    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    SmartCardStopListItemRepository smartCardStopListItemRepository() {
        return Dagger.appComponent().smartCardStopListItemRepository();
    }

    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    PrivateSettings privateSettings() {
        return di.getPrivateSettings().get();
    }

    CommonSettings commonSettings() {
        return Dagger.appComponent().commonSettingsStorage().get();
    }

    ExemptionChecker exemptionChecker() {
        return new ExemptionChecker(localDaoSession(), nsiDaoSession(), new TicketCategoryChecker(), Dagger.appComponent().prohibitedTicketTypeForExemptionCategoryRepository(), exemptionRepository());
    }

    FindCardTaskFactory findCardTaskFactory() {
        return Dagger.appComponent().findCardTaskFactory();
    }

    NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    TicketTypeRepository ticketTypeRepository() {
        return Dagger.appComponent().ticketTypeRepository();
    }

    ExemptionGroupRepository exemptionGroupRepository() {
        return Dagger.appComponent().exemptionGroupRepository();
    }

    SmartCardCancellationReasonRepository smartCardCancellationReasonRepository() {
        return Dagger.appComponent().smartCardCancellationReasonRepository();
    }

    ExemptionRepository exemptionRepository() {
        return Dagger.appComponent().exemptionRepository();
    }

    FioFormatter fioFormatter() {
        return Dagger.appComponent().fioFormatter();
    }

}
