package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.ParentTicketInfo;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class ParentTicketInfoWriter extends BaseWriter<ParentTicketInfo> {

    private final DateFormatter dateFormatter;

    public ParentTicketInfoWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void writeProperties(ParentTicketInfo parentTicketInfo, ExportJsonWriter writer) throws IOException {
        writer.name("SaleDateTime").value(dateFormatter.formatDateForExport(parentTicketInfo.SaleDateTime));
        writer.name("TicketNumber").value(parentTicketInfo.TicketNumber);
        writer.name("CashRegisterNumber").value(parentTicketInfo.CashRegisterNumber);
        writer.name("WayType").value(parentTicketInfo.WayType);
    }
}
