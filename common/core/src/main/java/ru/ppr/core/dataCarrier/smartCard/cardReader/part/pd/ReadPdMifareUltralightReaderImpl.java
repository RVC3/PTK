package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.rfid.IRfid;

/**
 * Ридер, читающий ПД со смарт-карт Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadPdMifareUltralightReaderImpl extends BaseCardReader implements ReadPdMifareUltralightReader {

    private final MifareUltralightReader mifareUltralightReader;

    private final PdDecoderFactory pdDecoderFactory;

    public ReadPdMifareUltralightReaderImpl(IRfid rfid,
                                            CardInfo cardInfo,
                                            MifareUltralightReader mifareUltralightReader,
                                            PdDecoderFactory pdDecoderFactory) {
        super(rfid, cardInfo);
        this.mifareUltralightReader = mifareUltralightReader;
        this.pdDecoderFactory = pdDecoderFactory;
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList(byte startPageNumber, byte startByteNumber, byte byteCount, int pdCount) {
        ReadCardResult<byte[]> rawResult = mifareUltralightReader.readBytes(startPageNumber, startByteNumber, byteCount);

        ReadCardResult<List<Pd>> result;

        if (rawResult.isSuccess()) {
            List<Pd> pdList = new ArrayList<>();
            int pdSize = byteCount / pdCount;
            for (int i = 0; i < pdCount; i++) {
                byte[] pdData = DataCarrierUtils.subArray(rawResult.getData(), pdSize * i, pdSize);
                PdDecoder pdDecoder = pdDecoderFactory.create(pdData);
                Pd pd = pdDecoder.decode(pdData);
                pdList.add(pd);
            }
            result = new ReadCardResult<>(pdList);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }
}
