package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.rfid.IRfid;

/**
 * Ридер, пишущий ПД на смарт-карту Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public class WritePdMifareUltralightReaderImpl extends BaseCardReader implements WritePdMifareUltralightReader {

    private final MifareUltralightReader mifareUltralightReader;

    private final PdEncoderFactory pdEncoderFactory;

    public WritePdMifareUltralightReaderImpl(IRfid rfid,
                                             CardInfo cardInfo,
                                             MifareUltralightReader mifareUltralightReader,
                                             PdEncoderFactory pdEncoderFactory) {
        super(rfid, cardInfo);
        this.mifareUltralightReader = mifareUltralightReader;
        this.pdEncoderFactory = pdEncoderFactory;
    }

    @Override
    public WriteCardResult writePd(Pd pd, byte startPageNumber) {
        PdEncoder pdEncoder = pdEncoderFactory.create(pd);
        byte[] data = pdEncoder.encode(pd);
        WriteCardResult result = mifareUltralightReader.writeBytes(data, startPageNumber);
        return result;
    }
}
