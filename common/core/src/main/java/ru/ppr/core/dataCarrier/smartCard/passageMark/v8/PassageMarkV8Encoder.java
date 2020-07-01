package ru.ppr.core.dataCarrier.smartCard.passageMark.v8;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;

/**
 * Энкодер метки прохода v.8.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV8Encoder implements PassageMarkEncoder {

    @NonNull
    @Override
    public byte[] encode(PassageMark passageMark) {
        byte[] data = new byte[PassageMarkV8Structure.PASSAGE_MARK_SIZE];
        data[0] = (byte) passageMark.getVersion().getCode();

        PassageMarkV8 passageMarkV8 = (PassageMarkV8) passageMark;

        int usageCounterValue = passageMarkV8.getUsageCounterValue();
        DataCarrierUtils.writeInt(
                usageCounterValue,
                data,
                PassageMarkV8Structure.COUNTER_VALUE_BYTE_INDEX,
                PassageMarkV8Structure.COUNTER_VALUE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int pd1TurnstileNumber = passageMarkV8.getPd1TurnstileNumber();
        data[PassageMarkV8Structure.PD1_TURNSTILE_NUMBER_BYTE_INDEX] = (byte) pd1TurnstileNumber;

        int pd2TurnstileNumber = passageMarkV8.getPd2TurnstileNumber();
        data[PassageMarkV8Structure.PD2_TURNSTILE_NUMBER_BYTE_INDEX] = (byte) pd2TurnstileNumber;

        int passageStationCode = (int) passageMarkV8.getPassageStationCode();
        DataCarrierUtils.writeInt(
                passageStationCode,
                data,
                PassageMarkV8Structure.PASSAGE_STATION_BYTE_INDEX,
                PassageMarkV8Structure.PASSAGE_STATION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean passageTypeForPd1 = passageMarkV8.getPassageTypeForPd1() == PassageMarkWithFlags.PASSAGE_TYPE_FROM_STATION;
        DataCarrierUtils.writeBoolean(
                passageTypeForPd1,
                data,
                PassageMarkV8Structure.PASSAGE_TYPE_FOR_PD1_BYTE_INDEX,
                PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD1_BIT_INDEX
        );

        boolean passageStatusForPd1 = passageMarkV8.getPassageStatusForPd1() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS;
        DataCarrierUtils.writeBoolean(
                passageStatusForPd1,
                data,
                PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD1_BYTE_INDEX,
                PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD1_BIT_INDEX
        );

        int pd1PassageTime = passageMarkV8.getPd1PassageTime();
        DataCarrierUtils.writeInt(
                pd1PassageTime,
                data,
                PassageMarkV8Structure.PD1_PASSAGE_TIME_BYTE_INDEX,
                PassageMarkV8Structure.PD1_PASSAGE_TIME_BIT_INDEX,
                PassageMarkV8Structure.PD1_PASSAGE_TIME_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean passageTypeForPd2 = passageMarkV8.getPassageTypeForPd2() == PassageMarkWithFlags.PASSAGE_TYPE_FROM_STATION;
        DataCarrierUtils.writeBoolean(
                passageTypeForPd2,
                data,
                PassageMarkV8Structure.PASSAGE_TYPE_FOR_PD2_BYTE_INDEX,
                PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD2_BIT_INDEX
        );

        boolean passageStatusForPd2 = passageMarkV8.getPassageStatusForPd2() == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS;
        DataCarrierUtils.writeBoolean(
                passageStatusForPd2,
                data,
                PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD2_BYTE_INDEX,
                PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD2_BIT_INDEX
        );

        int pd2PassageTime = passageMarkV8.getPd2PassageTime();
        DataCarrierUtils.writeInt(
                pd2PassageTime,
                data,
                PassageMarkV8Structure.PD2_PASSAGE_TIME_BYTE_INDEX,
                PassageMarkV8Structure.PD2_PASSAGE_TIME_BIT_INDEX,
                PassageMarkV8Structure.PD2_PASSAGE_TIME_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean boundToPassenger = passageMarkV8.isBoundToPassenger();
        DataCarrierUtils.writeBoolean(
                boundToPassenger,
                data,
                PassageMarkV8Structure.BOUND_TO_PASSENGER_BYTE_INDEX,
                PassageMarkV8Structure.BOUND_TO_PASSENGER_BIT_INDEX
        );

        return data;
    }
}
