package ru.ppr.rfid;

/**
 * Правило доступа к сектору смарт-карты Mifare Classic с использованием SAM-модуля.
 *
 * @author Aleksandr Brazhkin
 */
public class SamAccessRule {
    /**
     * Номер слота sam-модуля
     */
    private int samSlotNumber;
    /**
     * Номер сектора mifare карты
     */
    private byte sectorNumber;
    /**
     * Версия ключа
     */
    private byte samKeyVersion;
    /**
     * Номер ключа на SAM
     */
    private byte cellNumber;
    /**
     * Тип ключа (A/B)
     */
    private int keyName;

    public SamAccessRule() {

    }

    public SamAccessRule(int samSlotNumber, byte sectorNumber, byte samKeyVersion, byte cellNumber, int keyName) {
        this.samSlotNumber = samSlotNumber;
        this.sectorNumber = sectorNumber;
        this.samKeyVersion = samKeyVersion;
        this.cellNumber = cellNumber;
        this.keyName = keyName;
    }

    public int getSamSlotNumber() {
        return samSlotNumber;
    }

    public void setSamSlotNumber(int samSlotNumber) {
        this.samSlotNumber = samSlotNumber;
    }

    public byte getSectorNumber() {
        return sectorNumber;
    }

    public void setSectorNumber(byte sectorNumber) {
        this.sectorNumber = sectorNumber;
    }

    public byte getSamKeyVersion() {
        return samKeyVersion;
    }

    public void setSamKeyVersion(byte samKeyVersion) {
        this.samKeyVersion = samKeyVersion;
    }

    public byte getCellNumber() {
        return cellNumber;
    }

    public void setCellNumber(byte cellNumber) {
        this.cellNumber = cellNumber;
    }

    public int getKeyName() {
        return keyName;
    }

    public void setKeyName(int keyName) {
        this.keyName = keyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SamAccessRule that = (SamAccessRule) o;

        if (samSlotNumber != that.samSlotNumber) return false;
        if (sectorNumber != that.sectorNumber) return false;
        if (samKeyVersion != that.samKeyVersion) return false;
        if (cellNumber != that.cellNumber) return false;
        return keyName == that.keyName;

    }

    @Override
    public int hashCode() {
        int result = samSlotNumber;
        result = 31 * result + (int) sectorNumber;
        result = 31 * result + (int) samKeyVersion;
        result = 31 * result + (int) cellNumber;
        result = 31 * result + keyName;
        return result;
    }
}
