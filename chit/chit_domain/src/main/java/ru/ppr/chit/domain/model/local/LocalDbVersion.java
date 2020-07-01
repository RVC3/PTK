package ru.ppr.chit.domain.model.local;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

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
    private Date upgradeDateTime;
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

    public Date getUpgradeDateTime() {
        return upgradeDateTime;
    }

    public void setUpgradeDateTime(Date upgradeDateTime) {
        this.upgradeDateTime = upgradeDateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
