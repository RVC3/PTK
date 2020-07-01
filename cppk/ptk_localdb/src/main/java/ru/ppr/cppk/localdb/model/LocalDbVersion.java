package ru.ppr.cppk.localdb.model;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Версия структуры локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public class LocalDbVersion implements LocalModelWithId<Long> {
    /**
     * Порядковый номер версии
     */
    private Long id;
    /**
     * Дата обновления структуры
     */
    private Date createdDateTime;
    /**
     * Описание новой версии структуры
     */
    private String description;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
