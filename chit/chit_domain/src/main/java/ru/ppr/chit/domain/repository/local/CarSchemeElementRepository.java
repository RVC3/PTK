package ru.ppr.chit.domain.repository.local;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.chit.domain.model.local.CarSchemeElement;
import ru.ppr.chit.domain.repository.local.base.RudLocalDbRepository;

/**
 * Репозиторий элементов схем вагона
 *
 * @author Dmitry Nevolin
 */
public interface CarSchemeElementRepository extends RudLocalDbRepository<CarSchemeElement, Long> {

    @NonNull
    List<CarSchemeElement> loadAllByCarScheme(@NonNull Long carSchemeId);

    long insert(@NonNull CarSchemeElement carSchemeElement, @NonNull Long carSchemeId);

    void insertAll(@NonNull List<CarSchemeElement> modelList, @NonNull Long carSchemeId);

}
