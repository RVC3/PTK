package ru.ppr.cppk.logic;

import android.text.TextUtils;

import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.helpers.CashierSessionInfo;
import ru.ppr.cppk.managers.PrinterManager;

/**
 * Логика проверки номера ЭКЛЗ.
 * Реализация для Зебры.
 *
 * @author Aleksandr Brazhkin
 */
public class ZebraEklzChecker implements EklzChecker {
    /**
     * Информация о текущем кассире.
     */
    private final CashierSessionInfo cashierSessionInfo;
    /**
     * Менеджер для работы с принтером.
     */
    private final PrinterManager printerManager;

    public ZebraEklzChecker(CashierSessionInfo cashierSessionInfo, PrinterManager printerManager) {
        this.cashierSessionInfo = cashierSessionInfo;
        this.printerManager = printerManager;
    }

    @Override
    public boolean check(String printerEKLZNumber, String printerSerialNumber) {
        String ptkEKLZNumber = null;
        String ptkSerialNumber = null;
        Cashier cashier = cashierSessionInfo.getCurrentCashier();
        if (cashier != null) {
            CashRegister cashRegister = printerManager.getCashRegister();
            if (cashRegister != null) {
                ptkEKLZNumber = cashRegister.getEKLZNumber();
                ptkSerialNumber = cashRegister.getSerialNumber();
            }
        }
        if (ptkEKLZNumber != null) {
            if (TextUtils.equals(ptkSerialNumber, printerSerialNumber) && !TextUtils.equals(ptkEKLZNumber, printerEKLZNumber)) {
                return false;
            }
        }
        return true;
    }
}
