package ru.ppr.core.dataCarrier.smartCard.passageMark.v7;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;

/**
 * Декодер метки прохода версии 7.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV7Decoder implements PassageMarkDecoder {

    @Nullable
    @Override
    public PassageMarkV7 decode(@NonNull byte[] data) {

        if (data.length < PassageMarkV7Structure.PASSAGE_MARK_SIZE)
            return null;

        byte[] counterValueData = DataCarrierUtils.subArray(data, PassageMarkV7Structure.COUNTER_VALUE_BYTE_INDEX, PassageMarkV7Structure.COUNTER_VALUE_BYTE_LENGTH);
        int counterValue = DataCarrierUtils.bytesToInt(counterValueData, ByteOrder.LITTLE_ENDIAN);

        byte turnstileNumberData = data[PassageMarkV7Structure.TURNSTILE_NUMBER_BYTE_INDEX];
        int turnstileNumber = DataCarrierUtils.byteToInt(turnstileNumberData);

        byte[] passageStationCodeData = DataCarrierUtils.subArray(data, PassageMarkV7Structure.PASSAGE_STATION_BYTE_INDEX, PassageMarkV7Structure.PASSAGE_STATION_BYTE_LENGTH);
        long passageStationCode = DataCarrierUtils.bytesToLong(passageStationCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] passageTimeData = DataCarrierUtils.subArray(data, PassageMarkV7Structure.PASSAGE_TIME_BYTE_INDEX, PassageMarkV7Structure.PASSAGE_TIME_BYTE_LENGTH);
        int passageTime = DataCarrierUtils.bytesToInt(passageTimeData, PassageMarkV7Structure.PASSAGE_TIME_BIT_INDEX, PassageMarkV7Structure.PASSAGE_TIME_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte passageTypeData = data[PassageMarkV7Structure.PASSAGE_TYPE_BYTE_INDEX];
        boolean passageType = DataCarrierUtils.byteToBoolean(passageTypeData, PassageMarkV7Structure.PASSAGE_TYPE_BIT_INDEX);

        byte[] coverageAreaNumberData = DataCarrierUtils.subArray(data, PassageMarkV7Structure.COVERAGE_AREA_NUMBER_BYTE_INDEX, PassageMarkV7Structure.COVERAGE_AREA_NUMBER_BYTE_LENGTH);
        int coverageAreaNumber = DataCarrierUtils.bytesToInt(coverageAreaNumberData, PassageMarkV7Structure.COVERAGE_AREA_NUMBER_BIT_INDEX, PassageMarkV7Structure.COVERAGE_AREA_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        PassageMarkV7Impl passageMarkV7 = new PassageMarkV7Impl();
        passageMarkV7.setUsageCounterValue(counterValue);
        passageMarkV7.setTurnstileNumber(turnstileNumber);
        passageMarkV7.setPassageStationCode(passageStationCode);
        passageMarkV7.setPassageTime(passageTime);
        passageMarkV7.setPassageType(passageType ? PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION : PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
        passageMarkV7.setCoverageAreaNumber(coverageAreaNumber);

        return passageMarkV7;
    }

}
