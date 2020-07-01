package ru.ppr.cppk.ui.activity.coupon;

import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.logic.coupon.PtsKeysProvider;
import ru.ppr.cppk.logic.coupon.SecurityPtsKeysProvider;
import ru.ppr.kuznyechik.CppcryptoKuznyechik;
import ru.ppr.kuznyechik.Kuznyechik;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PtsKey;
import ru.ppr.utils.CommonUtils;

/**
 * @author Dmitry Nevolin
 */
class CouponTestDi {

    private final Di di;

    CouponTestDi(Di di) {
        this.di = di;
    }

    PtsKeysProvider ptsKeysProvider() {
        return securityPtsKeysProvider();
    }

    Kuznyechik kuznyechik() {
        return new CppcryptoKuznyechik();
    }

    LongSparseArray<Boolean> testCoupons() {
        LongSparseArray<Boolean> testCoupons = new LongSparseArray<>();

        testCoupons.put(0L, false);
        testCoupons.put(4667193132950768L, true);

        return testCoupons;
    }

    private PtsKeysProvider securityPtsKeysProvider() {
        return new SecurityPtsKeysProvider(di.getDbManager().getSecurityDaoSession().get());
    }

    private PtsKeysProvider testPtsKeysProvider() {
        return (deviceKey) -> {
            List<PtsKey> ptsKeys = new ArrayList<>();

            PtsKey ptsKey = new PtsKey();
            ptsKey.setComplexInstanceId(0);
            ptsKey.setKey(CommonUtils.hexStringToByteArray("00000000000000000000000000000000"));

            PtsKey ptsKey2 = new PtsKey();
            ptsKey2.setComplexInstanceId(1234567890);
            ptsKey2.setKey(CommonUtils.hexStringToByteArray("0123456789abcdeffedcba9876543210"));

            PtsKey ptsKey3 = new PtsKey();
            ptsKey3.setComplexInstanceId(2147483646);
            ptsKey3.setKey(CommonUtils.hexStringToByteArray("0a141e28323c46505a646e78828c96a0"));

            PtsKey ptsKey4 = new PtsKey();
            ptsKey4.setComplexInstanceId(123456789);
            ptsKey4.setKey(CommonUtils.hexStringToByteArray("0123456789abcdeffedcba9876543210"));

            ptsKeys.add(ptsKey);
            ptsKeys.add(ptsKey2);
            ptsKeys.add(ptsKey3);
            ptsKeys.add(ptsKey4);

            return ptsKeys;
        };
    }

}
