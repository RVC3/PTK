package ru.ppr.cppk.logic.interactor;

import ru.ppr.cppk.logic.ShiftManager;

/**
 * Операция проверки состояния смены в локальной БД ПТК.
 *
 * @author Aleksandr Brazhkin
 */
public class CheckShiftOpenedInDbInteractor {

    private final ShiftManager shiftManager;

    public CheckShiftOpenedInDbInteractor(ShiftManager shiftManager) {
        this.shiftManager = shiftManager;
    }

    public boolean isShiftOpened() {
        return shiftManager.isShiftOpened();
    }
}
