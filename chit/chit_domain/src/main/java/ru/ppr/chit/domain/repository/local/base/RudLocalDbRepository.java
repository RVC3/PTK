package ru.ppr.chit.domain.repository.local.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * @author Dmitry Nevolin
 */
public interface RudLocalDbRepository<M extends LocalModelWithId<K>, K> extends LocalDbRepository {

    @Nullable
    M load(@NonNull K key);

    @NonNull
    List<M> loadAll();

    @NonNull
    List<M> loadAll(@NonNull List<K> keyList);

    List<M> loadAllNotIn(@NonNull List<K> keyList);

    void update(@NonNull M model);

    void delete(@NonNull M model);

}
