package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.Exemption;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class ExemptionWriter extends BaseWriter<Exemption> {

    private final SmartCardWriter smartCardWriter;

    public ExemptionWriter(DateFormatter dateFormatter) {
        smartCardWriter = new SmartCardWriter(dateFormatter);
    }

    @Override
    public void writeProperties(Exemption exemption, ExportJsonWriter writer) throws IOException {
        writer.name("Fio").value(exemption.Fio);
        writer.name("Code").value(exemption.Code);
        writer.name("RegionOkatoCode").value(exemption.RegionOkatoCode);
        writer.name("LossSum").value(exemption.LossSum);
        smartCardWriter.writeField("SmartCardFromWhichWasReadAboutExemption", exemption.SmartCardFromWhichWasReadAboutExemption, writer);
        writer.name("TypeOfDocumentWhichApproveExemption").value(exemption.TypeOfDocumentWhichApproveExemption);
        writer.name("NumberOfDocumentWhichApproveExemption").value(exemption.NumberOfDocumentWhichApproveExemption);
        writer.name("Organization").value(exemption.Organization);
        writer.name("IsSnilsUsed").value(exemption.IsSnilsUsed);
        writer.name("RequireSocialCard").value(exemption.RequireSocialCard);
    }
}
