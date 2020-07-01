package ru.ppr.rfid;

/**
 * Класс физического типа mifare карты.
 * https://docs.google.com/spreadsheets/d/1qV5gaDi9HjPpkP4pTHZFZse7nrBIF-XgcpndQqe0RKM/edit#gid=0
 *
 * @author G.Kashka
 */
public enum MifareCardType {

    Unknown,
    Mifare_Classic_1K,
    Mifare_Classic_4K,
    Mifare_Plus_2K,
    Mifare_Plus_4K,
    UltralightC,
    UltralightEV1;

    /**
     * Возвращает размер карты в байтах
     */
    public int getCardSize() {
        int out = 0;
        if (this == Mifare_Classic_1K) out = 1024;
        else if (this == Mifare_Classic_4K) out = (4 * 1024);
        else if (this == Mifare_Plus_2K) out = (2 * 1024);
        else if (this == Mifare_Plus_4K) out = (4 * 1024);
        else if (this == UltralightC) out = 192;
        else if (this == UltralightEV1) out = 168;
        return out;
    }

    /**
     * Проверяет принадлежность карты к типу Ultralight
     */
    public boolean isUltralight() {
        return this == UltralightC || this == UltralightEV1;
    }

    /**
     * Проверяет принадлежность карты к типу Classic
     */
    public boolean isClassic() {
        if (this == Unknown) return false;
        else if (isUltralight()) return false;
        return true;
    }

    /**
     * Проверяет принадлежность карты к семейству 1k - используется для разделения СКМ карт
     * https://aj.srvdev.ru/browse/CPPKPP-29985
     */
    public boolean isClassic1k() {
        return isClassic() && getCardSize() == 1024;
    }

}
