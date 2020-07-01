package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.SkmSkmoIpkReaderImpl;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.EmissionDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.PersonalDataReader;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер карт СКМ с которых не удалось прочитать ПД или ЭЦП
 * http://agile.srvdev.ru/browse/CPPKPP-34076
 *
 * @author Grigoriy Kashka
 */
public class SkmNoPdPlaceReaderImpl extends SkmSkmoIpkReaderImpl implements SkmNoPdPlaceReader {

    private static final byte MAX_PD_COUNT = 0;

    public SkmNoPdPlaceReaderImpl(IRfid rfid,
                                  CardInfo cardInfo,
                                  StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                  SamAuthorizationStrategy samAuthorizationStrategy,
                                  MifareClassicReader mifareClassicReader,
                                  BscInformationReader bscInformationReader,
                                  EmissionDataReader emissionDataReader,
                                  PersonalDataReader personalDataReader
    ) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader, bscInformationReader, emissionDataReader, personalDataReader);
    }

    @Override
    public int getMaxPdCount() {
        return MAX_PD_COUNT;
    }
}
