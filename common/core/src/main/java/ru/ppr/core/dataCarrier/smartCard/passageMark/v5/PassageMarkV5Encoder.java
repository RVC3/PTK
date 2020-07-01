package ru.ppr.core.dataCarrier.smartCard.passageMark.v5;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;

/**
 * Энкодер метки прохода v.5.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV5Encoder implements PassageMarkEncoder {

    @NonNull
    @Override
    public byte[] encode(PassageMark passageMark) {

        byte[] data = new byte[PassageMarkV5Structure.PASSAGE_MARK_SIZE];
        data[0] = (byte) passageMark.getVersion().getCode();

        PassageMarkV5 passageMarkV5 = (PassageMarkV5) passageMark;

        int hwCounterValue = passageMarkV5.getHwCounterValue();
        DataCarrierUtils.writeInt(
                hwCounterValue,
                data,
                PassageMarkV5Structure.COUNTER_VALUE_BYTE_INDEX,
                PassageMarkV5Structure.COUNTER_VALUE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int pd1TurnstileNumber = passageMarkV5.getPd1TurnstileNumber();
        data[PassageMarkV5Structure.PD1_TURNSTILE_NUMBER_BYTE_INDEX] = (byte) pd1TurnstileNumber;

        int pd2TurnstileNumber = passageMarkV5.getPd2TurnstileNumber();
        data[PassageMarkV5Structure.PD2_TURNSTILE_NUMBER_BYTE_INDEX] = (byte) pd2TurnstileNumber;

        long passageStationCode = passageMarkV5.getPassageStationCode();
        DataCarrierUtils.writeLong(
                passageStationCode,
                data,
                PassageMarkV5Structure.PASSAGE_STATION_BYTE_INDEX,
                PassageMarkV5Structure.PASSAGE_STATION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean passageTypeForPd1 = passageMarkV5.getPassageTypeForPd1() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION;
        DataCarrierUtils.writeBoolean(
                passageTypeForPd1,
                data,
                PassageMarkV5Structure.PASSAGE_TYPE_FOR_PD1_BYTE_INDEX,
                PassageMarkV5Structure.PASSAGE_TYPE_FOR_PD1_BIT_INDEX
        );

        boolean passageStatusForPd1 = passageMarkV5.getPassageStatusForPd1() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS;
        DataCarrierUtils.writeBoolean(
                passageStatusForPd1,
                data,
                PassageMarkV5Structure.PASSAGE_STATUS_FOR_PD1_BYTE_INDEX,
                PassageMarkV5Structure.PASSAGE_STATUS_FOR_PD1_BIT_INDEX
        );

        int pd1PassageTime = passageMarkV5.getPd1PassageTime();
        DataCarrierUtils.writeInt(
                pd1PassageTime,
                data,
                PassageMarkV5Structure.PD1_PASSAGE_TIME_BYTE_INDEX,
                PassageMarkV5Structure.PD1_PASSAGE_TIME_BIT_INDEX,
                PassageMarkV5Structure.PD1_PASSAGE_TIME_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean passageTypeForPd2 = passageMarkV5.getPassageTypeForPd2() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION;
        DataCarrierUtils.writeBoolean(
                passageTypeForPd2,
                data,
                PassageMarkV5Structure.PASSAGE_TYPE_FOR_PD2_BYTE_INDEX,
                PassageMarkV5Structure.PASSAGE_TYPE_FOR_PD2_BIT_INDEX
        );

        boolean passageStatusForPd2 = passageMarkV5.getPassageStatusForPd2() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS;
        DataCarrierUtils.writeBoolean(
                passageStatusForPd2,
                data,
                PassageMarkV5Structure.PASSAGE_STATUS_FOR_PD2_BYTE_INDEX,
                PassageMarkV5Structure.PASSAGE_STATUS_FOR_PD2_BIT_INDEX
        );

        int pd2PassageTime = passageMarkV5.getPd2PassageTime();
        DataCarrierUtils.writeInt(
                pd2PassageTime,
                data,
                PassageMarkV5Structure.PD2_PASSAGE_TIME_BYTE_INDEX,
                PassageMarkV5Structure.PD2_PASSAGE_TIME_BIT_INDEX,
                PassageMarkV5Structure.PD2_PASSAGE_TIME_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
