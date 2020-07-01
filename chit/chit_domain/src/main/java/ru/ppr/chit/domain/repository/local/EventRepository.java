package ru.ppr.chit.domain.repository.local;

import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * @author Dmitry Nevolin
 */
public interface EventRepository extends CrudLocalDbRepository<Event, Long> {


}
