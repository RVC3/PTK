package ru.ppr.chit.domain.model.local;

import java.util.List;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.CarSchemeElementRepository;

/**
 * Схема вагона
 *
 * @author Dmitry Nevolin
 */
public class CarScheme implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
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
    private List<CarSchemeElement> elements;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

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

    public List<CarSchemeElement> getElements(CarSchemeElementRepository carSchemeElementRepository) {
        List<CarSchemeElement> local = elements;
        if (local == null) {
            synchronized (this) {
                if (elements == null) {
                    elements = carSchemeElementRepository.loadAllByCarScheme(id);
                }
            }
            return elements;
        }
        return local;
    }

    public void setElements(List<CarSchemeElement> elements) {
        this.elements = elements;
    }

}
