package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.CarSchemeRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Информация о вагоне
 *
 * @author Dmitry Nevolin
 */
public class CarInfo implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Номер вагона
     */
    private String number;
    /**
     * Идентификатор scheme
     */
    private Long schemeId;
    /**
     * Схема вагона
     */
    private CarScheme scheme;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Long getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
        if (this.scheme != null && !ObjectUtils.equals(this.scheme.getId(), schemeId)) {
            this.scheme = null;
        }
    }

    public CarScheme getScheme(CarSchemeRepository carSchemeRepository) {
        CarScheme local = scheme;
        if (local == null && schemeId != null) {
            synchronized (this) {
                if (scheme == null) {
                    scheme = carSchemeRepository.load(schemeId);
                }
            }
            return scheme;
        }
        return local;
    }

    public void setScheme(CarScheme scheme) {
        this.scheme = scheme;
        this.schemeId = scheme != null ? scheme.getId() : null;
    }

}
