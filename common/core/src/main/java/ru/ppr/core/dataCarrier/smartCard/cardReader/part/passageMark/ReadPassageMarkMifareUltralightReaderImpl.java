package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoderFactory;
import ru.ppr.rfid.IRfid;

/**
 * Ридер, читающий метку прохода со смарт-карт Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadPassageMarkMifareUltralightReaderImpl extends BaseCardReader implements ReadPassageMarkMifareUltralightReader {

    /**
     * Размер метки прохода в байтах
     */
    private final byte PASSAGE_MARK_SIZE = 16;

    private final MifareUltralightReader mifareUltralightReader;

    private final PassageMarkDecoderFactory passageMarkDecoderFactory;

    public ReadPassageMarkMifareUltralightReaderImpl(IRfid rfid,
                                                     CardInfo cardInfo,
                                                     MifareUltralightReader mifareUltralightReader,
                                                     PassageMarkDecoderFactory passageMarkDecoderFactory) {
        super(rfid, cardInfo);
        this.mifareUltralightReader = mifareUltralightReader;
        this.passageMarkDecoderFactory = passageMarkDecoderFactory;
    }

    @Override
    public ReadCardResult<PassageMark> readPassageMark(byte startPageNumber, byte startByteNumber) {
        ReadCardResult<byte[]> rawResult = mifareUltralightReader.readBytes(startPageNumber, startByteNumber, PASSAGE_MARK_SIZE);

        ReadCardResult<PassageMark> result;

        if (rawResult.isSuccess()) {
            PassageMarkDecoder passageMarkDecoder = passageMarkDecoderFactory.create(rawResult.getData());
            PassageMark passageMark = passageMarkDecoder.decode(rawResult.getData());
            result = new ReadCardResult<>(passageMark);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }
}
