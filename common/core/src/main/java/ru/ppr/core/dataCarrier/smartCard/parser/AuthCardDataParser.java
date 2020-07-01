package ru.ppr.core.dataCarrier.smartCard.parser;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.entity.AuthCardData;

/**
 * Парсер информации об авторизационной карте.
 *
 * @author Aleksandr Brazhkin
 */
public class AuthCardDataParser {

    private static final int BLOCK_SIZE = 16;

    private static final int PASSWORD_INDEX = 0;
    private static final int PASSWORD_LENGTH = BLOCK_SIZE;
    private static final int EDS_INDEX = BLOCK_SIZE;
    private static final int EDS_LENGTH = BLOCK_SIZE * 4;
    private static final int FIO_INDEX = BLOCK_SIZE * 5;
    private static final int FIO_LENGTH = BLOCK_SIZE * 3;
    private static final int LOGIN_INDEX = BLOCK_SIZE * 8;
    private static final int LOGIN_LENGTH = BLOCK_SIZE;
    private static final int VALIDITY_PERIOD_INDEX = BLOCK_SIZE * 9;
    private static final int VALIDITY_PERIOD_LENGTH = BLOCK_SIZE;
    private static final int FIRST_ROLE_INDEX = BLOCK_SIZE * 10;
    private static final int ROLE_LENGTH = BLOCK_SIZE;
    private static final int ROLE_COUNT = 12;

    public AuthCardDataParser() {

    }

    /**
     * Парсит информацию об авторизационной карте.
     *
     * @param data Сырые данные с карты
     * @return Информация об авторизационной карте
     */
    @NonNull
    public AuthCardData parse(@NonNull byte[] data) {
        AuthCardData authCardData = new AuthCardData();

        if (data.length < BLOCK_SIZE * 22)
            return authCardData;

        byte[] passwordData = DataCarrierUtils.subArray(data, PASSWORD_INDEX, PASSWORD_LENGTH);

        byte[] edsData = DataCarrierUtils.subArray(data, EDS_INDEX, EDS_LENGTH);

        byte[] fioData = DataCarrierUtils.subArray(data, FIO_INDEX, FIO_LENGTH);

        byte[] loginData = DataCarrierUtils.subArray(data, LOGIN_INDEX, LOGIN_LENGTH);

        byte[] validityPeriodData = DataCarrierUtils.subArray(data, VALIDITY_PERIOD_INDEX, VALIDITY_PERIOD_LENGTH);

        byte[] rolesData = DataCarrierUtils.subArray(data, FIRST_ROLE_INDEX, ROLE_LENGTH * ROLE_COUNT);

        authCardData.setPassword(passwordData);
        authCardData.setEds(edsData);
        authCardData.setFio(fioData);
        authCardData.setLogin(loginData);
        authCardData.setValidityPeriod(validityPeriodData);
        authCardData.setRoles(rolesData);

        return authCardData;
    }
}
