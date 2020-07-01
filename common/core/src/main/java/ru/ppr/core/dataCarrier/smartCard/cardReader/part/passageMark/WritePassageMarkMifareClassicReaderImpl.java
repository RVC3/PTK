package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoderFactory;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер, пишущий метку прохода на смарт-карту Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public class WritePassageMarkMifareClassicReaderImpl extends BaseClassicCardReader implements WritePassageMarkMifareClassicReader {

    private final PassageMarkEncoderFactory passageMarkEncoderFactory;

    public WritePassageMarkMifareClassicReaderImpl(IRfid rfid,
                                                   CardInfo cardInfo,
                                                   StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                                   SamAuthorizationStrategy samAuthorizationStrategy,
                                                   MifareClassicReader mifareClassicReader,
                                                   PassageMarkEncoderFactory passageMarkEncoderFactory) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.passageMarkEncoderFactory = passageMarkEncoderFactory;
    }

    @Override
    public WriteCardResult writePassageMark(PassageMark passageMark, byte sectorNumber, byte blockNumber) {
        PassageMarkEncoder passageMarkEncoder = passageMarkEncoderFactory.create(passageMark);
        byte[] data = passageMarkEncoder.encode(passageMark);
        WriteCardResult result = writeBlocks(data, sectorNumber, blockNumber);
        return result;
    }
}
