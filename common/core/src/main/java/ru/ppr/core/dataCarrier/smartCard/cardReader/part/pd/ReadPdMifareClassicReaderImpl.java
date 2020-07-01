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
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер, читающий ПД со смарт-карт Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadPdMifareClassicReaderImpl extends BaseClassicCardReader implements ReadPdMifareClassicReader {

    private static final int BLOCK_SIZE = 16;

    private final PdDecoderFactory pdDecoderFactory;

    public ReadPdMifareClassicReaderImpl(IRfid rfid,
                                         CardInfo cardInfo,
                                         StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                         SamAuthorizationStrategy samAuthorizationStrategy,
                                         MifareClassicReader mifareClassicReader,
                                         PdDecoderFactory pdDecoderFactory) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.pdDecoderFactory = pdDecoderFactory;
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList(byte startSectorNumber, byte startBlockNumber, byte blockCount, int pdCount) {
        ReadCardResult<byte[]> rawResult = readBlocks(startSectorNumber, startBlockNumber, blockCount);
        ReadCardResult<List<Pd>> result;
        if (rawResult.isSuccess()) {
            List<Pd> pdList = new ArrayList<>();
            int pdSize = blockCount * BLOCK_SIZE / pdCount;
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
