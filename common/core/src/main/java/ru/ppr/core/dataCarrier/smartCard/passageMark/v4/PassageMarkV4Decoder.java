package ru.ppr.core.dataCarrier.smartCard.passageMark.v4;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;

/**
 * Декодер метки прохода версии 4.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV4Decoder implements PassageMarkDecoder {

    @Nullable
    @Override
    public PassageMarkV4 decode(@NonNull byte[] data) {

        if (data.length < PassageMarkV4Structure.PASSAGE_MARK_SIZE)
            return null;

        byte[] counterValueData = DataCarrierUtils.subArray(data, PassageMarkV4Structure.COUNTER_VALUE_BYTE_INDEX, PassageMarkV4Structure.COUNTER_VALUE_BYTE_LENGTH);
        int counterValue = DataCarrierUtils.bytesToInt(counterValueData, ByteOrder.LITTLE_ENDIAN);

        byte pd1TurnstileNumberData = data[PassageMarkV4Structure.PD1_TURNSTILE_NUMBER_BYTE_INDEX];
        int pd1TurnstileNumber = DataCarrierUtils.byteToInt(pd1TurnstileNumberData);

        byte pd2TurnstileNumberData = data[PassageMarkV4Structure.PD2_TURNSTILE_NUMBER_BYTE_INDEX];
        int pd2TurnstileNumber = DataCarrierUtils.byteToInt(pd2TurnstileNumberData);

        byte[] passageStationCodeData = DataCarrierUtils.subArray(data, PassageMarkV4Structure.PASSAGE_STATION_BYTE_INDEX, PassageMarkV4Structure.PASSAGE_STATION_BYTE_LENGTH);
        long passageStationCode = DataCarrierUtils.bytesToLong(passageStationCodeData, ByteOrder.LITTLE_ENDIAN);

        byte passageTypeForPd1Data = data[PassageMarkV4Structure.PASSAGE_TYPE_FOR_PD1_BYTE_INDEX];
        boolean passageTypeForPd1 = DataCarrierUtils.byteToBoolean(passageTypeForPd1Data, PassageMarkV4Structure.PASSAGE_TYPE_FOR_PD1_BIT_INDEX);

        byte passageStatusForPd1Data = data[PassageMarkV4Structure.PASSAGE_STATUS_FOR_PD1_BYTE_INDEX];
        boolean passageStatusForPd1 = DataCarrierUtils.byteToBoolean(passageStatusForPd1Data, PassageMarkV4Structure.PASSAGE_STATUS_FOR_PD1_BIT_INDEX);

        byte[] pd1PassageTimeData = DataCarrierUtils.subArray(data, PassageMarkV4Structure.PD1_PASSAGE_TIME_BYTE_INDEX, PassageMarkV4Structure.PD1_PASSAGE_TIME_BYTE_LENGTH);
        int pd1PassageTime = DataCarrierUtils.bytesToInt(pd1PassageTimeData, PassageMarkV4Structure.PD1_PASSAGE_TIME_BIT_INDEX, PassageMarkV4Structure.PD1_PASSAGE_TIME_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte passageTypeForPd2Data = data[PassageMarkV4Structure.PASSAGE_TYPE_FOR_PD2_BYTE_INDEX];
        boolean passageTypeForPd2 = DataCarrierUtils.byteToBoolean(passageTypeForPd2Data, PassageMarkV4Structure.PASSAGE_TYPE_FOR_PD2_BIT_INDEX);

        byte passageStatusForPd2Data = data[PassageMarkV4Structure.PASSAGE_STATUS_FOR_PD2_BYTE_INDEX];
        boolean passageStatusForPd2 = DataCarrierUtils.byteToBoolean(passageStatusForPd2Data, PassageMarkV4Structure.PASSAGE_STATUS_FOR_PD2_BIT_INDEX);

        byte[] pd2PassageTimeData = DataCarrierUtils.subArray(data, PassageMarkV4Structure.PD2_PASSAGE_TIME_BYTE_INDEX, PassageMarkV4Structure.PD2_PASSAGE_TIME_BYTE_LENGTH);
        int pd2PassageTime = DataCarrierUtils.bytesToInt(pd2PassageTimeData, PassageMarkV4Structure.PD2_PASSAGE_TIME_BIT_INDEX, PassageMarkV4Structure.PD2_PASSAGE_TIME_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);


        PassageMarkV4Impl passageMarkV4 = new PassageMarkV4Impl();
        passageMarkV4.setUsageCounterValue(counterValue);
        passageMarkV4.setPd1TurnstileNumber(pd1TurnstileNumber);
        passageMarkV4.setPd2TurnstileNumber(pd2TurnstileNumber);
        passageMarkV4.setPassageStationCode(passageStationCode);
        passageMarkV4.setPassageTypeForPd1(passageTypeForPd1 ? PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION : PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
        passageMarkV4.setPassageStatusForPd1(passageStatusForPd1 ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV4.setPd1PassageTime(pd1PassageTime);
        passageMarkV4.setPassageTypeForPd2(passageTypeForPd2 ? PassageMarkWithPassageType.PASSAGE_TYPE_FROM_STATION : PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
        passageMarkV4.setPassageStatusForPd2(passageStatusForPd2 ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV4.setPd2PassageTime(pd2PassageTime);

        return passageMarkV4;
    }

}
