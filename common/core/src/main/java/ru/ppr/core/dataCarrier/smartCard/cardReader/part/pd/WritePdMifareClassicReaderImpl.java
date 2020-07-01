package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер, пишущий ПД на смарт-карту Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public class WritePdMifareClassicReaderImpl extends BaseClassicCardReader implements WritePdMifareClassicReader {

    private final PdEncoderFactory pdEncoderFactory;

    public WritePdMifareClassicReaderImpl(IRfid rfid,
                                          CardInfo cardInfo,
                                          StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                          SamAuthorizationStrategy samAuthorizationStrategy,
                                          MifareClassicReader mifareClassicReader,
                                          PdEncoderFactory pdEncoderFactory) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.pdEncoderFactory = pdEncoderFactory;
    }

    @Override
    public WriteCardResult writePd(Pd pd, byte startSectorNumber, byte startBlockNumber) {
        PdEncoder pdEncoder = pdEncoderFactory.create(pd);
        byte[] data = pdEncoder.encode(pd);
        WriteCardResult result = writeBlocks(data, startSectorNumber, startBlockNumber);
        return result;
    }
}
