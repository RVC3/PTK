package ru.ppr.core.dataCarrier.smartCard.passageMark.base;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Метка прохода с флагами прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkWithFlags extends PassageMarkWithPassageType {

    /**
     * Флаг прохода по ПД
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PASSAGE_STATUS_NO_EXISTS,
            PASSAGE_STATUS_EXISTS})
    @interface PassageStatus {
    }

    /**
     * По ПД не было ни одного прохода
     */
    int PASSAGE_STATUS_NO_EXISTS = 0;
    /**
     * По ПД был хот я бы 1 проход
     */
    int PASSAGE_STATUS_EXISTS = 1;

    /**
     * Возвращает флаг прохода по ПД.
     *
     * @param pdIndex Номер ПД, с которым ассоциирован флаг прохода
     * @return Флаг прохода по ПД
     */
    @PassageStatus
    int getPassageStatusForPd(int pdIndex);

    /**
     * Возвращает направление прохода по ПД.
     *
     * @param pdIndex Номер ПД, с которым ассоциирован флаг прохода
     * @return Направление прохода по ПД
     */
    @PassageType
    int getPassageTypeForPd(int pdIndex);
}
