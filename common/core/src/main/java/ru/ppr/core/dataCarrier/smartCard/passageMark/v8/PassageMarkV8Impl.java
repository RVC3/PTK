package ru.ppr.core.dataCarrier.smartCard.passageMark.v8;

import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.BasePassageMarkV4V8;

/**
 * Метка прохода v.8.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV8Impl extends BasePassageMarkV4V8 implements PassageMarkV8 {

    /**
     * Признак привязки БСК к пассажиру
     */
    private boolean boundToPassenger;

    public PassageMarkV8Impl() {
        super(PassageMarkVersion.V8, PassageMarkV8Structure.PASSAGE_MARK_SIZE);
    }

    @Override
    public boolean isBoundToPassenger() {
        return boundToPassenger;
    }

    @Override
    public void setBoundToPassenger(boolean boundToPassenger) {
        this.boundToPassenger = boundToPassenger;
    }

    @Override
    public String toString() {
        return "PassageMarkV8Impl{" +
                "boundToPassenger=" + boundToPassenger +
                ", version=" + getVersion() +
                ", size=" + getSize() +
                ", passageStationCode=" + getPassageStationCode() +
                ", passageStatusForPd1=" + getPassageStatusForPd1() +
                ", passageStatusForPd2=" + getPassageStatusForPd2() +
                ", passageTypeForPd1=" + getPassageTypeForPd1() +
                ", passageTypeForPd2=" + getPassageTypeForPd2() +
                ", pd1PassageTime=" + getPd1PassageTime() +
                ", pd2PassageTime=" + getPd2PassageTime() +
                ", pd1TurnstileNumber=" + getPd1TurnstileNumber() +
                ", pd2TurnstileNumber=" + getPd2TurnstileNumber() +
                ", usageCounterValue=" + getUsageCounterValue() +
                '}';
    }
}
