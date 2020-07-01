package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * Репозиторий событий контроля билета
 *
 * @author Dmitry Nevolin
 */
public interface TicketControlEventRepository extends CrudLocalDbRepository<TicketControlEvent, Long> {

    @NonNull
    List<TicketControlEvent> loadAllNotExported();

}
