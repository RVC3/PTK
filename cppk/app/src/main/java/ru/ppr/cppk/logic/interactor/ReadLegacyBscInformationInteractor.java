package ru.ppr.cppk.logic.interactor;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.findcardtask.CardInfo;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.EmissionDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.PersonalDataReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.EttReader;
import ru.ppr.core.dataCarrier.smartCard.checker.SkmSkmoIpkRecognizer;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;
import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;
import ru.ppr.core.dataCarrier.smartCard.entity.EttData;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.cppk.dataCarrier.BscInformationMapper;
import ru.ppr.cppk.dataCarrier.EmissionDataMapper;
import ru.ppr.cppk.dataCarrier.EttDataMapper;
import ru.ppr.cppk.dataCarrier.OuterNumberMapper;
import ru.ppr.cppk.dataCarrier.PersonalDataMapper;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
public class ReadLegacyBscInformationInteractor {

    @Inject
    public ReadLegacyBscInformationInteractor() {

    }

    public ru.ppr.cppk.dataCarrier.entity.BscInformation read(CardReader cardReader) {

        ru.ppr.cppk.dataCarrier.entity.BscInformation legacyBscInformation;

        // Считываем информацию о карте
        CardInfo cardInfo = cardReader.getCardInfo();

        if (cardReader instanceof BscInformationReader) {
            // Если ридер поддерживает чтение информации о БСК
            ReadCardResult<BscInformation> readBscInformationResult = ((BscInformationReader) cardReader).readBscInformation();
            if (readBscInformationResult.isSuccess()) {
                // Если информация о БСК успешно считана, получаем её
                BscInformation bscInformation = readBscInformationResult.getData();
                legacyBscInformation = new BscInformationMapper().toLegacyBscInformation(bscInformation, cardInfo.getCardUid());
            } else {
                // Если информацию о БСК считать не удалось, завершаем работу
                return null;
            }
        } else if (cardReader instanceof OuterNumberReader) {
            // Если ридер поддерживает чтение внешнего номера БСК
            ReadCardResult<OuterNumber> readOuterNumberResult = ((OuterNumberReader) cardReader).readOuterNumber();
            if (readOuterNumberResult.isSuccess()) {
                // Если внешний номер успешно считан, получаем его
                OuterNumber outerNumber = readOuterNumberResult.getData();
                legacyBscInformation = new OuterNumberMapper().toLegacyBscInformation(outerNumber, cardInfo.getCardUid());
            } else {
                // Если внешний номер считать не удалось, завершаем работу
                return null;
            }
        } else {
            // Если ридер не поддерживает ни чтения информации о БСК, ни чтения внешнего номера, завершаем работу
            return null;
        }

        if (cardReader instanceof EmissionDataReader) {
            // Читаем эмисионные данные
            ReadCardResult<EmissionData> emissionDataResult = ((EmissionDataReader) cardReader).readEmissionData();
            if (emissionDataResult.isSuccess()) {
                EmissionData emissionData = emissionDataResult.getData();
                legacyBscInformation.setEmissionData(new EmissionDataMapper().toLegacyEmissionData(emissionData));
                SkmSkmoIpkRecognizer skmSkmoIpkRecognizer = new SkmSkmoIpkRecognizer();
                if (skmSkmoIpkRecognizer.isSkm(emissionData.getCardNumber())) {
                    legacyBscInformation.setTypeBsc(TicketStorageType.SKM);
                } else if (skmSkmoIpkRecognizer.isSkmo(emissionData.getCardNumber())) {
                    legacyBscInformation.setTypeBsc(TicketStorageType.SKMO);
                } else if (skmSkmoIpkRecognizer.isIpk(emissionData.getCardNumber())) {
                    legacyBscInformation.setTypeBsc(TicketStorageType.IPK);
                }
            }
        }

        if (cardReader instanceof PersonalDataReader) {
            // Читаем персональные данные
            ReadCardResult<PersonalData> personalDataResult = ((PersonalDataReader) cardReader).readPersonalData();
            if (personalDataResult.isSuccess()) {
                PersonalData personalData = personalDataResult.getData();
                legacyBscInformation.setPersonalData(new PersonalDataMapper().toLegacyPersonalData(personalData));
            }
        }

        if (cardReader instanceof EttReader) {
            // Читаем иноформацию об ЭТТ
            ReadCardResult<EttData> personalDataResult = ((EttReader) cardReader).readEttData();
            if (personalDataResult.isSuccess()) {
                EttData ettData = personalDataResult.getData();
                legacyBscInformation.setEttData(new EttDataMapper().toLegacyPersonalData(ettData));
            }
        }

        return legacyBscInformation;
    }

}
