package ru.ppr.cppk.logic.pd;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Класс, выполняюший проверку возможности продажи ПД на ПТК.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleSupportedChecker {

    @Inject
    PdSaleSupportedChecker(){

    }

    /**
     * Проверяет, возможна ли продажа ПД данной версии на ПТК.
     *
     * @param pdVersion Версия ПД
     * @return {@code true} если продажа возмлжна, {@code false} иначе
     */
    public boolean isSaleSupported(@NonNull PdVersion pdVersion) {
        return pdVersion == PdVersion.V1
                || pdVersion == PdVersion.V2
                || pdVersion == PdVersion.V3
                || pdVersion == PdVersion.V5
                || pdVersion == PdVersion.V11
                || pdVersion == PdVersion.V13;
    }
}
