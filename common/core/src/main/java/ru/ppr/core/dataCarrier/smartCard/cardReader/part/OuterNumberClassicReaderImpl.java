package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.parser.outerNumber.OuterNumberParser;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер внешнего номера карты Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public class OuterNumberClassicReaderImpl extends BaseClassicCardReader implements OuterNumberReader {

    private static final String TAG = Logger.makeLogTag(OuterNumberClassicReaderImpl.class);

    private static final byte SECTOR_NUMBER = 2;
    private static final byte BLOCK_NUMBER = 0;

    private final OuterNumberParser outerNumberParser;

    private OuterNumber cachedOuterNumber = null;

    public OuterNumberClassicReaderImpl(IRfid rfid,
                                        CardInfo cardInfo,
                                        StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                        SamAuthorizationStrategy samAuthorizationStrategy,
                                        MifareClassicReader mifareClassicReader,
                                        OuterNumberParser outerNumberParser,
                                        byte[] data) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
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

        ReadCardResult<byte[]> rawResult = readBlock(SECTOR_NUMBER, BLOCK_NUMBER);

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
