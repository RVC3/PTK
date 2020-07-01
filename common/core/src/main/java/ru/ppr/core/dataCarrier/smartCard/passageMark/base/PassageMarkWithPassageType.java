package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Метка прохода с направлением прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkWithPassageType extends PassageMark {
    /**
     * Последним был вход на станцию
     */
    int PASSAGE_TYPE_TO_STATION = 0;
    /**
     * Последним был вход на станцию
     */
    int PASSAGE_TYPE_FROM_STATION = 1;

    /**
     * Направление прохода по ПД
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PASSAGE_TYPE_TO_STATION,
            PASSAGE_TYPE_FROM_STATION})
    @interface PassageType {
    }
}
