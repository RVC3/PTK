package ru.ppr.core.dataCarrier.smartCard.passageMark.v4;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;

/**
 * Энкодер метки прохода v.4.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV4Encoder implements PassageMarkEncoder {

    @NonNull
    @Override
    public byte[] encode(PassageMark passageMark) {

        byte[] data = new byte[PassageMarkV4Structure.PASSAGE_MARK_SIZE];
        data[0] = (byte) passageMark.getVersion().getCode();

        PassageMarkV4 passageMarkV4 = (PassageMarkV4) passageMark;

        int counterValue = passageMarkV4.getUsageCounterValue();
        DataCarrierUtils.writeInt(
                counterValue,
                data,
                PassageMarkV4Structure.COUNTER_VALUE_BYTE_INDEX,
                PassageMarkV4Structure.COUNTER_VALUE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int pd1TurnstileNumber = passageMarkV4.getPd1TurnstileNumber();
        data[PassageMarkV4Structure.PD1_TURNSTILE_NUMBER_BYTE_INDEX] = (byte) pd1TurnstileNumber;

        int pd2TurnstileNumber = passageMarkV4.getPd2TurnstileNumber();
        data[PassageMarkV4Structure.PD2_TURNSTILE_NUMBER_BYTE_INDEX] = (byte) pd2TurnstileNumber;

        long passageStationCode = passageMarkV4.getPassageStationCode();
        DataCarrierUtils.writeLong(
                passageStationCode,
                data,
                PassageMarkV4Structure.PASSAGE_STATION_BYTE_INDEX,
                PassageMarkV4Structure.PASSAGE_STATION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean passageTypeForPd1 = passageMarkV4.getPassageTypeForPd1() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION;
        DataCarrierUtils.writeBoolean(
                passageTypeForPd1,
                data,
                PassageMarkV4Structure.PASSAGE_TYPE_FOR_PD1_BYTE_INDEX,
                PassageMarkV4Structure.PASSAGE_TYPE_FOR_PD1_BIT_INDEX
        );

        boolean passageStatusForPd1 = passageMarkV4.getPassageStatusForPd1() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS;
        DataCarrierUtils.writeBoolean(
                passageStatusForPd1,
                data,
                PassageMarkV4Structure.PASSAGE_STATUS_FOR_PD1_BYTE_INDEX,
                PassageMarkV4Structure.PASSAGE_STATUS_FOR_PD1_BIT_INDEX
        );

        int pd1PassageTime = passageMarkV4.getPd1PassageTime();
        DataCarrierUtils.writeInt(
                pd1PassageTime,
                data,
                PassageMarkV4Structure.PD1_PASSAGE_TIME_BYTE_INDEX,
                PassageMarkV4Structure.PD1_PASSAGE_TIME_BIT_INDEX,
                PassageMarkV4Structure.PD1_PASSAGE_TIME_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean passageTypeForPd2 = passageMarkV4.getPassageTypeForPd2() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION;
        DataCarrierUtils.writeBoolean(
                passageTypeForPd2,
                data,
                PassageMarkV4Structure.PASSAGE_TYPE_FOR_PD2_BYTE_INDEX,
                PassageMarkV4Structure.PASSAGE_TYPE_FOR_PD2_BIT_INDEX
        );

        boolean passageStatusForPd2 = passageMarkV4.getPassageStatusForPd2() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS;
        DataCarrierUtils.writeBoolean(
                passageStatusForPd2,
                data,
                PassageMarkV4Structure.PASSAGE_STATUS_FOR_PD2_BYTE_INDEX,
                PassageMarkV4Structure.PASSAGE_STATUS_FOR_PD2_BIT_INDEX
        );

        int pd2PassageTime = passageMarkV4.getPd2PassageTime();
        DataCarrierUtils.writeInt(
                pd2PassageTime,
                data,
                PassageMarkV4Structure.PD2_PASSAGE_TIME_BYTE_INDEX,
                PassageMarkV4Structure.PD2_PASSAGE_TIME_BIT_INDEX,
                PassageMarkV4Structure.PD2_PASSAGE_TIME_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
