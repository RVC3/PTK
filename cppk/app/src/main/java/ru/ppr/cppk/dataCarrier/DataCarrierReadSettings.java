package ru.ppr.cppk.dataCarrier;

/**
 * Настройки времени считывания карты/ШК.
 * Возможно, нужно от него избавиться.
 */
public class DataCarrierReadSettings {
    // время ожидания карты для считывания
    public static final long RFID_FIND_TIME = 9000;
    // время ожидания ШК для считывания
    public static final long BARCODE_FIND_TIME = 4000;
}
