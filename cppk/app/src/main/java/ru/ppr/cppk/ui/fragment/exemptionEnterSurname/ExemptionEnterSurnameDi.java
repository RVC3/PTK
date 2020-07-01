package ru.ppr.cppk.ui.fragment.exemptionEnterSurname;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.core.logic.FioNormalizer;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;

/**
 * @author Aleksandr Brazhkin
 */
class ExemptionEnterSurnameDi {

    private final Di di;

    ExemptionEnterSurnameDi(Di di) {
        this.di = di;
    }

    UiThread uiThread() {
        return di.uiThread();
    }

    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    ExemptionGroupRepository exemptionGroupRepository() {
        return Dagger.appComponent().exemptionGroupRepository();
    }

    ExemptionRepository exemptionRepository() {
        return Dagger.appComponent().exemptionRepository();
    }

    FioNormalizer fioNormalizer() {
        return Dagger.appComponent().fioNormalizer();
    }

}
