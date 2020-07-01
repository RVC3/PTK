package ru.ppr.core.dataCarrier.smartCard.passageMark.v4;

import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.BasePassageMarkV4V8;

/**
 * Метка прохода v.4.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV4Impl extends BasePassageMarkV4V8 implements PassageMarkV4 {

    public PassageMarkV4Impl() {
        super(PassageMarkVersion.V4, PassageMarkV4Structure.PASSAGE_MARK_SIZE);
    }

    @Override
    public String toString() {
        return "PassageMarkV4Impl{" +
                "version=" + getVersion() +
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
                "} ";
    }
}
