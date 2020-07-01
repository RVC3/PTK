package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.LegalEntity;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class LegalEntityWriter extends BaseWriter<LegalEntity> {

    @Override
    public void writeProperties(LegalEntity legalEntity, ExportJsonWriter writer) throws IOException {
        writer.name("Code").value(legalEntity.Code);
        writer.name("INN").value(legalEntity.INN);
        writer.name("Name").value(legalEntity.Name, true);
    }

}
