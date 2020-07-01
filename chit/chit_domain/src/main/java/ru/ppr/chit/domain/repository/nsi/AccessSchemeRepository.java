package ru.ppr.chit.domain.repository.nsi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumSet;
import java.util.List;

import ru.ppr.chit.domain.model.nsi.AccessScheme;
import ru.ppr.chit.domain.model.nsi.TicketStorageType;
import ru.ppr.chit.domain.repository.nsi.base.CvNsiDbRepository;

/**
 * Репозиторий для схем доступа.
 *
 * @author Dmitry Nevolin
 */
public interface AccessSchemeRepository extends CvNsiDbRepository<AccessScheme, Long> {

    /**
     * Загружает все схемы доступа для указанных аргументов
     *
     * @param ticketStorageTypeSet типы носителей ПД
     * @param nsiVersion версия НСИ
     * @return все найденные схемы доступа
     */
    @NonNull
    List<AccessScheme> loadAllByTicketStorageTypeSet(@Nullable EnumSet<TicketStorageType> ticketStorageTypeSet, int nsiVersion);

}
