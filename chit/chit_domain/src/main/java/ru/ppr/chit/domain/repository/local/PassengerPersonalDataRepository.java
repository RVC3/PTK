package ru.ppr.chit.domain.repository.local;

import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.chit.domain.model.local.PassengerPersonalData;
import ru.ppr.chit.domain.model.local.PassengerWithTicketId;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * @author Dmitry Nevolin
 */
public interface PassengerPersonalDataRepository extends CrudLocalDbRepository<PassengerPersonalData, Long> {

    List<PassengerWithTicketId> loadPassengersWithTicketId(@Nullable String fioFilter, @Nullable String documentNumberFilter,
                                                           int recordsOffset, int pageLimit, long currentStationCode);

}
