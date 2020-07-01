package ru.ppr.cppk.sync.writer.baseEntities;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.cppk.sync.writer.model.CashRegisterWriter;
import ru.ppr.cppk.sync.writer.model.CashierWriter;
import ru.ppr.cppk.sync.writer.model.WorkingShiftWriter;

/**
 * @author Grigoriy Kashka
 */
public class CashRegisterEventWriter extends BaseWriter<CashRegisterEvent> {

    private final CashierWriter cashierWriter;
    private final CashRegisterWriter cashRegisterWriter;
    private final WorkingShiftWriter workingShiftWriter;

    public CashRegisterEventWriter(CashierWriter cashierWriter,
                                   CashRegisterWriter cashRegisterWriter,
                                   WorkingShiftWriter workingShiftWriter) {
        this.cashierWriter = cashierWriter;
        this.cashRegisterWriter = cashRegisterWriter;
        this.workingShiftWriter = workingShiftWriter;
    }

    @Override
    public void writeProperties(CashRegisterEvent cashRegisterEvent, ExportJsonWriter writer) throws IOException {
        cashierWriter.writeField("Cashier", cashRegisterEvent.Cashier, writer);
        cashRegisterWriter.writeField("CashRegister", cashRegisterEvent.CashRegister, writer);
        workingShiftWriter.writeField("WorkingShift", cashRegisterEvent.WorkingShift, writer);
    }
}
