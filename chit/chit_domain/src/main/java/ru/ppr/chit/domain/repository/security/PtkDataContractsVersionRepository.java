package ru.ppr.chit.domain.repository.security;

import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.security.PtkDataContractsVersion;
import ru.ppr.chit.domain.repository.security.base.SecurityDbRepository;

/**
 * Репозиторий сущности версии дата контрактов
 *
 * @author Dmitry Nevolin
 */
public interface PtkDataContractsVersionRepository extends SecurityDbRepository {

    @Nullable
    PtkDataContractsVersion loadLast();

}
