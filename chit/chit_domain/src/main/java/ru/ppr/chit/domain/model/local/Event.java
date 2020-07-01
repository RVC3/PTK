package ru.ppr.chit.domain.model.local;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * Событие
 *
 * @author Dmitry Nevolin
 */
public class Event implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Дата создания события
     */
    private Date createdAt;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
