package ru.ppr.chit.domain.repository.nsi.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCV;

/**
 * Репозиторий НСИ .
 *
 * @param <M> Тип модели слоя логики
 * @param <C> Тип поля-кода модели слоя логики
 * @author Aleksandr Brazhkin
 */
public interface CvNsiDbRepository<M extends NsiModelWithCV<C>, C> extends NsiDbRepository {

    @Nullable
    M load(@NonNull C code, int versionId);

    @NonNull
    List<M> loadAll(int versionId);

    @NonNull
    List<M> loadAll(@NonNull List<C> codeList, int versionId);
}
