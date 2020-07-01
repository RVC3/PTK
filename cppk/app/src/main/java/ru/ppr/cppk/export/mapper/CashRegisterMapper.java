package ru.ppr.cppk.export.mapper;

import ru.ppr.cppk.localdb.model.CashRegister;

/**
 * @author Grigoriy Kashka
 */
public class CashRegisterMapper {

    public ru.ppr.cppk.sync.kpp.model.CashRegister toExportCashRegister(CashRegister localCashRegister) {
        if (localCashRegister == null)
            return null;
        ru.ppr.cppk.sync.kpp.model.CashRegister cashRegister = new ru.ppr.cppk.sync.kpp.model.CashRegister();
        cashRegister.model = localCashRegister.getModel();
        cashRegister.serialNumber = localCashRegister.getSerialNumber();
        cashRegister.inn = localCashRegister.getINN();
        cashRegister.eklzNumber = localCashRegister.getEKLZNumber();
        cashRegister.fnSerial = localCashRegister.getFNSerial();
        return cashRegister;
    }
}
