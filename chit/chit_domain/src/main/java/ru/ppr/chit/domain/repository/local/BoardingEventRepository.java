package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * @author Dmitry Nevolin
 */
public interface BoardingEventRepository extends CrudLocalDbRepository<BoardingEvent, Long> {

    @Nullable
    BoardingEvent loadLast();

    @NonNull
    List<BoardingEvent> loadAllForTripService(@NonNull String tripServiceUuid);

    @NonNull
    List<BoardingEvent> loadAllByNotInUuid(@NonNull List<String> boardingEventUuidList);

    @NonNull
    List<BoardingEvent> loadAllExported();

    @NonNull
    List<BoardingEvent> loadAllNotExported();

}
