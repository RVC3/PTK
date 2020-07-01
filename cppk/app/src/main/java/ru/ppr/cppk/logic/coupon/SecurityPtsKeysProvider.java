package ru.ppr.cppk.logic.coupon;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PtsKey;

/**
 * Реализация получения ключей из Security базы
 *
 * @author Dmitry Nevolin
 */
public class SecurityPtsKeysProvider implements PtsKeysProvider {

    private final SecurityDaoSession securityDaoSession;

    public SecurityPtsKeysProvider(SecurityDaoSession securityDaoSession) {
        this.securityDaoSession = securityDaoSession;
    }

    @NonNull
    @Override
    public List<PtsKey> provide(int deviceKey) {
        return securityDaoSession.getPtsKeyDao().loadByDeviceKey(deviceKey);
    }

}
