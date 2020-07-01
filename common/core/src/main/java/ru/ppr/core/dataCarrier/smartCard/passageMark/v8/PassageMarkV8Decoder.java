package ru.ppr.core.dataCarrier.smartCard.passageMark.v8;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;

/**
 * Декодер метки прохода версии 8.
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkV8Decoder implements PassageMarkDecoder {

    @Nullable
    @Override
    public PassageMarkV8 decode(@NonNull byte[] data) {

        if (data.length < PassageMarkV8Structure.PASSAGE_MARK_SIZE)
            return null;

        byte[] counterValueData = DataCarrierUtils.subArray(data, PassageMarkV8Structure.COUNTER_VALUE_BYTE_INDEX, PassageMarkV8Structure.COUNTER_VALUE_BYTE_LENGTH);
        int counterValue = DataCarrierUtils.bytesToInt(counterValueData, ByteOrder.LITTLE_ENDIAN);

        byte pd1TurnstileNumberData = data[PassageMarkV8Structure.PD1_TURNSTILE_NUMBER_BYTE_INDEX];
        int pd1TurnstileNumber = DataCarrierUtils.byteToInt(pd1TurnstileNumberData);

        byte pd2TurnstileNumberData = data[PassageMarkV8Structure.PD2_TURNSTILE_NUMBER_BYTE_INDEX];
        int pd2TurnstileNumber = DataCarrierUtils.byteToInt(pd2TurnstileNumberData);

        byte[] passageStationCodeData = DataCarrierUtils.subArray(data, PassageMarkV8Structure.PASSAGE_STATION_BYTE_INDEX, PassageMarkV8Structure.PASSAGE_STATION_BYTE_LENGTH);
        int passageStationCode = DataCarrierUtils.bytesToInt(passageStationCodeData, ByteOrder.LITTLE_ENDIAN);

        byte passageTypeForPd1Data = data[PassageMarkV8Structure.PASSAGE_TYPE_FOR_PD1_BYTE_INDEX];
        boolean passageTypeForPd1 = DataCarrierUtils.byteToBoolean(passageTypeForPd1Data, PassageMarkV8Structure.PASSAGE_TYPE_FOR_PD1_BIT_INDEX);

        byte passageStatusForPd1Data = data[PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD1_BYTE_INDEX];
        boolean passageStatusForPd1 = DataCarrierUtils.byteToBoolean(passageStatusForPd1Data, PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD1_BIT_INDEX);

        byte[] pd1PassageTimeData = DataCarrierUtils.subArray(data, PassageMarkV8Structure.PD1_PASSAGE_TIME_BYTE_INDEX, PassageMarkV8Structure.PD1_PASSAGE_TIME_BYTE_LENGTH);
        int pd1PassageTime = DataCarrierUtils.bytesToInt(pd1PassageTimeData, PassageMarkV8Structure.PD1_PASSAGE_TIME_BIT_INDEX, PassageMarkV8Structure.PD1_PASSAGE_TIME_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte passageTypeForPd2Data = data[PassageMarkV8Structure.PASSAGE_TYPE_FOR_PD2_BYTE_INDEX];
        boolean passageTypeForPd2 = DataCarrierUtils.byteToBoolean(passageTypeForPd2Data, PassageMarkV8Structure.PASSAGE_TYPE_FOR_PD2_BIT_INDEX);

        byte passageStatusForPd2Data = data[PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD2_BYTE_INDEX];
        boolean passageStatusForPd2 = DataCarrierUtils.byteToBoolean(passageStatusForPd2Data, PassageMarkV8Structure.PASSAGE_STATUS_FOR_PD2_BIT_INDEX);

        byte[] pd2PassageTimeData = DataCarrierUtils.subArray(data, PassageMarkV8Structure.PD2_PASSAGE_TIME_BYTE_INDEX, PassageMarkV8Structure.PD2_PASSAGE_TIME_BYTE_LENGTH);
        int pd2PassageTime = DataCarrierUtils.bytesToInt(pd2PassageTimeData, PassageMarkV8Structure.PD2_PASSAGE_TIME_BIT_INDEX, PassageMarkV8Structure.PD2_PASSAGE_TIME_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte boundToPassengerData = data[PassageMarkV8Structure.BOUND_TO_PASSENGER_BYTE_INDEX];
        boolean boundToPassenger = DataCarrierUtils.byteToBoolean(boundToPassengerData, PassageMarkV8Structure.BOUND_TO_PASSENGER_BIT_INDEX);

        PassageMarkV8Impl passageMarkV8 = new PassageMarkV8Impl();
        passageMarkV8.setUsageCounterValue(counterValue);
        passageMarkV8.setPd1TurnstileNumber(pd1TurnstileNumber);
        passageMarkV8.setPd2TurnstileNumber(pd2TurnstileNumber);
        passageMarkV8.setPassageStationCode(passageStationCode);
        passageMarkV8.setPassageTypeForPd1(passageTypeForPd1 ? PassageMarkWithFlags.PASSAGE_TYPE_FROM_STATION : PassageMarkWithFlags.PASSAGE_TYPE_TO_STATION);
        passageMarkV8.setPassageStatusForPd1(passageStatusForPd1 ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV8.setPd1PassageTime(pd1PassageTime);
        passageMarkV8.setPassageTypeForPd2(passageTypeForPd2 ? PassageMarkWithFlags.PASSAGE_TYPE_FROM_STATION : PassageMarkWithFlags.PASSAGE_TYPE_TO_STATION);
        passageMarkV8.setPassageStatusForPd2(passageStatusForPd2 ? PassageMarkWithFlags.PASSAGE_STATUS_EXISTS : PassageMarkWithFlags.PASSAGE_STATUS_NO_EXISTS);
        passageMarkV8.setPd2PassageTime(pd2PassageTime);
        passageMarkV8.setBoundToPassenger(boundToPassenger);

        return passageMarkV8;
    }

}
