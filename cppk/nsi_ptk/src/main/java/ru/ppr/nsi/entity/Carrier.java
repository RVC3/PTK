package ru.ppr.nsi.entity;

/**
 * Перевозчик.
 *
 * @author Brazhkin A.V.
 */
public class Carrier extends BaseNSIObject<String> {

    public static final String CPPK_CODE = "0015";

    /**
     * Наименование
     */
    private String name;
    /**
     * Краткое наименование
     */
    private String shortName;
    /**
     * ИНН
     */
    private String inn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }
}
