package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.core.dataCarrier.smartCard.parser.PersonalDataParser;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер песональных данных.
 *
 * @author Aleksandr Brazhkin
 */
public class PersonalDataReaderImpl extends BaseClassicCardReader implements PersonalDataReader {

    private static final String TAG = Logger.makeLogTag(PersonalDataReaderImpl.class);

    private static final byte PERSONAL_DATA_SECTOR = 13;
    private static final byte PERSONAL_DATA_START_BLOCK = 0;
    private static final byte PERSONAL_DATA_COUNT_BLOCK = 6;

    private PersonalData cachedPersonalData = null;

    public PersonalDataReaderImpl(IRfid rfid,
                                  CardInfo cardInfo,
                                  StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                  SamAuthorizationStrategy samAuthorizationStrategy,
                                  MifareClassicReader mifareClassicReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
    }

    @NonNull
    @Override
    public ReadCardResult<PersonalData> readPersonalData() {

        if (cachedPersonalData != null) {
            return new ReadCardResult<>(cachedPersonalData);
        }

        ReadCardResult<byte[]> rawResult = readBlocks(PERSONAL_DATA_SECTOR, PERSONAL_DATA_START_BLOCK, PERSONAL_DATA_COUNT_BLOCK);

        ReadCardResult<PersonalData> result;

        if (rawResult.isSuccess()) {
            cachedPersonalData = new PersonalDataParser().parse(rawResult.getData());
            result = new ReadCardResult<>(cachedPersonalData);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }
}
