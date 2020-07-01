package ru.ppr.cppk.localdb.repository.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Репозиторий для доступа к данным локальной БД с поддержкой следующих базовых операций:
 * - Выборка
 * - Обновление
 * - Удаление
 *
 * @author Aleksandr Brazhkin
 */
public interface RudLocalDbRepository<M extends LocalModelWithId<K>, K> extends LocalDbRepository {

    @Nullable
    M load(@Nullable K key);

    @NonNull
    List<M> loadAll();

    @NonNull
    List<M> loadAll(@NonNull List<K> keyList);

    List<M> loadAllNotIn(@NonNull List<K> keyList);

    void update(@NonNull M model);

    void delete(@NonNull M model);

}
