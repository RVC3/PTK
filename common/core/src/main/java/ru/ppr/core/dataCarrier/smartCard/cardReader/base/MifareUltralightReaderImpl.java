package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;
import android.util.Pair;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.findcardtask.ReadCardErrorTypeMapper;
import ru.ppr.core.dataCarrier.findcardtask.WriteCardErrorTypeMapper;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.entity.BscType;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.RfidResult;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.utils.CommonUtils;

/**
 * Ридер смарт-карт Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public class MifareUltralightReaderImpl extends BaseCardReader implements MifareUltralightReader {

    private static final String TAG = Logger.makeLogTag(MifareUltralightReaderImpl.class);

    private static final byte START_PAGE_NUMBER = 4;
    private static final byte START_BYTE_NUMBER = 0;
    private static final byte LENGTH_IN_BYTES = 16;

    public MifareUltralightReaderImpl(IRfid rfid, CardInfo cardInfo) {
        super(rfid, cardInfo);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBytes(byte startPageNumber, byte startByteNumber, byte byteCount) {
        Logger.trace(TAG, "readBytesWithCardType, cardUid = " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " START");

        ReadCardResult<byte[]> result;

        RfidResult<byte[]> rfidResult = rfid.readFromUltralight(startPageNumber, startByteNumber, byteCount);

        if (rfidResult.isOk()) {
            result = new ReadCardResult<>(rfidResult.getResult());
        } else {
            result = new ReadCardResult<>(ReadCardErrorTypeMapper.map(rfidResult.getErrorType()), rfidResult.getErrorMessage());
        }

        Logger.trace(TAG, "readBytesWithCardType, cardUid = " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " FINISH res: " + result.getDescription());
        return result;
    }

    @NonNull
    @Override
    public WriteCardResult writeBytes(byte[] data, byte startPageNumber) {
        Logger.trace(TAG, "writeBytes, cardUid = " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " START");

        WriteCardResult result;

        WriteToCardResult rfidResult = rfid.writeToUltralight(data, cardInfo.getCardUid(), startPageNumber);

        if (rfidResult.isOk()) {
            result = new WriteCardResult();
        } else {
            result = new WriteCardResult(WriteCardErrorTypeMapper.map(rfidResult));
        }

        Logger.trace(TAG, "writeBytes, cardUid = " + CommonUtils.bytesToHexWithoutSpaces(cardInfo.getCardUid()) + " FINISH res: " + result.getDescription());
        return result;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBytesWithCardType() {
        return readBytes(START_PAGE_NUMBER, START_BYTE_NUMBER, LENGTH_IN_BYTES);
    }

    @NonNull
    @Override
    public ReadCardResult<Pair<BscType, byte[]>> readBscTypeWithRawData() {
        ReadCardResult<byte[]> prevResult = readBytesWithCardType();
        if (prevResult.isSuccess()) {
            byte[] bscTypeBytes = getBytesFromData(prevResult.getData(), TYPE_BSC_START_INDEX, TYPE_BSC_BYTES_COUNT);
            BscType bscType = BscType.getByRawCode(bscTypeBytes[0]);
            Pair<BscType, byte[]> pair = new Pair<>(bscType, prevResult.getData());
            return new ReadCardResult<>(pair, prevResult.getDescription());
        } else {
            return new ReadCardResult<>(prevResult.getReadCardErrorType(), prevResult.getDescription());
        }
    }
}
