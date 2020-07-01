package ru.ppr.core.dataCarrier.smartCard.passageMark.v8;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkV4V5V8;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithUsageCounterValue;

/**
 * Метка прохода v.8.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkV8 extends PassageMarkV4V5V8, PassageMarkWithUsageCounterValue {

    /**
     * Признак привязки БСК к пассажиру.
     * ПТК не перезаписывает этот признак, только касса!
     *
     * @return признак привязки БСК к пассажиру
     */
    boolean isBoundToPassenger();

    void setBoundToPassenger(boolean boundToPassenger);

}
