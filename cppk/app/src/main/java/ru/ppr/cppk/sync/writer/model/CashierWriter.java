package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.Cashier;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class CashierWriter extends BaseWriter<Cashier> {

    @Override
    public void writeProperties(Cashier cashier, ExportJsonWriter writer) throws IOException {
        writer.name("UserLogin").value(cashier.userLogin);
        writer.name("OfficialCode").value(cashier.officialCode);
        writer.name("Fio").value(cashier.fio);
    }

}
