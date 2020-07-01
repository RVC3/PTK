package ru.ppr.core.dataCarrier.pd.base;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ПД с направлением.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithDirection extends Pd {

    /**
     * Направление
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DIRECTION_THERE,
            DIRECTION_BACK})
    @interface Direction {
    }

    int DIRECTION_THERE = 1;
    int DIRECTION_BACK = 2;

    /**
     * Возвращает направление.
     *
     * @return Направление
     */
    @PdWithDirection.Direction
    int getDirection();
}
