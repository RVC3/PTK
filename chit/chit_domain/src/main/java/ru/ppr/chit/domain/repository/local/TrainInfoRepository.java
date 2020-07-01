package ru.ppr.chit.domain.repository.local;

import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.TrainInfo;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * Репозиторий информации о нити поезда
 *
 * @author Dmitry Nevolin
 */
public interface TrainInfoRepository extends CrudLocalDbRepository<TrainInfo, Long> {

    @Nullable
    TrainInfo loadLastNotLegacy();

}
