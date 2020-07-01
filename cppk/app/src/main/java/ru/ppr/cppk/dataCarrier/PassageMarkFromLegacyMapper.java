package ru.ppr.cppk.dataCarrier;

import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4Impl;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5Impl;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8Impl;

/**
 * Маппер старых сущностей метки прохода в новые.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkFromLegacyMapper {

    public PassageMarkFromLegacyMapper() {

    }

    public PassageMark fromLegacyPassageMark(ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark) {
        PassageMarkVersion passageMarkVersion = PassageMarkVersion.getByCode(legacyPassageMark.getVersionMark());

        if (passageMarkVersion == null){
            return null;
        }

        switch (passageMarkVersion) {
            case V4:
                return fromLegacyPassageMarkV4(legacyPassageMark);
            case V5:
                return fromLegacyPassageMarkV5(legacyPassageMark);
            case V8:
                return fromLegacyPassageMarkV8(legacyPassageMark);
            default:
                return null;
        }
    }

    private PassageMarkV4 fromLegacyPassageMarkV4(ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark) {
        PassageMarkV4Impl passageMarkV4 = new PassageMarkV4Impl();
        passageMarkV4.setUsageCounterValue(legacyPassageMark.getCounterCard());
        passageMarkV4.setPd1TurnstileNumber(legacyPassageMark.getTurniketForOnePd());
        passageMarkV4.setPd2TurnstileNumber(legacyPassageMark.getTurniketForTwoPd());
        passageMarkV4.setPassageStationCode(legacyPassageMark.getStation());
        passageMarkV4.setPassageTypeForPd1(legacyPassageMark.getDirectionForOne() == 1 ? PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION : PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
        passageMarkV4.setPassageTypeForPd2(legacyPassageMark.getDirectionTwo() == 1 ? PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION : PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
        passageMarkV4.setPassageStatusForPd1(legacyPassageMark.getUseOne() ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV4.setPassageStatusForPd2(legacyPassageMark.getUseTwo() ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV4.setPd1PassageTime((int) (long) legacyPassageMark.getSecondsFromSalePd1());
        passageMarkV4.setPd2PassageTime((int) (long) legacyPassageMark.getSecondsFromSalePd2());
        return passageMarkV4;
    }

    private PassageMarkV5 fromLegacyPassageMarkV5(ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark) {
        PassageMarkV5Impl passageMarkV5 = new PassageMarkV5Impl();
        passageMarkV5.setHwCounterValue(legacyPassageMark.getCounterCard());
        passageMarkV5.setPd1TurnstileNumber(legacyPassageMark.getTurniketForOnePd());
        passageMarkV5.setPd2TurnstileNumber(legacyPassageMark.getTurniketForTwoPd());
        passageMarkV5.setPassageStationCode(legacyPassageMark.getStation());
        passageMarkV5.setPassageTypeForPd1(legacyPassageMark.getDirectionForOne() == 1 ? PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION : PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
        passageMarkV5.setPassageTypeForPd2(legacyPassageMark.getDirectionTwo() == 1 ? PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION : PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
        passageMarkV5.setPassageStatusForPd1(legacyPassageMark.getUseOne() ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV5.setPassageStatusForPd2(legacyPassageMark.getUseTwo() ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV5.setPd1PassageTime((int) (long) legacyPassageMark.getSecondsFromSalePd1());
        passageMarkV5.setPd2PassageTime((int) (long) legacyPassageMark.getSecondsFromSalePd2());
        return passageMarkV5;
    }

    private PassageMarkV8 fromLegacyPassageMarkV8(ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark) {
        PassageMarkV8Impl passageMarkV8 = new PassageMarkV8Impl();
        passageMarkV8.setUsageCounterValue(legacyPassageMark.getCounterCard());
        passageMarkV8.setPd1TurnstileNumber(legacyPassageMark.getTurniketForOnePd());
        passageMarkV8.setPd2TurnstileNumber(legacyPassageMark.getTurniketForTwoPd());
        passageMarkV8.setPassageStationCode(legacyPassageMark.getStation());
        passageMarkV8.setPassageTypeForPd1(legacyPassageMark.getDirectionForOne() == 1 ? PassageMarkWithFlags.PASSAGE_TYPE_FROM_STATION : PassageMarkWithFlags.PASSAGE_TYPE_TO_STATION);
        passageMarkV8.setPassageTypeForPd2(legacyPassageMark.getDirectionTwo() == 1 ? PassageMarkWithFlags.PASSAGE_TYPE_FROM_STATION : PassageMarkWithFlags.PASSAGE_TYPE_TO_STATION);
        passageMarkV8.setPassageStatusForPd1(legacyPassageMark.getUseOne() ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV8.setPassageStatusForPd2(legacyPassageMark.getUseTwo() ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV8.setPd1PassageTime((int) (long) legacyPassageMark.getSecondsFromSalePd1());
        passageMarkV8.setPd2PassageTime((int) (long) legacyPassageMark.getSecondsFromSalePd2());
        passageMarkV8.setBoundToPassenger(legacyPassageMark.getBoundToPassenger());
        return passageMarkV8;
    }

}
