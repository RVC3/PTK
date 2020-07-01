package ru.ppr.chit.domain.repository.local;

import android.support.annotation.Nullable;

import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * Репозиторий пользователей
 *
 * @author Dmitry Nevolin
 */
public interface UserRepository extends CrudLocalDbRepository<User, Long> {

    @Nullable
    User loadLast();

}
