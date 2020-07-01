package ru.ppr.cppk.entity.utils.builders.events;

import android.support.annotation.NonNull;

import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.nsi.entity.TrainCategory;

/**
 * Created by Артем on 22.12.2015.
 */
public class TrainInfoGenerator extends AbstractGenerator implements Generator<TrainInfo> {

    private TrainCategory trainCategory;

    public TrainInfoGenerator setTrainCategory(TrainCategory trainCategory) {
        this.trainCategory = trainCategory;
        return this;
    }

    @NonNull
    @Override
    public TrainInfo build() {

        checkNotNull(trainCategory, "TrainCategory is null");

        TrainInfo trainInfo = new TrainInfo();
        trainInfo.setCarClass(null);//должен быть null: https://aj.srvdev.ru/browse/CPPKPP-24747
        trainInfo.setTrainCategory(trainCategory.category);
        trainInfo.setTrainCategoryCode(trainCategory.code);

        return trainInfo;
    }
}
