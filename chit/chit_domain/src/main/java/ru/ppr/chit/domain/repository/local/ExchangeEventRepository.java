package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumSet;

import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * Репозиторий событий обмена данными с БС
 *
 * @author Dmitry Nevolin
 */
public interface ExchangeEventRepository extends CrudLocalDbRepository<ExchangeEvent, Long> {

    @Nullable
    ExchangeEvent loadLast();

    @Nullable
    ExchangeEvent loadLastByTypeSetAndStatusSet(@NonNull EnumSet<ExchangeEvent.Type> typeSet, @NonNull EnumSet<ExchangeEvent.Status> statusSet);

}
