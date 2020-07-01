package ru.ppr.rfid;

import java.util.Arrays;

import ru.ppr.utils.CommonUtils;

/**
 * Класс - хранилище данных о карте.
 * Используется как результат функции getRfidData.
 */
public class CardData {
    /**
     * "Заголовок" карты - на основе него и COM можно получить UID карты
     */
    private byte[] rfidAttr = null;
    /**
     * UID карты
     */
    private byte[] cardUID = null;
    /**
     * COM - один из параметров позволяющий определить тип карты
     */
    private byte[] com = null;
    // нельзя путать порядок получения иначе все ломается:
    // mifareUlIdentifyType, atqa, sak
    /**
     * MifareUlIdentifyType карты - один из параметров позволяющий определить тип карты
     */
    private byte mifareUlIdentifyType = 0;
    /**
     * ATQA карты - один из параметров позволяющий определить тип карты
     */
    private byte[] atqa = null;
    /**
     * SAK карты - один из параметров позволяющий определить тип карты
     */
    private byte[] sak = null;
    /**
     * Принадлежность карты к семейству Ultralight Ev1, достоверно определяется, если метод mifareUlEv1GetVersion выполняется успешно
     */
    private boolean isEv1 = false;
    /**
     * Физический тип карты
     */
    private MifareCardType mifareCardType = MifareCardType.Unknown;

    public CardData() {

    }

    public CardData(CardData toCopy) {
        this.rfidAttr = toCopy.rfidAttr;
        this.cardUID = toCopy.cardUID;
        this.com = toCopy.com;
        this.mifareUlIdentifyType = toCopy.mifareUlIdentifyType;
        this.atqa = toCopy.atqa;
        this.sak = toCopy.sak;
        this.mifareCardType = toCopy.mifareCardType;
    }

    public byte[] getRfidAttr() {
        return rfidAttr;
    }

    public void setRfidAttr(byte[] rfidAttr) {
        this.rfidAttr = rfidAttr;
    }

    public byte[] getCardUID() {
        return cardUID;
    }

    public void setCardUID(byte[] cardUID) {
        this.cardUID = cardUID;
    }

    public byte[] getCom() {
        return com;
    }

    public void setCom(byte[] com) {
        this.com = com;
    }

    public byte getMifareUlIdentifyType() {
        return mifareUlIdentifyType;
    }

    public void setMifareUlIdentifyType(byte mifareUlIdentifyType) {
        this.mifareUlIdentifyType = mifareUlIdentifyType;
    }

    public byte[] getAtqa() {
        return atqa;
    }

    public int getAtqaInt() {
        return atqa[0] & 0xFF | (atqa[1] & 0xFF) << 8;
    }

    public void setAtqa(byte[] atqa) {
        this.atqa = atqa;
    }

    public byte[] getSak() {
        return sak;
    }

    public int getSakInt() {
        return (int) sak[0];
    }

    public void setSak(byte[] sak) {
        this.sak = sak;
    }

    public MifareCardType getMifareCardType() {
        return mifareCardType;
    }

    public void setMifareCardType(MifareCardType mifareCardType) {
        this.mifareCardType = mifareCardType;
    }

    public boolean isEv1() {
        return isEv1;
    }

    public void setEv1(boolean ev1) {
        isEv1 = ev1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardData cardData = (CardData) o;

        if (mifareUlIdentifyType != cardData.mifareUlIdentifyType) return false;
        if (!Arrays.equals(cardUID, cardData.cardUID)) return false;
        if (!Arrays.equals(com, cardData.com)) return false;
        if (!Arrays.equals(atqa, cardData.atqa)) return false;
        if (!Arrays.equals(sak, cardData.sak)) return false;
        return mifareCardType == cardData.mifareCardType;

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(cardUID);
        result = 31 * result + Arrays.hashCode(com);
        result = 31 * result + (int) mifareUlIdentifyType;
        result = 31 * result + Arrays.hashCode(atqa);
        result = 31 * result + Arrays.hashCode(sak);
        result = 31 * result + mifareCardType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CardData{" +
                "rfidAttr=" + CommonUtils.bytesToHexWithSpaces(rfidAttr) +
                ", cardUID=" + CommonUtils.bytesToHexWithSpaces(cardUID) +
                ", com=" + CommonUtils.bytesToHexWithSpaces(com) +
                ", mifareUlIdentifyType=" + mifareUlIdentifyType +
                ", atqa=" + CommonUtils.bytesToHexWithSpaces(atqa) +
                ", sak=" + CommonUtils.bytesToHexWithSpaces(sak) +
                ", mifareCardType=" + mifareCardType +
                '}';
    }
}
