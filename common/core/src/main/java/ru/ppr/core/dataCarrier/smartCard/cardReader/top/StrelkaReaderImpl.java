package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.StrelkaDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.TroykaDecoderFactory;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт Стрелка.
 *
 * @author Aleksandr Brazhkin
 */
public class StrelkaReaderImpl extends BaseClassicCardReader implements StrelkaReader {
    private static final String TAG = Logger.makeLogTag(StrelkaReaderImpl.class);
    private static final byte MAX_PD_COUNT = 2;

    private static final byte PD_START_SECTOR = 5;
    private static final byte PD_1_START_BLOCK = 0;
    private static final byte PD_2_START_BLOCK = 1;
    private static final byte PD_BLOCK_COUNT = 2;

    private static final byte EDS_START_SECTOR = 5;
    private static final byte EDS_START_BLOCK = 2;
    private static final byte EDS_BLOCK_COUNT = 4;

    private static final byte PASSAGE_MARK_SECTOR = 15;
    private static final byte PASSAGE_MARK_BLOCK = 0;

    // В будущем: 11.03.2017 Only for test! Delete it
    private static final byte AUTH_CARD_DATA_SECTOR = 7;
    private static final byte AUTH_CARD_DATA_START_BLOCK = 0;
    private static final byte AUTH_CARD_DATA_COUNT_BLOCK = 23;

    private final OuterNumberReader outerNumberReader;
    private final ReadPdMifareClassicReader readPdMifareClassicReader;
    private final WritePdMifareClassicReader writePdMifareClassicReader;

    public StrelkaReaderImpl(IRfid rfid,
                             CardInfo cardInfo,
                             StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                             SamAuthorizationStrategy samAuthorizationStrategy,
                             MifareClassicReader mifareClassicReader,
                             OuterNumberReader outerNumberReader,
                             ReadPdMifareClassicReader readPdMifareClassicReader,
                             WritePdMifareClassicReader writePdMifareClassicReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.outerNumberReader = outerNumberReader;
        this.readPdMifareClassicReader = readPdMifareClassicReader;
        this.writePdMifareClassicReader = writePdMifareClassicReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        return readBlocks(EDS_START_SECTOR, EDS_START_BLOCK, EDS_BLOCK_COUNT);
    }

    @NonNull
    @Override
    public ReadCardResult<PassageMark> readPassageMark() {
        final ReadCardResult<byte[]> rawResult = readBlock(PASSAGE_MARK_SECTOR, PASSAGE_MARK_BLOCK);
        ReadCardResult<PassageMark> result;
        if (rawResult.isSuccess()) {
            Logger.trace(TAG, "Чтение метки прохода readPassageMark sector:" + PASSAGE_MARK_SECTOR +  " data:bytes - " + Arrays.toString(rawResult.getData()) + " hex:" + DataCarrierUtils.byteArrayToHex(rawResult.getData()));
            PassageMarkDecoder passageMarkDecoder = new StrelkaDecoderFactory((int) PASSAGE_MARK_SECTOR).create(rawResult.getData());
            PassageMark passageMark = passageMarkDecoder.decode(rawResult.getData());
            result = new ReadCardResult<>(passageMark);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }
        Logger.trace(TAG, "Чтение метки прохода getPassageMarkReadCardResult result:" + result);
        return result;
    }

    @NonNull
    @Override
    public WriteCardResult writeEds(byte[] eds) {
        return writeBlocks(eds, EDS_START_SECTOR, EDS_START_BLOCK);
    }

    @NonNull
    @Override
    public ReadCardResult<OuterNumber> readOuterNumber() {
        return outerNumberReader.readOuterNumber();
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

    @Override
    public int getMaxPdCount() {
        return MAX_PD_COUNT;
    }
}
