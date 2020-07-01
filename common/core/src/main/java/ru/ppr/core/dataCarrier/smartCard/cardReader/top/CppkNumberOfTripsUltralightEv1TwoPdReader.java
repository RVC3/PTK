package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardErrorType;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardErrorType;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CppkNumberOfTripsPayloadReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.HardwareCounterReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdMifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.rfid.IRfid;

/**
 * Ридер карт ЦППК Ultralight EV1 на количество поездок (2 ПД на карту).
 *
 * @author Aleksandr Brazhkin
 */
public class CppkNumberOfTripsUltralightEv1TwoPdReader extends CppkNumberOfTripsPayloadReaderImpl implements CppkNumberOfTripsTwoPdReader {

    private static final byte MAX_PD_COUNT = 2;

    private static final byte PD1_HW_COUNTER_INDEX = 0;
    private static final byte PD2_HW_COUNTER_INDEX = 1;

    private static final byte PD1_PAGE_NUMBER = 8;
    private static final byte PD2_PAGE_NUMBER = 12;
    private static final byte PD1_BYTE_NUMBER = 0;
    private static final byte PD_BYTE_COUNT = 32;

    private static final byte EDS_PAGE_NUMBER = 16;
    private static final byte EDS_BYTE_NUMBER = 0;
    private static final byte EDS_LENGTH = 64;

    private static final byte PASSAGE_MARK_PAGE = 32;
    private static final byte PASSAGE_MARK_BYTE = 0;

    private final ReadPassageMarkMifareUltralightReader readPassageMarkMifareUltralightReader;
    private final WritePassageMarkMifareUltralightReader writePassageMarkMifareUltralightReader;
    private final ReadPdMifareUltralightReader readPdMifareUltralightReader;
    private final WritePdMifareUltralightReader writePdMifareUltralightReader;
    private final HardwareCounterReader hardwareCounterReader;

    public CppkNumberOfTripsUltralightEv1TwoPdReader(IRfid rfid,
                                                     CardInfo cardInfo,
                                                     MifareUltralightReader mifareUltralightReader,
                                                     OuterNumberReader outerNumberReader,
                                                     ReadPdMifareUltralightReader readPdMifareUltralightReader,
                                                     WritePdMifareUltralightReader writePdMifareUltralightReader,
                                                     ReadPassageMarkMifareUltralightReader readPassageMarkMifareUltralightReader,
                                                     WritePassageMarkMifareUltralightReader writePassageMarkMifareUltralightReader,
                                                     HardwareCounterReader hardwareCounterReader) {
        super(rfid, cardInfo, mifareUltralightReader, outerNumberReader);
        this.readPassageMarkMifareUltralightReader = readPassageMarkMifareUltralightReader;
        this.writePassageMarkMifareUltralightReader = writePassageMarkMifareUltralightReader;
        this.readPdMifareUltralightReader = readPdMifareUltralightReader;
        this.writePdMifareUltralightReader = writePdMifareUltralightReader;
        this.hardwareCounterReader = hardwareCounterReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readEds() {
        return readBytes(EDS_PAGE_NUMBER, EDS_BYTE_NUMBER, EDS_LENGTH);
    }

    @NonNull
    @Override
    public WriteCardResult writeEds(byte[] eds) {
        return writeBytes(eds, EDS_PAGE_NUMBER);
    }

    @NonNull
    @Override
    public ReadCardResult<PassageMark> readPassageMark() {
        return readPassageMarkMifareUltralightReader.readPassageMark(PASSAGE_MARK_PAGE, PASSAGE_MARK_BYTE);
    }

    @NonNull
    @Override
    public WriteCardResult writePassageMark(PassageMark passageMark) {
        return writePassageMarkMifareUltralightReader.writePassageMark(passageMark, PASSAGE_MARK_PAGE);
    }

    @NonNull
    @Override
    public ReadCardResult<List<Pd>> readPdList() {
        return readPdMifareUltralightReader.readPdList(PD1_PAGE_NUMBER, PD1_BYTE_NUMBER, PD_BYTE_COUNT, MAX_PD_COUNT);
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
                writeCardResult = writePdMifareUltralightReader.writePd(pdList.get(0), PD1_PAGE_NUMBER);
                if (!writeCardResult.isSuccess()) {
                    return writeCardResult;
                }
            }
            if (size > 1) {
                if (forWriteIndexes[1]) {
                    writeCardResult = writePdMifareUltralightReader.writePd(pdList.get(1), PD2_PAGE_NUMBER);
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
    public ReadCardResult<Integer> readHardwareCounter(int pdIndex) {
        if (pdIndex == 0) {
            return hardwareCounterReader.readHardwareCounter(PD1_HW_COUNTER_INDEX);
        } else if (pdIndex == 1) {
            return hardwareCounterReader.readHardwareCounter(PD2_HW_COUNTER_INDEX);
        } else {
            return new ReadCardResult<>(ReadCardErrorType.OTHER, "pdIndex is out of bounds");
        }
    }

    @NonNull
    @Override
    public WriteCardResult incrementHardwareCounter(int pdIndex, int incrementValue) {
        if (pdIndex == 0) {
            return hardwareCounterReader.incrementHardwareCounter(PD1_HW_COUNTER_INDEX, incrementValue);
        } else if (pdIndex == 1) {
            return hardwareCounterReader.incrementHardwareCounter(PD2_HW_COUNTER_INDEX, incrementValue);
        } else {
            return new WriteCardResult(WriteCardErrorType.NOT_SUPPORTED, "pdIndex is out of bounds");
        }
    }

    @Override
    public int getMaxPdCount() {
        return MAX_PD_COUNT;
    }
}
