package ru.ppr.cppk.sync.writer.model;

import java.io.IOException;

import ru.ppr.cppk.sync.kpp.model.TrainInfo;
import ru.ppr.cppk.sync.writer.base.BaseWriter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;

/**
 * @author Grigoriy Kashka
 */
public class TrainInfoWriter extends BaseWriter<TrainInfo> {

    @Override
    public void writeProperties(TrainInfo trainInfo, ExportJsonWriter writer) throws IOException {
        writer.name("TrainCategory").value(trainInfo.TrainCategory);
        writer.name("CarClass").value(trainInfo.CarClass);
        writer.name("TrainCategoryCode").value(trainInfo.TrainCategoryCode);
    }

}
