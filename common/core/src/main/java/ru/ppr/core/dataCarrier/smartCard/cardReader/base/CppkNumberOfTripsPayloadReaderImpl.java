package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.rfid.IRfid;

/**
 * Ридер карт ЦППК на количество поездок.
 *
 * @author Aleksandr Brazhkin
 */
public class CppkNumberOfTripsPayloadReaderImpl extends BaseUltralightCardReader implements CppkNumberOfTripsPayloadReader {

    private static final byte PAYLOAD_PAGE = 8;
    private static final byte PAYLOAD_BYTE = 0;
    private static final byte PAYLOAD_BYTE_COUNT = 1;

    private final OuterNumberReader outerNumberReader;

    public CppkNumberOfTripsPayloadReaderImpl(IRfid rfid,
                                              CardInfo cardInfo,
                                              MifareUltralightReader mifareUltralightReader,
                                              OuterNumberReader outerNumberReader) {
        super(rfid, cardInfo, mifareUltralightReader);
        this.outerNumberReader = outerNumberReader;
    }

    @NonNull
    @Override
    public ReadCardResult<byte[]> readFirstPayloadBytes() {
        return readBytes(PAYLOAD_PAGE, PAYLOAD_BYTE, PAYLOAD_BYTE_COUNT);
    }

    @NonNull
    @Override
    public ReadCardResult<OuterNumber> readOuterNumber() {
        return outerNumberReader.readOuterNumber();
    }
}
