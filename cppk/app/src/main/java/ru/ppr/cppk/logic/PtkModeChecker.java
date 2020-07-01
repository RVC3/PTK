package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.cppk.entity.settings.PrivateSettings;

/**
 * Класс, выполняющий проверку режима работы ПТК.
 *
 * @author Dmitry Nevolin
 */
public class PtkModeChecker {

    private final PrivateSettings privateSettings;

    @Inject
    public PtkModeChecker(@NonNull PrivateSettings privateSettings) {
        this.privateSettings = privateSettings;
    }

    /**
     * Проверяет, работает ли ПТК в режиме контроля ПД в поезде
     */
    public boolean isTrainControlMode() {
        return !isMobileCashRegisterMode() && !isTransferControlMode();
    }

    /**
     * Проверяет, работает ли ПТК в режиме мобильной касссы
     */
    public boolean isMobileCashRegisterMode() {
        return !isTransferControlMode() && privateSettings.isMobileCashRegister();
    }

    /**
     * Проверяет, работает ли ПТК в режиме мобильной касссы на выход
     */
    public boolean isMobileCashRegisterOutputMode() {
        return isMobileCashRegisterMode() && privateSettings.isOutputMode();
    }

    /**
     * Проверяет, работает ли ПТК в режиме мобильной касссы на вход
     */
    public boolean isMobileCashRegisterInputMode() {
        return isMobileCashRegisterMode() && !privateSettings.isOutputMode();
    }

    /**
     * Проверяет, работает ли ПТК в режиме контроля трасфера
     */
    public boolean isTransferControlMode() {
        return privateSettings.isTransferControlMode();
    }

}
