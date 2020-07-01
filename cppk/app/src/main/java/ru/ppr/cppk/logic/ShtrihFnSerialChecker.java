package ru.ppr.cppk.logic;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.managers.PrinterManager;

/**
 * Чекер для проверки номера ФН в принтере Штрих.
 */
public class ShtrihFnSerialChecker implements FnSerialChecker {

    private final PrinterManager printerManager;

    public ShtrihFnSerialChecker(PrinterManager printerManager) {
        this.printerManager = printerManager;
    }

    @Override
    public boolean check(@Nullable String fnSerialFromPrinter) {
        String ptkFnSerial = null;
        CashRegister cashRegister = printerManager.getCashRegister();
        if (cashRegister != null) {
            ptkFnSerial = cashRegister.getFNSerial();
        }
        return TextUtils.equals(ptkFnSerial, fnSerialFromPrinter);
    }
}
