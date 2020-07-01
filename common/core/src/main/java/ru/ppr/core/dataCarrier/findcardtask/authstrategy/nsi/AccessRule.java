package ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi;

/**
 * @author Aleksandr Brazhkin
 */
public class AccessRule {

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
     * Тип ключа (r/w/rw)
     */
    private KeyType keyType;
    /**
     * Тип ключа (A/B)
     */
    private KeyName keyName;

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

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public KeyName getKeyName() {
        return keyName;
    }

    public void setKeyName(KeyName keyName) {
        this.keyName = keyName;
    }

    public enum KeyType {
        READ,
        WRITE,
        READ_AND_WRITE
    }

    public enum KeyName {
        A(1),
        B(2);

        private final int code;

        KeyName(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    @Override
    public boolean equals(Object rule) {
        // Сравнение на себя
        if (rule == this) {
            return true;
        }

        /* Сравнение на другой тип */
        if (!(rule instanceof AccessRule)) {
            return false;
        }

        AccessRule other = (AccessRule) rule;

        return  sectorNumber == other.sectorNumber &&
                samKeyVersion == other.samKeyVersion &&
                cellNumber == other.cellNumber &&
                keyType == other.keyType &&
                keyName == other.keyName;

    }
}
