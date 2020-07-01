package ru.ppr.chit.domain.repository.local;

import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * Репозиторий станций контроля
 *
 * @author Dmitry Nevolin
 */
public interface ControlStationRepository extends CrudLocalDbRepository<ControlStation, Long> {

    @Nullable
    ControlStation loadFirst();

}
