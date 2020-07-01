package ru.ppr.chit.api.entity;

import java.util.List;

/**
 * @author Dmitry Nevolin
 */
public class CarSchemeEntity {

    /**
     * Высота схемы
     */
    private Integer height;
    /**
     * Ширина схемы
     */
    private Integer width;
    /**
     * Элементы схемы
     */
    private List<CarSchemeElementEntity> elements;

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public List<CarSchemeElementEntity> getElements() {
        return elements;
    }

    public void setElements(List<CarSchemeElementEntity> elements) {
        this.elements = elements;
    }

}
