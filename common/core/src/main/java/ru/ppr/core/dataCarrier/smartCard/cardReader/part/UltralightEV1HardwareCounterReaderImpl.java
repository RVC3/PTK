package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.findcardtask.ReadCardErrorTypeMapper;
import ru.ppr.core.dataCarrier.findcardtask.WriteCardErrorTypeMapper;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseUltralightCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.RfidResult;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.utils.CommonUtils;

/**
 * Ридер, читающий и инкрементрирующий хардварный счетчик на картах Ultralight EV1.
 *
 * @author Aleksandr Brazhkin
 */
public class UltralightEV1HardwareCounterReaderImpl extends BaseUltralightCardReader implements HardwareCounterReader {

    private static final String TAG = Logger.makeLogTag(UltralightEV1HardwareCounterReaderImpl.class);

    public UltralightEV1HardwareCounterReaderImpl(IRfid rfid,
                                                  CardInfo cardInfo,
                                                  MifareUltralightReader mifareUltralightReader) {
        super(rfid, cardInfo, mifareUltralightReader);
    }

    @NonNull
    @Override
    public ReadCardResult<Integer> readHardwareCounter(int counterIndex) {
        Logger.trace(TAG, "readRawHardwareCounter, cardUid = " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " START");
        ReadCardResult<Integer> result;

        RfidResult<byte[]> rfidResult = rfid.readCounterFromUltralightEV1((byte) counterIndex);

        if (rfidResult.isOk()) {
            Logger.trace(TAG, "readRawHardwareCounter OK - " + CommonUtils.bytesToHexWithoutSpaces(rfidResult.getResult()));
            Integer counter = DataCarrierUtils.bytesToInt(rfidResult.getResult(), ByteOrder.LITTLE_ENDIAN);
            result = new ReadCardResult<>(counter);
        } else {
            result = new ReadCardResult<>(ReadCardErrorTypeMapper.map(rfidResult.getErrorType()), rfidResult.getErrorMessage());
        }

        Logger.trace(TAG, "readRawHardwareCounter, cardUid = " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " FINISH res: " + result.getDescription());

        return result;
    }

    @NonNull
    @Override
    public WriteCardResult incrementHardwareCounter(int counterIndex, int incrementValue) {
        Logger.trace(TAG, "incrementHardwareCounter: cardUID - " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " START");

        WriteCardResult result;

        WriteToCardResult rfidResult = rfid.incrementCounterUltralightEV1(counterIndex, incrementValue, cardInfo.getCardUid());

        if (rfidResult.isOk()) {
            Logger.trace(TAG, "incrementHardwareCounter OK - " + rfidResult);
            result = new WriteCardResult();
        } else {
            result = new WriteCardResult(WriteCardErrorTypeMapper.map(rfidResult));
        }

        Logger.trace(TAG, "incrementHardwareCounter: cardUID - " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " FINISH res: " + result);

        return result;
    }
}
