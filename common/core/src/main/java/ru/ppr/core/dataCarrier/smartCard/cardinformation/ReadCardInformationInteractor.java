package ru.ppr.core.dataCarrier.smartCard.cardinformation;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.SkmSkmoIpkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.EttReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaReader;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;
import ru.ppr.core.dataCarrier.smartCard.entity.EttData;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;

/**
 * @author Aleksandr Brazhkin
 */
public class ReadCardInformationInteractor {

    @Inject
    public ReadCardInformationInteractor() {

    }

    @NonNull
    public ReadCardResult<CardInformation> readCardInformation(CardReader cardReader) {
        if (cardReader instanceof SkmSkmoIpkReader) {
            SkmSkmoIpkReader skmSkmoIpkReader = (SkmSkmoIpkReader) cardReader;
            ReadCardResult<BscInformation> bscInformationResult = skmSkmoIpkReader.readBscInformation();
            if (!bscInformationResult.isSuccess()) {
                return new ReadCardResult<>(bscInformationResult.getReadCardErrorType(), bscInformationResult.getDescription());
            }
            ReadCardResult<EmissionData> emissionDataResult = skmSkmoIpkReader.readEmissionData();
            if (!emissionDataResult.isSuccess()) {
                return new ReadCardResult<>(emissionDataResult.getReadCardErrorType(), emissionDataResult.getDescription());
            }
            ReadCardResult<PersonalData> personalDataResult = skmSkmoIpkReader.readPersonalData();
            if (!personalDataResult.isSuccess()) {
                return new ReadCardResult<>(personalDataResult.getReadCardErrorType(), personalDataResult.getDescription());
            }
            return new ReadCardResult<>(new SkmSkmoIpkCardInformation(
                    cardReader.getCardInfo(),
                    bscInformationResult.getData(),
                    emissionDataResult.getData(),
                    personalDataResult.getData()
            ));
        } else if (cardReader instanceof StrelkaReader || cardReader instanceof TroykaReader) {
            OuterNumberReader outerNumberReader = (OuterNumberReader) cardReader;
            ReadCardResult<OuterNumber> outerNumberResult = outerNumberReader.readOuterNumber();
            if (!outerNumberResult.isSuccess()) {
                return new ReadCardResult<>(outerNumberResult.getReadCardErrorType(), outerNumberResult.getDescription());
            }
            return new ReadCardResult<>(new StrelkaTroykaCardInformation(
                    cardReader.getCardInfo(),
                    outerNumberResult.getData()
            ));
        } else if (cardReader instanceof EttReader) {
            EttReader ettReader = (EttReader) cardReader;
            ReadCardResult<BscInformation> bscInformationResult = ettReader.readBscInformation();
            if (!bscInformationResult.isSuccess()) {
                return new ReadCardResult<>(bscInformationResult.getReadCardErrorType(), bscInformationResult.getDescription());
            }
            ReadCardResult<EttData> ettDataResult = ettReader.readEttData();
            if (!ettDataResult.isSuccess()) {
                return new ReadCardResult<>(ettDataResult.getReadCardErrorType(), ettDataResult.getDescription());
            }
            return new ReadCardResult<>(new EttCardInformation(
                    cardReader.getCardInfo(),
                    bscInformationResult.getData(),
                    ettDataResult.getData()
            ));
        } else if (cardReader instanceof OuterNumberReader) {
            OuterNumberReader outerNumberReader = (OuterNumberReader) cardReader;
            ReadCardResult<OuterNumber> outerNumberResult = outerNumberReader.readOuterNumber();
            if (!outerNumberResult.isSuccess()) {
                return new ReadCardResult<>(outerNumberResult.getReadCardErrorType(), outerNumberResult.getDescription());
            }
            return new ReadCardResult<>(new CppkCardInformation(
                    cardReader.getCardInfo(),
                    outerNumberResult.getData()
            ));
        } else {
            throw new IllegalArgumentException("Unknown card reader: " + cardReader.getClass());
        }
    }
}
