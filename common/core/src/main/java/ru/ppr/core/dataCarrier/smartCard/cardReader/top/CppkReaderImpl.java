package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;
import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionDetector;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт ЦППК.
 *
 * @author Aleksandr Brazhkin
 */
public class CppkReaderImpl extends BaseClassicCardReader implements CppkReader {

    private static final byte MAX_PD_COUNT = 2;

    private static final byte PD_START_SECTOR = 7;
    private static final byte PD_1_START_BLOCK = 0;
    private static final byte PD_2_START_BLOCK = 2;
    private static final byte PD_BLOCK_COUNT = 4;
    private static final byte PD_SIZE_IN_BYTES_TWO_PD = 32;

    private static final byte EDS_START_SECTOR = 8;
    private static final byte EDS_START_BLOCK = 1;
    private static final byte EDS_BLOCK_COUNT = 4;

    private static final byte PASSAGE_MARK_SECTOR = 9;
    private static final byte PASSAGE_MARK_BLOCK = 2;

    private final OuterNumberReader outerNumberReader;
    private final WritePdMifareClassicReader writePdMifareClassicReader;
    private final ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader;
    private final WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader;
    private final PdDecoderFactory pdDecoderFactory;
    private final PdVersionDetector pdVersionDetector;

    public CppkReaderImpl(IRfid rfid,
                          CardInfo cardInfo,
                          StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                          SamAuthorizationStrategy samAuthorizationStrategy,
                          MifareClassicReader mifareClassicReader,
                          OuterNumberReader outerNumberReader,
                          WritePdMifareClassicReader writePdMifareClassicReader,
                          ReadPassageMarkMifareClassicReader readPassageMarkMifareClassicReader,
                          WritePassageMarkMifareClassicReader writePassageMarkMifareClassicReader,
                          PdDecoderFactory pdDecoderFactory,
                          PdVersionDetector pdVersionDetector) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.outerNumberReader = outerNumberReader;
        this.writePdMifareClassicReader = writePdMifareClassicReader;
        this.readPassageMarkMifareClassicReader = readPassageMarkMifareClassicReader;
        this.writePassageMarkMifareClassicReader = writePassageMarkMifareClassicReader;
        this.pdDecoderFactory = pdDecoderFactory;
        this.pdVersionDetector = pdVersionDetector;
    }

    @NonNull
    @Override
    public ReadCardResult<OuterNumber> readOuterNumber() {
        return outerNumberReader.readOuterNumber();
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList() {

        ReadCardResult<byte[]> rawResult = readBlocks(PD_START_SECTOR, PD_1_START_BLOCK, PD_BLOCK_COUNT);

        ReadCardResult<List<Pd>> result;

        if (rawResult.isSuccess()) {
            if (pdVersionDetector.getVersion(rawResult.getData()) == PdVersion.V10) {
                PdDecoder pdDecoder = pdDecoderFactory.create(rawResult.getData());
                Pd pd = pdDecoder.decode(rawResult.getData());
                result = new ReadCardResult<>(Collections.singletonList(pd));
            } else {
                byte[] pd1Data = DataCarrierUtils.subArray(rawResult.getData(), 0, PD_SIZE_IN_BYTES_TWO_PD);
                byte[] pd2Data = DataCarrierUtils.subArray(rawResult.getData(), PD_SIZE_IN_BYTES_TWO_PD, PD_SIZE_IN_BYTES_TWO_PD);

                List<Pd> pdList = new ArrayList<>();

                PdDecoder pd1Decoder = pdDecoderFactory.create(pd1Data);
                Pd pd1 = pd1Decoder.decode(pd1Data);
                pdList.add(pd1);

                PdDecoder pd2Decoder = pdDecoderFactory.create(pd2Data);
                Pd pd2 = pd2Decoder.decode(pd2Data);
                pdList.add(pd2);

                result = new ReadCardResult<>(pdList);
            }
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
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
