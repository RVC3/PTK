package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.SmartCard;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Aleksandr Brazhkin
 */
public class SmartCardWriter extends BaseWriter<SmartCard> {

    private final ParentTicketInfoWriter parentTicketInfoWriter;

    public SmartCardWriter(DateFormatter dateFormatter) {
        parentTicketInfoWriter = new ParentTicketInfoWriter(dateFormatter);
    }

    @Override
    public void writeProperties(SmartCard smartCard, ExportJsonWriter writer) throws IOException {
        writer.name("OuterNumber").value(smartCard.OuterNumber);
        writer.name("CrystalSerialNumber").value(smartCard.CrystalSerialNumber);
        writer.name("Type").value(smartCard.Type);
        writer.name("Issuer").value(smartCard.Issuer);
        writer.name("UsageCount").value(smartCard.UsageCount);
        writer.name("Track").value(smartCard.Track);
        parentTicketInfoWriter.writeField("PresentTicket1", smartCard.PresentTicket1, writer);
        parentTicketInfoWriter.writeField("PresentTicket2", smartCard.PresentTicket2, writer);
    }

}
