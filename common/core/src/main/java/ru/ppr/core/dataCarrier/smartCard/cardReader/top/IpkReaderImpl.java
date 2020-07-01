package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.SkmSkmoIpkReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.EmissionDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.PersonalDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт ИПК.
 *
 * @author Aleksandr Brazhkin
 */
public class IpkReaderImpl extends SkmSkmoIpkReaderImpl implements IpkReader {

    private static final byte MAX_PD_COUNT = 2;

    private static final byte PD_START_SECTOR = 7;
    private static final byte PD_1_START_BLOCK = 0;
    private static final byte PD_2_START_BLOCK = 2;
    private static final byte PD_BLOCK_COUNT = 4;

    private static final byte EDS_START_SECTOR = 8;
    private static final byte EDS_START_BLOCK = 1;
    private static final byte EDS_BLOCK_COUNT = 4;

    private static final byte PASSAGE_MARK_SECTOR = 9;
    private static final byte PASSAGE_MARK_BLOCK = 2;

    private final ReadPdMifareClassicReader readPdMifareClassicReader;
    private final WritePdMifareClassicReader writePdMifareClassicReader;
    private final ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader;
    private final WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader;

    public IpkReaderImpl(IRfid rfid,
                         CardInfo cardInfo,
                         StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                         SamAuthorizationStrategy samAuthorizationStrategy,
                         BscInformationReader bscInformationReader,
                         EmissionDataReader emissionDataReader,
                         PersonalDataReader personalDataReader,
                         MifareClassicReader mifareClassicReader,
                         ReadPdMifareClassicReader readPdMifareClassicReader,
                         WritePdMifareClassicReader writePdMifareClassicReader,
                         ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader,
                         WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, bscInformationReader, emissionDataReader, personalDataReader);
        this.readPdMifareClassicReader = readPdMifareClassicReader;
        this.writePdMifareClassicReader = writePdMifareClassicReader;
        this.readPassageMarkMifareClassicReader = readPassageMarkMifareClassicReader;
        this.writePassageMarkMifareClassicReader = writePassageMarkMifareClassicReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        return readBlocks(EDS_START_SECTOR, EDS_START_BLOCK, EDS_BLOCK_COUNT);
    }

    @NonNull
    @Override
    public WriteCardResult writeEds(byte[] eds) {
        return writeBlocks(eds, EDS_START_SECTOR, EDS_START_BLOCK);
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

    @NonNull
    @Override
    public ReadCardResult<PassageMark> readPassageMark() {
        return readPassageMarkMifareClassicReader.readPassageMark(PASSAGE_MARK_SECTOR, PASSAGE_MARK_BLOCK);
    }

    @NonNull
    @Override
    public WriteCardResult writePassageMark(PassageMark passageMark) {
        return writePassageMarkMifareClassicReader.writePassageMark(passageMark, PASSAGE_MARK_SECTOR, PASSAGE_MARK_BLOCK);
    }

    @Override
    public int getMaxPdCount() {
        return MAX_PD_COUNT;
    }
}
