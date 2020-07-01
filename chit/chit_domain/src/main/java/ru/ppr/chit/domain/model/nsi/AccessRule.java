package ru.ppr.chit.domain.model.nsi;

import ru.ppr.chit.domain.model.nsi.base.NsiModelWithVD;
import ru.ppr.chit.domain.repository.nsi.AccessSchemeRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Правило доступа.
 *
 * @author Dmitry Nevolin
 */
public class AccessRule implements NsiModelWithVD {

    /**
     * Код схемы доступа
     */
    private Long accessSchemeCode;
    /**
     * Схема доступа
     */
    private AccessScheme accessScheme;
    /**
     * Код типа ключа на сам модуле
     */
    private int mifareKeyCode;
    /**
     * Номер сектора mifare карты
     */
    private int sectorNumber;
    /**
     * Версия ключа
     */
    private int samKeyVersion;
    /**
     * Номер ключа на SAM
     */
    private int cellNumber;
    /**
     * Тип ключа (r/w/rw)
     */
    private KeyType keyType;
    /**
     * Тип ключа (A/B)
     */
    private KeyName keyName;
    /**
     * Версия НСИ
     */
    private int versionId;
    /**
     * Версия удаления из НСИ
     */
    private Integer deleteInVersionId;

    //region AccessScheme getters and setters
    public Long getAccessSchemeCode() {
        return accessSchemeCode;
    }

    public void setAccessSchemeCode(Long accessSchemeCode) {
        this.accessSchemeCode = accessSchemeCode;
        if (this.accessScheme != null && !ObjectUtils.equals(this.accessScheme.getCode(), accessSchemeCode)) {
            this.accessScheme = null;
        }
    }

    public AccessScheme getAccessScheme(AccessSchemeRepository accessSchemeRepository, int nsiVersion) {
        AccessScheme local = accessScheme;
        if (local == null && accessSchemeCode != null) {
            synchronized (this) {
                if (accessScheme == null) {
                    accessScheme = accessSchemeRepository.load(accessSchemeCode, nsiVersion);
                }
            }
            return accessScheme;
        }
        return local;
    }

    public void setAccessScheme(AccessScheme accessScheme) {
        this.accessScheme = accessScheme;
        this.accessSchemeCode = accessScheme != null ? accessScheme.getCode() : null;
    }
    //endregion

    public int getMifareKeyCode() {
        return mifareKeyCode;
    }

    public void setMifareKeyCode(int mifareKeyCode) {
        this.mifareKeyCode = mifareKeyCode;
    }

    public int getSectorNumber() {
        return sectorNumber;
    }

    public void setSectorNumber(int sectorNumber) {
        this.sectorNumber = sectorNumber;
    }

    public int getSamKeyVersion() {
        return samKeyVersion;
    }

    public void setSamKeyVersion(int samKeyVersion) {
        this.samKeyVersion = samKeyVersion;
    }

    public int getCellNumber() {
        return cellNumber;
    }

    public void setCellNumber(int cellNumber) {
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

    @Override
    public int getVersionId() {
        return versionId;
    }

    @Override
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    @Override
    public Integer getDeleteInVersionId() {
        return deleteInVersionId;
    }

    @Override
    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }


    public enum KeyType {

        /**
         * Чтение
         */
        READ(1),
        /**
         * Запись
         */
        WRITE(2),
        /**
         * Чтение и запись
         */
        READ_AND_WRITE(3);

        private final int code;

        KeyType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static KeyType valueOf(int code) {
            for (KeyType keyType : KeyType.values()) {
                if (keyType.getCode() == code) {
                    return keyType;
                }
            }
            return null;
        }

    }

    public enum KeyName {

        /**
         * A
         */
        A(1),
        /**
         * B
         */
        B(2);

        private final int code;

        KeyName(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static KeyName valueOf(int code) {
            for (KeyName keyName : KeyName.values()) {
                if (keyName.getCode() == code) {
                    return keyName;
                }
            }
            return null;
        }

    }

}
