package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.SeasonTicket;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class SeasonTicketWriter extends BaseWriter<SeasonTicket> {

    @Override
    public void writeProperties(SeasonTicket seasonTicket, ExportJsonWriter writer) throws IOException {
        writer.name("PassCount").value(seasonTicket.PassCount);
        writer.name("PassLeftCount").value(seasonTicket.PassLeftCount);
        writer.name("MonthDays").value(seasonTicket.MonthDays);
    }

}
