package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoderFactory;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер, читающий метку прохода со смарт-карт Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadPassageMarkMifareClassicReaderImpl extends BaseClassicCardReader implements ReadPassageMarkMifareClassicReader {

    private final PassageMarkDecoderFactory passageMarkDecoderFactory;

    public ReadPassageMarkMifareClassicReaderImpl(IRfid rfid,
                                                  CardInfo cardInfo,
                                                  StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                                  SamAuthorizationStrategy samAuthorizationStrategy,
                                                  MifareClassicReader mifareClassicReader,
                                                  PassageMarkDecoderFactory passageMarkDecoderFactory) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.passageMarkDecoderFactory = passageMarkDecoderFactory;
    }

    @Override
    public ReadCardResult<PassageMark> readPassageMark(byte sectorNumber, byte blockNumber) {
        ReadCardResult<byte[]> rawResult = readBlock(sectorNumber, blockNumber);

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
