package ru.ppr.chit.domain.repository.local;

import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * Репозиторий данных для первичной авторизации на БС
 *
 * @author Dmitry Nevolin
 */
public interface AuthInfoRepository extends CrudLocalDbRepository<AuthInfo, Long> {

    @Nullable
    AuthInfo loadLast();

}
