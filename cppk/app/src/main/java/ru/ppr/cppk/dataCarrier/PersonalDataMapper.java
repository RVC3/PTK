package ru.ppr.cppk.dataCarrier;

import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.cppk.dataCarrier.entity.Gender;

/**
 * @author Aleksandr Brazhkin
 */
public class PersonalDataMapper {

    public PersonalDataMapper() {

    }

    public ru.ppr.cppk.dataCarrier.entity.PersonalData toLegacyPersonalData(PersonalData personalData) {

        ru.ppr.cppk.dataCarrier.entity.PersonalData legacyPersonalData = new ru.ppr.cppk.dataCarrier.entity.PersonalData();

        legacyPersonalData.setSurname(personalData.getSurname());
        legacyPersonalData.setName(personalData.getName());
        legacyPersonalData.setLastName(personalData.getSecondName());
        legacyPersonalData.setBirdthDate(String.valueOf(personalData.getBirthDate()));

        if (personalData.getGender() == PersonalData.Gender.MALE) {
            legacyPersonalData.setGender(Gender.MALE);
        } else if (personalData.getGender() == PersonalData.Gender.FEMALE) {
            legacyPersonalData.setGender(Gender.FEMALE);
        } else {
            legacyPersonalData.setGender(Gender.UNKNOWN);
        }

        return legacyPersonalData;
    }
}
