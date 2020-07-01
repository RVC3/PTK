package ru.ppr.cppk.ui.activity.resultBarcodeCoupon;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.security.SecurityDaoSession;

/**
 * @author Dmitry Nevolin
 */
class ResultBarcodeCouponDi {

    private final Di di;

    ResultBarcodeCouponDi(Di di) {
        this.di = di;
    }

    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    SecurityDaoSession securityDaoSession() {
        return di.getDbManager().getSecurityDaoSession().get();
    }

    CommonSettings commonSettings() {
        return Dagger.appComponent().commonSettingsStorage().get();
    }

    PrivateSettings privateSettings() {
        return di.getPrivateSettings().get();
    }

    UiThread uiThread() {
        return di.uiThread();
    }

    NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    PdSaleEnvFactory pdSaleEnvFactory() {
        return Dagger.appComponent().pdSaleEnvFactory();
    }

    StationRepository stationRepository() {
        return Dagger.appComponent().stationRepository();
    }

    PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder(){
        return Dagger.appComponent().pdSaleRestrictionsParamsBuilder();
    }

}
