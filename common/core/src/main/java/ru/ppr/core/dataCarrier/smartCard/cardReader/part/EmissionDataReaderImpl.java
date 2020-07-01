package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;
import ru.ppr.core.dataCarrier.smartCard.parser.EmissionDataParser;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер эмисионных данных.
 *
 * @author Aleksandr Brazhkin
 */
public class EmissionDataReaderImpl extends BaseClassicCardReader implements EmissionDataReader {

    private static final byte EMISSION_INFO_START_SECTOR = 15;
    private static final byte EMISSION_INFO_BLOCK_NUMBER = 0;

    private EmissionData cachedEmissionData = null;

    public EmissionDataReaderImpl(IRfid rfid,
                                  CardInfo cardInfo,
                                  StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                  SamAuthorizationStrategy samAuthorizationStrategy,
                                  MifareClassicReader mifareClassicReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
    }

    @NonNull
    @Override
    public ReadCardResult<EmissionData> readEmissionData() {

        if (cachedEmissionData != null) {
            return new ReadCardResult<>(cachedEmissionData);
        }

        ReadCardResult<byte[]> rawResult = readBlock(EMISSION_INFO_START_SECTOR, EMISSION_INFO_BLOCK_NUMBER);

        ReadCardResult<EmissionData> result;

        if (rawResult.isSuccess()) {
            cachedEmissionData = new EmissionDataParser().parse(rawResult.getData());
            result = new ReadCardResult<>(cachedEmissionData);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }
}
