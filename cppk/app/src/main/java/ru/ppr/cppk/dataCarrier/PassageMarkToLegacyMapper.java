package ru.ppr.cppk.dataCarrier;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8;

/**
 * Маппер новых сущностей метки прохода в старые.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkToLegacyMapper {

    public PassageMarkToLegacyMapper() {

    }

    public ru.ppr.cppk.dataCarrier.entity.PassageMark toLegacyPassageMark(PassageMark passageMark) {
        switch (passageMark.getVersion()) {
            case V4:
                return toLegacyPassageMarkV4((PassageMarkV4) passageMark);
            case V5:
                return toLegacyPassageMarkV5((PassageMarkV5) passageMark);
            case V8:
                return toLegacyPassageMarkV8((PassageMarkV8) passageMark);
            default:
                return null;
        }
    }

    private ru.ppr.cppk.dataCarrier.entity.PassageMark toLegacyPassageMarkV4(PassageMarkV4 passageMark) {
        ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new ru.ppr.cppk.dataCarrier.entity.PassageMark();
        legacyPassageMark.setVersion(passageMark.getVersion().getCode());
        legacyPassageMark.setCounterCard(passageMark.getUsageCounterValue());
        legacyPassageMark.setTurniketForOnePd(passageMark.getPd1TurnstileNumber());
        legacyPassageMark.setTurniketForTwoPd(passageMark.getPd2TurnstileNumber());
        legacyPassageMark.setStation((int) passageMark.getPassageStationCode());
        legacyPassageMark.setDirectionForOne(passageMark.getPassageTypeForPd1() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION ? 1 : 0);
        legacyPassageMark.setDirectionTwo(passageMark.getPassageTypeForPd2() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION ? 1 : 0);
        legacyPassageMark.setUseOne(passageMark.getPassageStatusForPd1() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS);
        legacyPassageMark.setUseTwo(passageMark.getPassageStatusForPd2() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS);
        legacyPassageMark.setSecondsFromSalePd1((long) passageMark.getPd1PassageTime());
        legacyPassageMark.setSecondsFromSalePd2((long) passageMark.getPd2PassageTime());
        return legacyPassageMark;
    }

    private ru.ppr.cppk.dataCarrier.entity.PassageMark toLegacyPassageMarkV5(PassageMarkV5 passageMark) {
        ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new ru.ppr.cppk.dataCarrier.entity.PassageMark();
        legacyPassageMark.setVersion(passageMark.getVersion().getCode());
        legacyPassageMark.setCounterCard(passageMark.getHwCounterValue());
        legacyPassageMark.setTurniketForOnePd(passageMark.getPd1TurnstileNumber());
        legacyPassageMark.setTurniketForTwoPd(passageMark.getPd2TurnstileNumber());
        legacyPassageMark.setStation((int) passageMark.getPassageStationCode());
        legacyPassageMark.setDirectionForOne(passageMark.getPassageTypeForPd1() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION ? 1 : 0);
        legacyPassageMark.setDirectionTwo(passageMark.getPassageTypeForPd2() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION ? 1 : 0);
        legacyPassageMark.setUseOne(passageMark.getPassageStatusForPd1() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS);
        legacyPassageMark.setUseTwo(passageMark.getPassageStatusForPd2() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS);
        legacyPassageMark.setSecondsFromSalePd1((long) passageMark.getPd1PassageTime());
        legacyPassageMark.setSecondsFromSalePd2((long) passageMark.getPd2PassageTime());
        return legacyPassageMark;
    }

    private ru.ppr.cppk.dataCarrier.entity.PassageMark toLegacyPassageMarkV8(PassageMarkV8 passageMark) {
        ru.ppr.cppk.dataCarrier.entity.PassageMark legacyPassageMark = new ru.ppr.cppk.dataCarrier.entity.PassageMark();
        legacyPassageMark.setVersion(passageMark.getVersion().getCode());
        legacyPassageMark.setCounterCard(passageMark.getUsageCounterValue());
        legacyPassageMark.setTurniketForOnePd(passageMark.getPd1TurnstileNumber());
        legacyPassageMark.setTurniketForTwoPd(passageMark.getPd2TurnstileNumber());
        legacyPassageMark.setStation((int)passageMark.getPassageStationCode());
        legacyPassageMark.setDirectionForOne(passageMark.getPassageTypeForPd1() == PassageMarkWithFlags.PASSAGE_TYPE_FROM_STATION ? 1 : 0);
        legacyPassageMark.setDirectionTwo(passageMark.getPassageTypeForPd2() == PassageMarkWithFlags.PASSAGE_TYPE_FROM_STATION ? 1 : 0);
        legacyPassageMark.setUseOne(passageMark.getPassageStatusForPd1() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS);
        legacyPassageMark.setUseTwo(passageMark.getPassageStatusForPd2() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS);
        legacyPassageMark.setSecondsFromSalePd1((long) passageMark.getPd1PassageTime());
        legacyPassageMark.setSecondsFromSalePd2((long) passageMark.getPd2PassageTime());
        legacyPassageMark.setBoundToPassenger(passageMark.isBoundToPassenger());
        return legacyPassageMark;
    }

}
