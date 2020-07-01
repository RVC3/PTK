package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseCppkNumberOfTripsOnePdReader;
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
 * Ридер карт ЦППК Ultralight EV1 на количество поездок (1 ПД на карту).
 *
 * @author Aleksandr Brazhkin
 */
public class CppkNumberOfTripsUltralightEv1OnePdReader extends BaseCppkNumberOfTripsOnePdReader implements CppkNumberOfTripsOnePdReader {

    private static final byte EDS_PAGE_NUMBER = 14;
    private static final byte EDS_BYTE_NUMBER = 0;
    private static final byte EDS_LENGTH = 64;

    private static final byte PASSAGE_MARK_PAGE = 30;
    private static final byte PASSAGE_MARK_BYTE = 0;

    private final ReadPassageMarkMifareUltralightReader readPassageMarkMifareUltralightReader;
    private final WritePassageMarkMifareUltralightReader writePassageMarkMifareUltralightReader;

    public CppkNumberOfTripsUltralightEv1OnePdReader(IRfid rfid,
                                                     CardInfo cardInfo,
                                                     MifareUltralightReader mifareUltralightReader,
                                                     OuterNumberReader outerNumberReader,
                                                     ReadPdMifareUltralightReader readPdMifareUltralightReader,
                                                     WritePdMifareUltralightReader writePdMifareUltralightReader,
                                                     ReadPassageMarkMifareUltralightReader readPassageMarkMifareUltralightReader,
                                                     WritePassageMarkMifareUltralightReader writePassageMarkMifareUltralightReader,
                                                     HardwareCounterReader hardwareCounterReader) {
        super(rfid, cardInfo, mifareUltralightReader, outerNumberReader, readPdMifareUltralightReader, writePdMifareUltralightReader, hardwareCounterReader);
        this.readPassageMarkMifareUltralightReader = readPassageMarkMifareUltralightReader;
        this.writePassageMarkMifareUltralightReader = writePassageMarkMifareUltralightReader;
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
}
