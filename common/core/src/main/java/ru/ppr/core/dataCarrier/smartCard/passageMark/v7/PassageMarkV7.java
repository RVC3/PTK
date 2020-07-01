package ru.ppr.core.dataCarrier.smartCard.passageMark.v7;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithUsageCounterValue;

/**
 * Метка прохода v.7.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkV7 extends PassageMark,
        PassageMarkWithUsageCounterValue,
        PassageMarkWithPassageType {

    /**
     * Возвращает номер турникета на станции, через который был совершен проход.
     *
     * @return Номер турникета на станции, через который был совершен проход.
     */
    int getTurnstileNumber();

    /**
     * Возвращает дату и время последнего прохода, UTC.
     *
     * @return Дата и время последнего прохода, UTC.
     */
    int getPassageTime();

    /**
     * Возвращает направление прохода.
     *
     * @return Направление прохода
     */
    @PassageType
    int getPassageType();

    /**
     * Возвращает номер зоны (на БСК) который был использован для прохода.
     * При инициализации БСК - 0.
     *
     * @return Номер зоны.
     */
    int getCoverageAreaNumber();
}
