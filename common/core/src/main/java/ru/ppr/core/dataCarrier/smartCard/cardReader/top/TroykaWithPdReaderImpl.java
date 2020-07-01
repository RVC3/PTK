package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт Тройка.
 *
 * @author Grigoriy Kashka
 */
public class TroykaWithPdReaderImpl extends TroykaReaderImpl implements TroykaWithPdReader {

    private static final byte MAX_PD_COUNT = 2;

    private static final byte PD_START_SECTOR = 6;
    private static final byte PD_1_START_BLOCK = 0;
    private static final byte PD_2_START_BLOCK = 1;
    private static final byte PD_BLOCK_COUNT = 2;



    private final ReadPdMifareClassicReader readPdMifareClassicReader;
    private final WritePdMifareClassicReader writePdMifareClassicReader;

    public TroykaWithPdReaderImpl(IRfid rfid,
                                  CardInfo cardInfo,
                                  StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                  SamAuthorizationStrategy samAuthorizationStrategy,
                                  MifareClassicReader mifareClassicReader,
                                  OuterNumberReader outerNumberReader,
                                  ReadPdMifareClassicReader readPdMifareClassicReader,
                                  WritePdMifareClassicReader writePdMifareClassicReader, CardReader readPassageMarkMifareClassicReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, outerNumberReader, null);
        this.readPdMifareClassicReader = readPdMifareClassicReader;
        this.writePdMifareClassicReader = writePdMifareClassicReader;
    }

    @Override
    public int getMaxPdCount() {
        return MAX_PD_COUNT;
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList() {
        return readPdMifareClassicReader.readPdList(PD_START_SECTOR, PD_1_START_BLOCK, PD_BLOCK_COUNT, MAX_PD_COUNT);
    }


    @NonNull
    @Override
    public WriteCardResult writePdList(List<Pd> pdList, boolean[] forWriteIndexes) {
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
}
