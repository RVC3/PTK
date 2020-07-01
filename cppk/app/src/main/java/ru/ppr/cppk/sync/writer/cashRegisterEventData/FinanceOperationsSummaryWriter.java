package ru.ppr.cppk.sync.writer.cashRegisterEventData;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.cashRegisterEventData.FinanceOperationsSummary;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class FinanceOperationsSummaryWriter extends BaseWriter<FinanceOperationsSummary> {

    @Override
    public void writeProperties(FinanceOperationsSummary field, ExportJsonWriter writer) throws IOException {
        writer.name("Amount").value(field.amount);
        writer.name("Count").value(field.count);
    }
}
