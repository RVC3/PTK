package ru.ppr.cppk.dataCarrier;

import ru.ppr.core.dataCarrier.smartCard.entity.EttData;
import ru.ppr.cppk.dataCarrier.entity.ETTData;

/**
 * @author Aleksandr Brazhkin
 */
public class EttDataMapper {

    public EttDataMapper() {

    }

    public ETTData toLegacyPersonalData(EttData ettData) {

        ETTData legacyEttData = new ETTData();
        legacyEttData.setPassengerCategoryCipher(ettData.getPassengerCategoryCode());
        legacyEttData.setDivisionCode(ettData.getDivisionCode());
        legacyEttData.setOrganizationCode(ettData.getOrganizationCode());
        legacyEttData.setETTNumber(ettData.getEttNumber());
        legacyEttData.setBenefitExpressCipher(ettData.getExemptionExpressCode());
        legacyEttData.setSurname(ettData.getSurname());
        legacyEttData.setFirstName(ettData.getFirstName());
        legacyEttData.setSecondName(ettData.getSecondName());
        legacyEttData.setBirthDate(ettData.getBirthDate());
        legacyEttData.setDocumentIssuingCountry(ettData.getDocumentIssuingCountry());
        legacyEttData.setBirthPlace(ettData.getBirthPlace());
        legacyEttData.setGender(ettData.getGender());
        legacyEttData.setWorkerInitials(ettData.getGuardianFio());
        legacyEttData.setSNILSCode(ettData.getSnilsCode());

        return legacyEttData;
    }
}
