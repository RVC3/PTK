package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.CashRegister;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class CashRegisterWriter extends BaseWriter<CashRegister> {

    @Override
    public void writeProperties(CashRegister cashRegister, ExportJsonWriter writer) throws IOException {
        writer.name("Model").value(cashRegister.model);
        writer.name("SerialNumber").value(cashRegister.serialNumber);
        writer.name("INN").value(cashRegister.inn);
        writer.name("EKLZNumber").value(cashRegister.eklzNumber);
        writer.name("FNSerial").value(cashRegister.fnSerial);
    }

}
