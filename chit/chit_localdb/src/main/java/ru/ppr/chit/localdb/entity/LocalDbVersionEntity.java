package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;

/**
 * Версия структуры локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = LocalDbVersionEntity.TABLE_NAME)
public class LocalDbVersionEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "LocalDbVersion";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    @Id(autoincrement = false)
    private Long id;
    @Property(nameInDb = "UpgradeDateTime")
    private long upgradeDateTime;
    @Property(nameInDb = "Description")
    private String description;

    @Generated(hash = 1226996736)
    public LocalDbVersionEntity(Long id, long upgradeDateTime, String description) {
        this.id = id;
        this.upgradeDateTime = upgradeDateTime;
        this.description = description;
    }

    @Generated(hash = 790488041)
    public LocalDbVersionEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUpgradeDateTime() {
        return this.upgradeDateTime;
    }

    public void setUpgradeDateTime(long upgradeDateTime) {
        this.upgradeDateTime = upgradeDateTime;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
