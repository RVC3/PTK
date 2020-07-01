package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.chit.domain.model.local.StationInfo;
import ru.ppr.chit.domain.repository.local.base.RudLocalDbRepository;

/**
 * Репозиторий информации о станции в нити поезда
 *
 * @author Dmitry Nevolin
 */
public interface StationInfoRepository extends RudLocalDbRepository<StationInfo, Long> {

    @NonNull
    List<StationInfo> loadAllByTrainInfo(@NonNull Long trainInfoId);

    void insertAll(@NonNull List<StationInfo> modelList, @NonNull Long trainInfoId);

}
