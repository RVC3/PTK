package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.BaseClassicCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.parser.BscInformationParser;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер информации о БСК.
 *
 * @author Aleksandr Brazhkin
 */
public class BscInformationReaderImpl extends BaseClassicCardReader implements BscInformationReader {

    private static final String TAG = Logger.makeLogTag(BscInformationReaderImpl.class);

    private BscInformation cachedBscInformation = null;

    public BscInformationReaderImpl(IRfid rfid,
                                    CardInfo cardInfo,
                                    StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                    SamAuthorizationStrategy samAuthorizationStrategy,
                                    MifareClassicReader mifareClassicReader,
                                    byte[] data) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        if (data != null) {
            this.cachedBscInformation = new BscInformationParser().parse(data);
        }
    }

    @Override
    public ReadCardResult<BscInformation> readBscInformation() {

        if (cachedBscInformation != null) {
            return new ReadCardResult<>(cachedBscInformation);
        }

        ReadCardResult<byte[]> rawResult = readBlockWithCardType();

        ReadCardResult<BscInformation> result;

        if (rawResult.isSuccess()) {
            cachedBscInformation = new BscInformationParser().parse(rawResult.getData());
            result = new ReadCardResult<>(cachedBscInformation);
        } else {
            result = new ReadCardResult<>(rawResult.getReadCardErrorType(), rawResult.getDescription());
        }

        return result;
    }
}
