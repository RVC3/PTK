package ru.ppr.cppk.ui.activity.nsiQueryTest;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.TariffRepository;

/**
 * @author Dmitry Nevolin
 */
class NsiQueryTestDi {

    private final Di di;

    NsiQueryTestDi(Di di) {
        this.di = di;
    }

    PdSaleEnvFactory pdSaleEnvFactory() {
        return Dagger.appComponent().pdSaleEnvFactory();
    }

    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    UiThread uiThread() {
        return di.uiThread();
    }

    NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    TariffRepository tariffRepository() {
        return Dagger.appComponent().tariffRepository();
    }

    StationRepository stationRepository() {
        return Dagger.appComponent().stationRepository();
    }

}
