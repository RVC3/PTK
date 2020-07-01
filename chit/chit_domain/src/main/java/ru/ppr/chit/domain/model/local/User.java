package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * Пользователь
 *
 * @author Dmitry Nevolin
 */
public class User implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Имя в свободном формате
     */
    private String name;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
