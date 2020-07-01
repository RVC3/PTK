package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseUltralightCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.parser.outerNumber.OuterNumberParser;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;

/**
 * Ридер внешнего номера карты Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public class OuterNumberUltralightReaderImpl extends BaseUltralightCardReader implements OuterNumberReader {

    private static final String TAG = Logger.makeLogTag(OuterNumberUltralightReaderImpl.class);

    private final OuterNumberParser outerNumberParser;

    private OuterNumber cachedOuterNumber = null;

    public OuterNumberUltralightReaderImpl(IRfid rfid,
                                           CardInfo cardInfo,
                                           MifareUltralightReader mifareUltralightReader,
                                           OuterNumberParser outerNumberParser,
                                           byte[] data) {
        super(rfid, cardInfo, mifareUltralightReader);
        this.outerNumberParser = outerNumberParser;
        if (data != null) {
            cachedOuterNumber = outerNumberParser.parse(data);
        }
    }

    @NonNull
    @Override
    public ReadCardResult<OuterNumber> readOuterNumber() {

        if (cachedOuterNumber != null) {
            return new ReadCardResult<>(cachedOuterNumber);
        }

        ReadCardResult<byte[]> rawResult = readBytesWithCardType();

        ReadCardResult<OuterNumber> result;

        if (rawResult.isSuccess()) {
            cachedOuterNumber = outerNumberParser.parse(rawResult.getData());
            result = new ReadCardResult<>(cachedOuterNumber);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }
}
