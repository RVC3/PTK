package ru.ppr.nsi.entity;

/**
 * Ж/Д направление.
 *
 * @author Aleksandr Brazhkin
 */
public class Direction extends BaseNSIObject<Long> {
    /**
     * Наименование
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
