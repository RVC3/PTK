package ru.ppr.rfid;

import java.util.Arrays;

/**
 * Правило доступа к сектору смарт-карты Mifare Classic с использованием статического ключа.
 *
 * @author Aleksandr Brazhkin
 */
public class StaticKeyAccessRule {
    /**
     * Ключ
     */
    private byte[] key;
    /**
     * Тип ключа (A/B)
     */
    private int keyName;

    public StaticKeyAccessRule() {

    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
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

        StaticKeyAccessRule that = (StaticKeyAccessRule) o;

        if (keyName != that.keyName) return false;
        return Arrays.equals(key, that.key);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(key);
        result = 31 * result + keyName;
        return result;
    }
}
