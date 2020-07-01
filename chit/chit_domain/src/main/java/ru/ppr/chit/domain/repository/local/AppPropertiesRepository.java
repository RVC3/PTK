package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.base.LocalDbRepository;

/**
 * @author Aleksandr Brazhkin
 */
public interface AppPropertiesRepository extends LocalDbRepository {
    @NonNull
    AppProperties load();

    // Читает из хранилища отдельный ключ (чтение кешируется)
    @Nullable
    String readKeyValue(@NonNull String key);

    // Сохраняет в хранилище отдельный ключ
    void writeKeyValue(@NonNull String key, @NonNull String value);

    // Удаляет ключ из хранилища
    void deleteKeyValue(@NonNull String key);

    Observable<AppProperties> rxLoad();

    void merge(@NonNull AppProperties appProperties);
}
