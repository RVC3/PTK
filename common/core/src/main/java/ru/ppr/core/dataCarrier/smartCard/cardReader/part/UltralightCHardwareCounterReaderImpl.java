package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardErrorType;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseUltralightCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.rfid.IRfid;

/**
 * Ридер, читающий и инкрементрирующий хардварный счетчик на картах Ultralight C.
 *
 * @author Aleksandr Brazhkin
 */
public class UltralightCHardwareCounterReaderImpl extends BaseUltralightCardReader implements HardwareCounterReader {

    private static final byte COUNTER_PAGE_NUMBER = 41;
    private static final byte COUNTER_START_BYTE_NUMBER = 0;
    private static final byte COUNTER_LENGTH_IN_BYTES = 2;

    public UltralightCHardwareCounterReaderImpl(IRfid rfid,
                                                CardInfo cardInfo,
                                                MifareUltralightReader mifareUltralightReader) {
        super(rfid, cardInfo, mifareUltralightReader);
    }

    @NonNull
    @Override
    public ReadCardResult<Integer> readHardwareCounter(int counterIndex) {
        if (counterIndex != 0) {
            return new ReadCardResult<>(ReadCardErrorType.OTHER, "Counter index is out of bounds");
        }

        ReadCardResult<byte[]> rawResult = readBytes(COUNTER_PAGE_NUMBER, COUNTER_START_BYTE_NUMBER, COUNTER_LENGTH_IN_BYTES);

        ReadCardResult<Integer> result;

        if (rawResult.isSuccess()) {
            Integer counter = DataCarrierUtils.bytesToInt(rawResult.getData(), ByteOrder.LITTLE_ENDIAN);
            result = new ReadCardResult<>(counter);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }

    @NonNull
    @Override
    public WriteCardResult incrementHardwareCounter(int counterIndex, int incrementValue) {
        if (counterIndex != 0) {
            return new WriteCardResult("Counter index is out of bounds");
        }
        byte[] incrementValueBytes = new byte[4];
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN).putInt(incrementValue).position(0);
        byteBuffer.get(incrementValueBytes);
        return writeBytes(incrementValueBytes, COUNTER_PAGE_NUMBER);
    }
}
