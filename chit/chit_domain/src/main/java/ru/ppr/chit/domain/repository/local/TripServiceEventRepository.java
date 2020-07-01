package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * Репозиторий событий обслуживания поездки
 *
 * @author Dmitry Nevolin
 */
public interface TripServiceEventRepository extends CrudLocalDbRepository<TripServiceEvent, Long> {

    @Nullable
    TripServiceEvent loadLast();

    @Nullable
    TripServiceEvent loadFirstByTripUuid(@NonNull String tripUuid);

}
