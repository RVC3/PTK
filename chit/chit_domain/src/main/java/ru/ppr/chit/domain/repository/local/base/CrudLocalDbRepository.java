package ru.ppr.chit.domain.repository.local.base;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * @author Dmitry Nevolin
 */
public interface CrudLocalDbRepository<M extends LocalModelWithId<K>, K> extends RudLocalDbRepository<M, K> {

    long insert(@NonNull M model);

    void insertAll(List<M> modelList);

}
