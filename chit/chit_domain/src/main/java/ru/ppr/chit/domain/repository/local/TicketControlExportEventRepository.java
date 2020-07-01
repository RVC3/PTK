package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.chit.domain.model.local.TicketControlExportEvent;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * @author Dmitry Nevolin
 */
public interface TicketControlExportEventRepository extends CrudLocalDbRepository<TicketControlExportEvent, Long> {

    @NonNull
    List<TicketControlExportEvent> loadAllByBoardingEvent(@NonNull Long boardingEventId);

}
