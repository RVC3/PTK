package ru.ppr.cppk.ui.activity.base.readBarcode;

import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTaskFactory;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.coupon.CouponChecker;
import ru.ppr.cppk.logic.coupon.PtsKeysProvider;
import ru.ppr.cppk.logic.coupon.ReadCouponFromBarcodeHandler;
import ru.ppr.cppk.logic.coupon.SecurityPtsKeysProvider;
import ru.ppr.cppk.logic.pd.PdHandler;
import ru.ppr.kuznyechik.CppcryptoKuznyechik;
import ru.ppr.kuznyechik.Kuznyechik;

/**
 * @author Aleksandr Brazhkin
 */
class BaseReadBarcodeDi {

    private final Di di;

    BaseReadBarcodeDi(Di di) {
        this.di = di;
    }

    ReadBarcodeTaskFactory readBarcodeTaskFactory(){
        return Dagger.appComponent().readBarcodeTaskFactory();
    }

    PdHandler pdHandler() {
        return Dagger.appComponent().pdHandler();
    }

    private LocalDaoSession localDaoSession() {
        return Dagger.appComponent().localDaoSession();
    }

    private Kuznyechik kuznyechik() {
        return new CppcryptoKuznyechik();
    }

    private PtsKeysProvider ptsKeysProvider() {
        return new SecurityPtsKeysProvider(Dagger.appComponent().securityDaoSession());
    }

    private CouponChecker couponChecker() {
        return new CouponChecker(ptsKeysProvider(), kuznyechik());
    }

    ReadCouponFromBarcodeHandler readCouponFromBarcodeHandler() {
        return new ReadCouponFromBarcodeHandler(
                couponChecker(),
                localDaoSession(),
                Dagger.appComponent().shiftManager(),
                Dagger.appComponent().eventBuilder());
    }

    UiThread uiThread() {
        return Dagger.appComponent().uiThread();
    }
}
