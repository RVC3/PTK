package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCCascadeLinksRemovable;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.logger.Logger;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = EventEntity.TABLE_NAME)
public class EventEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "Event";
    private static final String CreatedAtField = "CreatedAt";
    private static final String deletedMarkField = "deletedMark";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta implements GCOldDataRemovable, GCCascadeLinksRemovable {
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public void gcRemoveOldData(Database database, Date dateBefore) {
            // Удаляем события по дате создания события
            StringBuilder sql = new StringBuilder();
            sql.append("update ").append(TABLE_NAME).append(" set ").append(deletedMarkField).append(" = 1 ").append(" where ")
                    .append(CreatedAtField).append(" < ").append(dateBefore.getTime());

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
            database.execSQL(sql.toString());
        }

        @Override
        public String getDeletedMarkField() {
            return deletedMarkField;
        }

        @Override
        public boolean gcHandleRemoveCascadeLink(Database database, String referenceTable, String referenceField) {
            // стандартная реализация удаления каскадных ссылок
            return false;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = CreatedAtField)
    private Date createdAt;
    @Property(nameInDb = deletedMarkField)
    private boolean deletedMark;
    @Generated(hash = 559476382)
    public EventEntity(Long id, Date createdAt, boolean deletedMark) {
        this.id = id;
        this.createdAt = createdAt;
        this.deletedMark = deletedMark;
    }
    @Generated(hash = 893269617)
    public EventEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getCreatedAt() {
        return this.createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public boolean getDeletedMark() {
        return this.deletedMark;
    }
    public void setDeletedMark(boolean deletedMark) {
        this.deletedMark = deletedMark;
    }

}
