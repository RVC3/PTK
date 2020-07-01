package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoderFactory;
import ru.ppr.rfid.IRfid;

/**
 * Ридер, пишущий метку прохода на смарт-карту Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public class WritePassageMarkMifareUltralightReaderImpl extends BaseCardReader implements WritePassageMarkMifareUltralightReader {

    private final MifareUltralightReader mifareUltralightReader;

    private final PassageMarkEncoderFactory passageMarkEncoderFactory;

    public WritePassageMarkMifareUltralightReaderImpl(IRfid rfid,
                                                      CardInfo cardInfo,
                                                      MifareUltralightReader mifareUltralightReader,
                                                      PassageMarkEncoderFactory passageMarkEncoderFactory) {
        super(rfid, cardInfo);
        this.mifareUltralightReader = mifareUltralightReader;
        this.passageMarkEncoderFactory = passageMarkEncoderFactory;
    }

    @Override
    public WriteCardResult writePassageMark(PassageMark passageMark, byte pageNumber) {
        PassageMarkEncoder passageMarkEncoder = passageMarkEncoderFactory.create(passageMark);
        byte[] data = passageMarkEncoder.encode(passageMark);
        WriteCardResult result = mifareUltralightReader.writeBytes(data, pageNumber);
        return result;
    }
}
