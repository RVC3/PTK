package ru.ppr.nsi.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Регион.
 *
 * @author Aleksandr Brazhkin
 */
public class Region extends BaseNSIObject<Integer> {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MOSCOW, MOSCOW_REGION})
    public @interface Code {
    }

    public static final int MOSCOW = 77; // Москва
    public static final int MOSCOW_REGION = 50; // Московская область

    /**
     * ОКАТО код
     */
    private String regionOkatoCode;
    /**
     * Наименование
     */
    private String name;

    public Region() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionOkatoCode() {
        return regionOkatoCode;
    }

    public void setRegionOkatoCode(String regionOkatoCode) {
        this.regionOkatoCode = regionOkatoCode;
    }
}
