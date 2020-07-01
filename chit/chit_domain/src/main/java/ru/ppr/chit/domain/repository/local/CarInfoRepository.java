package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.chit.domain.model.local.CarInfo;
import ru.ppr.chit.domain.repository.local.base.RudLocalDbRepository;

/**
 * Репозиторий информации о вагоне
 *
 * @author Dmitry Nevolin
 */
public interface CarInfoRepository extends RudLocalDbRepository<CarInfo, Long> {

    @NonNull
    List<CarInfo> loadAllByTrainInfo(@NonNull Long trainInfoId);

    long insert(@NonNull CarInfo carInfo, @NonNull Long trainInfoId);

    void insertAll(@NonNull List<CarInfo> modelList, @NonNull Long trainInfoId);

}
