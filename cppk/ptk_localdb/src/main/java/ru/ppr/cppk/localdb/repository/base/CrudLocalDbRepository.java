package ru.ppr.cppk.localdb.repository.base;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Репозиторий для доступа к данным локальной БД с поддержкой следующих базовых операций:
 * - Выборка
 * - Обновление
 * - Удаление
 * - Вставка
 *
 * @author Aleksandr Brazhkin
 */
public interface CrudLocalDbRepository<M extends LocalModelWithId<K>, K> extends RudLocalDbRepository<M, K> {

    long insert(@NonNull M model);

    void insertAll(@NonNull List<M> modelList);

}
