package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.chit.domain.model.local.TicketId;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * @author Dmitry Nevolin
 */
public interface TicketIdRepository extends CrudLocalDbRepository<TicketId, Long> {

    @Nullable
    TicketId loadByIdentity(long ticketNumber, @NonNull Date saleDate, @NonNull String deviceId);

}
