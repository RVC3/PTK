package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaListDecoder;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.CoverageAreaListDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер зон действия БСК.
 *
 * @author Aleksandr Brazhkin
 */
public class CoverageAreaReaderImpl extends BaseClassicCardReader implements CoverageAreaReader {

    private final CoverageAreaListDecoderFactory coverageAreaListDecoderFactory;

    public CoverageAreaReaderImpl(IRfid rfid,
                                  CardInfo cardInfo,
                                  StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                  SamAuthorizationStrategy samAuthorizationStrategy,
                                  MifareClassicReader mifareClassicReader,
                                  CoverageAreaListDecoderFactory coverageAreaListDecoderFactory) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.coverageAreaListDecoderFactory = coverageAreaListDecoderFactory;
    }

    @NonNull
    @Override
    public ReadCardResult<List<CoverageArea>> readCoverageAreaList(byte startSectorNumber, byte startBlockNumber, byte blockCount) {
        ReadCardResult<byte[]> rawResult = readRawCoverageAreaList(startSectorNumber, startBlockNumber, blockCount);
        if (rawResult.isSuccess()) {
            CoverageAreaListDecoder coverageAreaListDecoder = coverageAreaListDecoderFactory.create();
            List<CoverageArea> coverageAreaList = coverageAreaListDecoder.decode(rawResult.getData());
            return new ReadCardResult<>(coverageAreaList);
        } else {
            return new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readRawCoverageAreaList(byte startSectorNumber, byte startBlockNumber, byte blockCount) {
        return readBlocks(startSectorNumber, startBlockNumber, blockCount);
    }
}
