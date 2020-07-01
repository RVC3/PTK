package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.AdditionalInfoForEtt;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class AdditionalInfoForEttWriter extends BaseWriter<AdditionalInfoForEtt> {

    private final DateFormatter dateFormatter;

    public AdditionalInfoForEttWriter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void writeProperties(AdditionalInfoForEtt additionalInfoForEtt, ExportJsonWriter writer) throws IOException {
        writer.name("PassengerCategory").value(additionalInfoForEtt.PassengerCategory);
        writer.name("IssueDataTime").value(dateFormatter.formatDateForExport(additionalInfoForEtt.IssueDataTime));
        writer.name("IssueUnitCode").value(additionalInfoForEtt.IssueUnitCode);
        writer.name("OwnerOrganizationCode").value(additionalInfoForEtt.OwnerOrganizationCode);
        writer.name("PassengerFio").value(additionalInfoForEtt.PassengerFio);
        writer.name("GuardianFio").value(additionalInfoForEtt.GuardianFio);
        writer.name("SNILS").value(additionalInfoForEtt.SNILS);
    }

}
