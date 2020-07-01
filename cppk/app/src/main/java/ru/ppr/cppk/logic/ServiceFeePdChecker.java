package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.cppk.dataCarrier.entity.PD;

/**
 * Класс, осуществляющий проверку является ли ПД услугой
 *
 * @author Dmitry Nevolin
 */
public class ServiceFeePdChecker {

    @Inject
    public ServiceFeePdChecker() {

    }

    @Deprecated
    public boolean check(@NonNull PD pd) {
        return pd.versionPD != null && pd.versionPD == PdVersion.V21.getCode();
    }

    public boolean check2(@NonNull Pd pd) {
        return pd.getVersion() != null && pd.getVersion() == PdVersion.V21;
    }

}
