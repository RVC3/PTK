package ru.ppr.nsi.entity;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ru.ppr.nsi.NsiDaoSession;

/**
 * Правило доступа.
 *
 * @author Aleksandr Brazhkin
 */
public class AccessRule extends BaseNSIObject<Integer> {

    /**
     * Имя ключа (A/B).
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({KEY_NAME_A,
            KEY_NAME_B})
    public @interface KeyName {
    }

    public static final int KEY_NAME_A = 1;
    public static final int KEY_NAME_B = 2;

    /**
     * Тип ключа (r/w/rw).
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({KEY_TYPE_READ,
            KEY_TYPE_WRITE,
            KEY_TYPE_READ_AND_WRITE})
    public @interface KeyType {
    }

    public static final int KEY_TYPE_READ = 1;
    public static final int KEY_TYPE_WRITE = 2;
    public static final int KEY_TYPE_READ_AND_WRITE = 3;

    /**
     * Код схемы доступа
     */
    private int accessSchemeCode = -1;
    /**
     * Схема доступа
     */
    private AccessScheme accessScheme = null;
    /**
     * Код типа ключа на сам модуле
     */
    private int mifareKeyCode = 0;
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
    @KeyType
    private int keyType;
    /**
     * Тип ключа (A/B)
     */
    @KeyName
    private int keyName;

    public int getAccessSchemeCode() {
        return accessSchemeCode;
    }

    public void setAccessSchemeCode(int accessSchemeCode) {
        this.accessSchemeCode = accessSchemeCode;
    }

    public AccessScheme getAccessScheme(@NonNull NsiDaoSession nsiDaoSession) {
        AccessScheme local = accessScheme;
        if (local == null && accessSchemeCode >= 0) {
            synchronized (this) {
                if (accessScheme == null) {
                    accessScheme = nsiDaoSession.getAccessSchemeDao().load(accessSchemeCode, getVersionId());
                }
            }
            return accessScheme;
        }
        return local;
    }

    public void setAccessScheme(AccessScheme accessScheme) {
        this.accessScheme = accessScheme;
        accessSchemeCode = this.accessScheme == null ? -1 : this.accessScheme.getCode();
    }

    public int getMifareKeyCode() {
        return mifareKeyCode;
    }

    public void setMifareKeyCode(int mifareKeyCode) {
        this.mifareKeyCode = mifareKeyCode;
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

    @KeyType
    public int getKeyType() {
        return keyType;
    }

    public void setKeyType(@KeyType int keyType) {
        this.keyType = keyType;
    }

    @KeyName
    public int getKeyName() {
        return keyName;
    }

    public void setKeyName(@KeyName int keyName) {
        this.keyName = keyName;
    }
}
