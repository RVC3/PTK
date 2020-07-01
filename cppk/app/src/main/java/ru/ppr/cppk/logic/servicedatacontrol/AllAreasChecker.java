package ru.ppr.cppk.logic.servicedatacontrol;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.coverageAreaT1.CoverageAreaT1;

/**
 * Валидатор действия СТУ на все зоны.
 *
 * @author Aleksandr Brazhkin
 */
public class AllAreasChecker {

    @Inject
    AllAreasChecker() {

    }

    /**
     * Проверяет, действует ли СТУ на все зоны
     *
     * @param coverageAreaList Зоны действия СТУ
     * @return {@code true} если действует на все зоны, {@code false} - иначе
     */
    public boolean isForAllAreas(@NonNull List<CoverageArea> coverageAreaList) {
        for (CoverageArea coverageArea : coverageAreaList) {
            if (coverageArea instanceof CoverageAreaT1) {
                return true;
            }
        }
        return false;
    }

}
