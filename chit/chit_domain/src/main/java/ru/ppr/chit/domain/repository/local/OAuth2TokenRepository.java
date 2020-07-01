package ru.ppr.chit.domain.repository.local;

import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.OAuth2Token;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * Репозиторий данных о токене авторизации
 *
 * @author Dmitry Nevolin
 */
public interface OAuth2TokenRepository extends CrudLocalDbRepository<OAuth2Token, Long> {

    @Nullable
    OAuth2Token loadFirst();

}
