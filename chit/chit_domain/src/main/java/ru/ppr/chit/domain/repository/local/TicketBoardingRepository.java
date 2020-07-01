package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * @author Dmitry Nevolin
 */
public interface TicketBoardingRepository extends CrudLocalDbRepository<TicketBoarding, Long> {

    @Nullable
    TicketBoarding loadByTicket(@NonNull Long ticketId);

}
