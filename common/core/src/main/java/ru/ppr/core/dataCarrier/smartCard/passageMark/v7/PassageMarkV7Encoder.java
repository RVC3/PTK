package ru.ppr.core.dataCarrier.smartCard.passageMark.v7;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;

/**
 * Энкодер метки прохода v.7.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV7Encoder implements PassageMarkEncoder {

    @NonNull
    @Override
    public byte[] encode(PassageMark passageMark) {

        byte[] data = new byte[PassageMarkV7Structure.PASSAGE_MARK_SIZE];
        data[0] = (byte) passageMark.getVersion().getCode();

        PassageMarkV7 passageMarkV7 = (PassageMarkV7) passageMark;

        int usageCounterValue = passageMarkV7.getUsageCounterValue();
        DataCarrierUtils.writeInt(
                usageCounterValue,
                data,
                PassageMarkV7Structure.COUNTER_VALUE_BYTE_INDEX,
                PassageMarkV7Structure.COUNTER_VALUE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int turnstileNumber = passageMarkV7.getTurnstileNumber();
        data[PassageMarkV7Structure.TURNSTILE_NUMBER_BYTE_INDEX] = (byte) turnstileNumber;

        long passageStationCode = passageMarkV7.getPassageStationCode();
        DataCarrierUtils.writeLong(
                passageStationCode,
                data,
                PassageMarkV7Structure.PASSAGE_STATION_BYTE_INDEX,
                PassageMarkV7Structure.PASSAGE_STATION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int passageTime = passageMarkV7.getPassageTime();
        DataCarrierUtils.writeInt(
                passageTime,
                data,
                PassageMarkV7Structure.PASSAGE_TIME_BYTE_INDEX,
                PassageMarkV7Structure.PASSAGE_TIME_BIT_INDEX,
                PassageMarkV7Structure.PASSAGE_TIME_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean passageType = passageMarkV7.getPassageType() == PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION;
        DataCarrierUtils.writeBoolean(
                passageType,
                data,
                PassageMarkV7Structure.PASSAGE_TYPE_BYTE_INDEX,
                PassageMarkV7Structure.PASSAGE_TYPE_BIT_INDEX
        );

        int coverageAreaNumber = passageMarkV7.getCoverageAreaNumber();
        DataCarrierUtils.writeInt(
                coverageAreaNumber,
                data,
                PassageMarkV7Structure.COVERAGE_AREA_NUMBER_BYTE_INDEX,
                PassageMarkV7Structure.COVERAGE_AREA_NUMBER_BIT_INDEX,
                PassageMarkV7Structure.COVERAGE_AREA_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
