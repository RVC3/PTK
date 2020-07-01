package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер ПД и ЭЦП для СКМ 4k.
 *
 * @author Grigoriy Kashka
 */
public class Skm4kReadWritePdReaderImpl extends BaseClassicCardReader implements Skm4kReadWritePdReader {

    private static final byte MAX_PD_COUNT = 2;

    private static final byte PD_START_SECTOR = 17;
    private static final byte PD_1_START_BLOCK = 0;
    private static final byte PD_2_START_BLOCK = 1;
    private static final byte PD_BLOCK_COUNT = 2;

    private static final byte EDS_START_SECTOR = 17;
    private static final byte EDS_START_BLOCK = 2;
    private static final byte EDS_BLOCK_COUNT = 4;

    private final ReadPdMifareClassicReader readPdMifareClassicReader;
    private final WritePdMifareClassicReader writePdMifareClassicReader;

    /**
     * Кеш для ЭЦП
     */
    private ReadCardResult<byte[]> readEdsResultCache = null;

    /**
     * Кеш для ПД
     */
    private ReadCardResult<List<Pd>> readPdListResultCache = null;

    public Skm4kReadWritePdReaderImpl(IRfid rfid,
                                      CardInfo cardInfo,
                                      StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                      SamAuthorizationStrategy samAuthorizationStrategy,
                                      MifareClassicReader mifareClassicReader,
                                      ReadPdMifareClassicReader readPdMifareClassicReader,
                                      WritePdMifareClassicReader writePdMifareClassicReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.readPdMifareClassicReader = readPdMifareClassicReader;
        this.writePdMifareClassicReader = writePdMifareClassicReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        if (readEdsResultCache == null)
            readEdsResultCache = readBlocks(EDS_START_SECTOR, EDS_START_BLOCK, EDS_BLOCK_COUNT);
        return readEdsResultCache;
    }

    @NonNull
    @Override
    public WriteCardResult writeEds(byte[] eds) {
        readEdsResultCache = null;
        return writeBlocks(eds, EDS_START_SECTOR, EDS_START_BLOCK);
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList() {
        if (readPdListResultCache == null)
            readPdListResultCache = readPdMifareClassicReader.readPdList(PD_START_SECTOR, PD_1_START_BLOCK, PD_BLOCK_COUNT, MAX_PD_COUNT);
        return readPdListResultCache;
    }

    @NonNull
    @Override
    public WriteCardResult writePdList(List<Pd> pdList, boolean[] forWriteIndexes) {

        readPdListResultCache = null;
        int size = pdList.size();

        if (size > MAX_PD_COUNT) {
            throw new IllegalArgumentException("Invalid pd count = " + size);
        }

        WriteCardResult writeCardResult;
        if (size > 0) {
            if (forWriteIndexes[0]) {
                writeCardResult = writePdMifareClassicReader.writePd(pdList.get(0), PD_START_SECTOR, PD_1_START_BLOCK);
                if (!writeCardResult.isSuccess()) {
                    return writeCardResult;
                }
            }
            if (size > 1) {
                if (forWriteIndexes[1]) {
                    writeCardResult = writePdMifareClassicReader.writePd(pdList.get(1), PD_START_SECTOR, PD_2_START_BLOCK);
                    if (!writeCardResult.isSuccess()) {
                        return writeCardResult;
                    }
                }
            }
        }

        return new WriteCardResult();
    }

    @Override
    public int getMaxPdCount() {
        return MAX_PD_COUNT;
    }
}