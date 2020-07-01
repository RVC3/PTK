package ru.ppr.cppk.legacy;

import ru.ppr.nsi.entity.AccessScheme;

/**
 * Возможные слоты SAM-модуля.
 * <p/>
 * Есть 1й - тот что выше второго
 * Есть 2й - тот что у самой платы, его не видно пока не вытащишь 1й
 */
public enum SamSlot {

    First((byte) AccessScheme.SAM_SLOT_NUMBER_1, (byte) 1, "первый"),

    Second((byte) AccessScheme.SAM_SLOT_NUMBER_2, (byte) 2, "второй");

    private byte dbNumber;
    private byte realNumber;
    private String description;

    private SamSlot(byte dbNumber, byte realNumber, String description) {
        this.dbNumber = dbNumber;
        this.realNumber = realNumber;
        this.description = description;
    }

    /**
     * Вернет номер слота, так как он приходит из БД (0 или 1)
     */
    public int getDbNumber() {
        return dbNumber;
    }

    /**
     * Вернет порядковый номер слота (1 или 2)
     */
    public int getRealNumber() {
        return realNumber;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Вернет объект слота
     *
     * @param realNumber - возможные значения 1 или 2
     */
    public static SamSlot getByRealNumber(int realNumber) {
        for (SamSlot keyType : SamSlot.values()) {
            if (keyType.getRealNumber() == realNumber) {
                return keyType;
            }
        }
        return First;
    }

}