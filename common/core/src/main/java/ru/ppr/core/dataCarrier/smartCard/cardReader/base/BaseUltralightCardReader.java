package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;
import android.util.Pair;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.entity.BscType;
import ru.ppr.rfid.IRfid;

/**
 * @author Aleksandr Brazhkin
 */
public abstract class BaseUltralightCardReader extends BaseCardReader implements MifareUltralightReader {

    private final MifareUltralightReader mifareUltralightReader;

    public BaseUltralightCardReader(IRfid rfid,
                                    CardInfo cardInfo,
                                    MifareUltralightReader mifareUltralightReader) {
        super(rfid, cardInfo);
        this.mifareUltralightReader = mifareUltralightReader;
    }

    @NonNull
    @Override
    public ReadCardResult<Pair<BscType, byte[]>> readBscTypeWithRawData() {
        return mifareUltralightReader.readBscTypeWithRawData();
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBytes(byte startPageNumber, byte startByteNumber, byte byteCount) {
        return mifareUltralightReader.readBytes(startPageNumber, startByteNumber, byteCount);
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readBytesWithCardType() {
        return mifareUltralightReader.readBytesWithCardType();
    }

    @NonNull
    @Override
    public WriteCardResult writeBytes(byte[] data, byte startPageNumber) {
        return mifareUltralightReader.writeBytes(data, startPageNumber);
    }
}
