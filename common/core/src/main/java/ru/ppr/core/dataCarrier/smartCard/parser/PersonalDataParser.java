package ru.ppr.core.dataCarrier.smartCard.parser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Парсер персональных данных с карт СКМ, СКМО, ИПК.
 *
 * @author Aleksandr Brazhkin
 */
public class PersonalDataParser {

    private static final String TAG = Logger.makeLogTag(PersonalDataParser.class);

    private static final String ENCODING_WIN_1251 = "windows-1251";
    private static final String ENCODING_ACII = "ASCII";

    private static final int SURNAME_LENGTH_INDEX = 0;
    private static final int SURNAME_START_INDEX = 1;
    private static final int NAME_AND_SECOND_NAME_LENGTH_INDEX = 48;
    private static final int NAME_AND_SECOND_NAME_START_INDEX = 49;
    private static final int BIRTH_DATE_LENGTH_INDEX = 38;
    private static final int BIRTH_DATE_START_INDEX = 39;

    private static final int GENDER_LENGTH_INDEX = 35;
    private static final int GENDER_START_INDEX = 36;

    public PersonalDataParser() {

    }

    /**
     * Парсит персональные данные.
     *
     * @param data Данные ПД
     * @return Персональные данные
     */
    public PersonalData parse(byte[] data) {

        PersonalData personalData = new PersonalData();

        if (data==null || data.length < 96)
            return personalData;

        Logger.debug(TAG, "parse personal data: "+ CommonUtils.bytesToHex(data));

        // Фамилия

        byte surnameLengthData = data[SURNAME_LENGTH_INDEX];
        int surnameLength = getLengthAsInt(surnameLengthData);
        byte[] surnameData = DataCarrierUtils.subArray(data, SURNAME_START_INDEX, surnameLength);
        String surname = null;
        try {
            surname = new String(surnameData, ENCODING_WIN_1251).trim();
        } catch (UnsupportedEncodingException e) {
            Logger.error(TAG, e);
        }

        // Имя и отчество

        byte nameAndPatronymicLengthData = data[NAME_AND_SECOND_NAME_LENGTH_INDEX];
        int nameAndPatronymicLength = getLengthAsInt(nameAndPatronymicLengthData);

        if(nameAndPatronymicLength + NAME_AND_SECOND_NAME_START_INDEX >= data.length)
            return personalData;

        byte[] nameAndPatronymicData = DataCarrierUtils.subArray(data, NAME_AND_SECOND_NAME_START_INDEX, nameAndPatronymicLength);
        String nameAndSecondName = null;
        try {
            nameAndSecondName = new String(nameAndPatronymicData, ENCODING_WIN_1251).trim();
        } catch (UnsupportedEncodingException e) {
            Logger.error(TAG, e);
        }
        String[] nameAndPatronymicArray = splitNameAndPatronymic(nameAndSecondName);
        String name = nameAndPatronymicArray[0] == null ? null : nameAndPatronymicArray[0].trim();
        String patronymic = nameAndPatronymicArray[1] == null ? null : nameAndPatronymicArray[1].trim();

        // Дата рождения

        byte birthDateLengthData = data[BIRTH_DATE_LENGTH_INDEX];
        int birthDateLength = getLengthAsInt(birthDateLengthData);
        if(BIRTH_DATE_START_INDEX + birthDateLength >= data.length)
            return personalData;

        byte[] birthDateData = DataCarrierUtils.subArray(data, BIRTH_DATE_START_INDEX, birthDateLength);
        String birthDateString = null;
        try {
            birthDateString = new String(birthDateData, ENCODING_ACII);
        } catch (UnsupportedEncodingException e) {
            Logger.error(TAG, e);
        }
        Date birthDate = parseBirthDate(birthDateString);

        // Пол

        byte genderLengthData = data[GENDER_LENGTH_INDEX];
        int genderLength = getLengthAsInt(genderLengthData);

        if(genderLength + GENDER_START_INDEX >= data.length)
            return personalData;

        byte[] genderData = DataCarrierUtils.subArray(data, GENDER_START_INDEX, genderLength);
        String genderString = null;
        try {
            genderString = new String(genderData, ENCODING_WIN_1251);
        } catch (UnsupportedEncodingException e) {
            Logger.error(TAG, e);
        }

        PersonalData.Gender gender;
        if ("M".equalsIgnoreCase(genderString)) {
            gender = PersonalData.Gender.MALE;
        } else if ("F".equalsIgnoreCase(genderString)) {
            gender = PersonalData.Gender.FEMALE;
        } else {
            gender = null;
        }

        // Заполнение

        personalData.setSurname(surname);
        personalData.setName(name);
        personalData.setSecondName(patronymic);
        personalData.setBirthDate(birthDate);
        personalData.setGender(gender);

        return personalData;
    }

    /**
     * Убирает из байта 2 бита с левой стороны, заменяя их на нули.
     * <p/>
     * Например было: 11010110
     * стало: 00010110
     *
     * @param lengthByte
     * @return
     */
    private int getLengthAsInt(byte lengthByte) {
        return lengthByte & 0x3f;
    }

    /**
     * Парсит дату рождения.
     *
     * @param stringDate
     * @return
     */
    @Nullable
    private Date parseBirthDate(@Nullable String stringDate) {
        Date date = null;
        try {
            if (stringDate != null) {
                Log.d(TAG, "parseBirthDate: " + stringDate);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                date = simpleDateFormat.parse(stringDate);

            }
            if (date != null)
                return date;
            else {
                date = (new SimpleDateFormat("yyyy-MM-dd")).parse("2100-01-01");
                System.out.println(date.toString());
                return date;

            }
        } catch (ParseException e) {
            Log.e(TAG, "parseBirthDate: ", e);
            date = new Date();
        }
        return date;
    }

    /**
     * Парсит имя и отчество.
     *
     * @param nameAndPatronymic
     * @return
     */
    @NonNull
    private String[] splitNameAndPatronymic(@Nullable String nameAndPatronymic) {
        String[] nameAndPatronymicArray = new String[2];
        String name = null;
        String patronymic = null;
        if (nameAndPatronymic != null) {
            int spaceIndex = nameAndPatronymic.indexOf(' ');
            if (spaceIndex < 0) {
                name = nameAndPatronymic;
                patronymic = null;
            } else {
                name = nameAndPatronymic.substring(0, spaceIndex);
                patronymic = nameAndPatronymic.substring(spaceIndex + 1, nameAndPatronymic.length());
            }
        }
        nameAndPatronymicArray[0] = name;
        nameAndPatronymicArray[1] = patronymic;
        return nameAndPatronymicArray;
    }
}
