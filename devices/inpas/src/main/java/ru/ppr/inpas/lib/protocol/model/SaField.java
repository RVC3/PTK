package ru.ppr.inpas.lib.protocol.model;

import android.support.annotation.NonNull;

/**
 * Поля для пакета согласно протоколу SA.
 *
 * @see ru.ppr.inpas.lib.protocol.SaPacket
 */
public enum SaField {
    SAF_UNKNOWN(-1),
    SAF_TRX_AMOUNT(0),
    SAF_TRX_ADD_AMOUNT(1),
    SAF_02_NOT_USED(2),
    SAF_03_NOT_USED(3),
    SAF_TRX_CURRENCY_CODE(4),
    SAF_SETTL_CURRENCY_CODE(5),
    SAF_TRX_DATE_TIME_HOST(6),
    SAF_SETTL_DATE_TIME(7),
    SAF_CARD_ENTRY_MODE(8),
    SAF_PIN_ENTRY_MODE(9),
    SAF_PAN(10),
    SAF_EXPIRY_DATE(11),
    SAF_TRACK2_DATA(12),
    SAF_AUTH_CODE(13),
    SAF_RRN(14),
    SAF_RESPONSE_CODE(15),
    SAF_PIN_DATA(16),
    SAF_WORK_KEY_PIN(17),
    SAF_WORK_KEY_MAC(18),
    SAF_ADDITIONAL_RESPONSE_DATA(19),
    SAF_20_NOT_USED(20),
    SAF_TRX_ORG_DATE_TIME(21),
    SAF_22_NOT_USED(22),
    SAF_TRX_ID(23),
    SAF_HOST_TERMINAL_ID(24),
    SAF_OPERATION_CODE(25),
    SAF_STAN(26),
    SAF_TERMINAL_ID(27),
    SAF_MERCHANT_ID(28),
    SAF_SETTL_DEB_AMN(29),
    SAF_SETTL_DEB_CNT(30),
    SAF_SETTL_CRED_AMN(31),
    SAF_SETTL_CRED_CNT(32),
    SAF_BATCH(33),
    SAF_ORG_OPER_ID(34),
    SAF_35_NOT_USED(35),
    SAF_MAC(36),
    SAF_HOST_ID(37),
    SAF_38_NOT_USED(38),
    SAF_TRX_STATUS(39),
    SAF_TRACK2_DATA_MERCH(40),
    SAF_PIN_DATA_MERCH(41),
    SAF_PAN_MERCH(42),
    SAF_EXP_DATE_MERCH(43),
    SAF_TRACK1_DATA(44),
    SAF_45_NOT_USED(45),
    SAF_TRACK2_MODE(46),
    SAF_CVV2_DATA(47),
    SAF_CRYPT_ALG(48),
    SAF_SETT_DEB_VOID_AMN(49),
    SAF_SETT_DEB_VOID_CNT(50),
    SAF_SETT_CRED_VOID_AMN(51),
    SAF_SETT_CRED_VOID_CNT(52),
    SAF_PROCESSING_FLAG(53),
    SAF_STAN_HOST(54),
    SAF_EMV_DATA(55),
    SAF_RECIPIENT_ADDRESS(56),
    SAF_CARD_TIMEOUT(57),
    SAF_58_NOT_USED(58),
    SAF_CRYPT_DATA(59),
    SAF_FILE_NAME(60),
    SAF_FILE_ID(61),
    SAF_DEVICE_TYPE(62),
    SAF_DEVICE_SER_NUMBER(63),
    SAF_CMD_MODE1(64),
    SAF_CMD_MODE2(65),
    SAF_CMD_MODE3(66),
    SAF_RESULT(67),
    SAF_DATA_LENGTH(68),
    SAF_DATA_MD5(69),
    SAF_FILE_DATA(70),
    SAF_MESSAGE(71),
    SAF_RANDOM(72),
    SAF_DATA_TYPE(73),
    SAF_FILE_DATE_TIME(74),
    SAF_MKEY_ID(75),
    SAF_FORCED_REQUEST(76),
    SAF_FORCED_RESPONSE(77),
    SAF_EMV_FLAG(78),
    SAF_ACCOUNT_TYPE(79),
    SAF_COMMODITY_CODE(80),
    SAF_PAYMENT_DETAILS(81),
    SAF_PROVIDER_CODE(82),
    SAF_83_NOT_USED(83),
    SAF_DEVICE_PHIS_NUMBER(84),
    SAF_LOG_FILE_CMD(85),
    SAF_ADDITIONAL_TRX_DATA(86),
    SAF_RESULT_FILE(87),
    SAF_REPORT_FILE(88),
    SAF_MODEL_NO(89),
    SAF_RECEIPT_DATA(90),
    SAF_PULSAR_TAGS(91),
    SAF_92_NOT_USED(92),
    SAF_93_NOT_USED(93),
    SAF_CURRENT_TIME(94),
    SAF_NEW_PIN_DATA(95),
    SAF_PROCESSING_STEP(96),
    SAF_TRACK3_DATA(97),
    SAF_EMPTY_ITEM_1(98),
    SAF_EMPTY_ITEM_2(99),
    SAF_EMPTY_ITEM_3(100),
    SAF_EMPTY_ITEM_4(101),
    SAF_APPLICATION_SETTING(102),
    SAF_TERMINAL_OS_VERSION(103),
    SAF_BANK_HOST_TERMINAL_ID(104),
    SAF_BANK_NAME(105),
    SAF_DC_RESPONSE_STATUS(106),
    SAF_KKM_COMPLETION_STATUS(107),
    SAF_RECEIPT_NUMBER(108);

    private final int mValue;

    SaField(final int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public static SaField from(final int value) {
        SaField id = SAF_UNKNOWN;

        for (SaField item : SaField.values()) {
            if (item.getValue() == value) {
                id = item;
                break;
            }
        }

        return id;
    }

    /**
     * Метод используется для определения принадлености конткретного поля к строковому типу.
     *
     * @param field конткретного поля.
     * @return результат принадлености конткретного поля к строковому типу.
     */
    public static boolean isStringType(@NonNull final SaField field) {
        switch (field) {
            case SAF_TRX_AMOUNT:
            case SAF_TRX_ADD_AMOUNT:
            case SAF_TRX_CURRENCY_CODE:
            case SAF_SETTL_CURRENCY_CODE:
            case SAF_TRX_DATE_TIME_HOST:
            case SAF_PAN:
            case SAF_EXPIRY_DATE:
            case SAF_TRACK2_DATA:
            case SAF_AUTH_CODE:
            case SAF_RRN:
            case SAF_RESPONSE_CODE:
            case SAF_PIN_DATA:
            case SAF_WORK_KEY_PIN:
            case SAF_WORK_KEY_MAC:
            case SAF_ADDITIONAL_RESPONSE_DATA:
            case SAF_TRX_ORG_DATE_TIME:
            case SAF_TERMINAL_ID:
            case SAF_MERCHANT_ID:
            case SAF_SETTL_DEB_AMN:
            case SAF_SETTL_DEB_CNT:
            case SAF_SETTL_CRED_AMN:
            case SAF_SETTL_CRED_CNT:
            case SAF_MAC:
            case SAF_TRACK2_DATA_MERCH:
            case SAF_PIN_DATA_MERCH:
            case SAF_PAN_MERCH:
            case SAF_EXP_DATE_MERCH:
            case SAF_SETT_DEB_VOID_AMN:
            case SAF_SETT_DEB_VOID_CNT:
            case SAF_SETT_CRED_VOID_AMN:
            case SAF_SETT_CRED_VOID_CNT:
            case SAF_DEVICE_SER_NUMBER:
            case SAF_FILE_DATA:
            case SAF_FORCED_REQUEST:
            case SAF_FORCED_RESPONSE:
            case SAF_ACCOUNT_TYPE:
            case SAF_COMMODITY_CODE:
            case SAF_PAYMENT_DETAILS:
            case SAF_PROVIDER_CODE:
            case SAF_ADDITIONAL_TRX_DATA:
            case SAF_RECEIPT_DATA:
            case SAF_RECEIPT_NUMBER:
                return true;
        }

        return false;
    }

    /**
     * Метод используется для определения принадлености конткретного поля к целочисленному типу.
     *
     * @param field конткретного поля.
     * @return результат принадлености конткретного поля к строковому типу.
     */
    public static boolean isIntegerType(@NonNull final SaField field) {
        switch (field) {
            case SAF_UNKNOWN:
            case SAF_CARD_ENTRY_MODE:
            case SAF_PIN_ENTRY_MODE:
            case SAF_TRX_ID:
            case SAF_OPERATION_CODE:
            case SAF_STAN:
            case SAF_ORG_OPER_ID:
            case SAF_TRX_STATUS:
            case SAF_TRACK2_MODE:
            case SAF_PROCESSING_FLAG:
            case SAF_STAN_HOST:
            case SAF_RECIPIENT_ADDRESS:
            case SAF_CMD_MODE1:
            case SAF_CMD_MODE2:
            case SAF_RESULT:
            case SAF_DC_RESPONSE_STATUS:
                return true;
        }

        return false;
    }

    /**
     * Метод используется для определения принадлености конткретного поля к бинарному типу.
     *
     * @param field конткретного поля.
     * @return результат принадлености конткретного поля к строковому типу.
     */
    public static boolean isBinaryType(@NonNull final SaField field) {
        switch (field) {
            case SAF_DATA_MD5:
            case SAF_FILE_DATA:
            case SAF_CRYPT_DATA:
            case SAF_EMV_DATA:
            case SAF_ADDITIONAL_TRX_DATA:
            case SAF_PULSAR_TAGS:
            case SAF_PROCESSING_STEP:
                return true;
        }

        return false;
    }

}