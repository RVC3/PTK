package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.EmissionDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.PersonalDataReader;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.rfid.SamAuthorizationStrategy;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.StaticKeyAuthorizationStrategy;

/**
 * Ридер смарт карт СКМ, СКМО, ИПК.
 *
 * @author Aleksandr Brazhkin
 */
public class SkmSkmoIpkReaderImpl extends BaseClassicCardReader implements SkmSkmoIpkReader {

    private final BscInformationReader bscInformationReader;
    private final EmissionDataReader emissionDataReader;
    private final PersonalDataReader personalDataReader;

    public SkmSkmoIpkReaderImpl(IRfid rfid,
                                CardInfo cardInfo,
                                StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy,
                                SamAuthorizationStrategy samAuthorizationStrategy,
                                MifareClassicReader mifareClassicReader,
                                BscInformationReader bscInformationReader,
                                EmissionDataReader emissionDataReader,
                                PersonalDataReader personalDataReader) {
        super(rfid, cardInfo, staticKeyAuthorizationStrategy, samAuthorizationStrategy, mifareClassicReader);
        this.bscInformationReader = bscInformationReader;
        this.emissionDataReader = emissionDataReader;
        this.personalDataReader = personalDataReader;
    }

    @NonNull
    @Override
    public ReadCardResult<BscInformation> readBscInformation() {
        return bscInformationReader.readBscInformation();
    }

    @NonNull
    @Override
    public ReadCardResult<EmissionData> readEmissionData() {
        return emissionDataReader.readEmissionData();
    }

    @NonNull
    @Override
    public ReadCardResult<PersonalData> readPersonalData() {
        return personalDataReader.readPersonalData();
    }
}
