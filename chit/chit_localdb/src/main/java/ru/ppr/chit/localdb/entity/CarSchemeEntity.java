package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import ru.ppr.chit.localdb.daosession.LocalDaoEntities;
import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.CarSchemeElementEntityDao;
import ru.ppr.chit.localdb.greendao.CarSchemeEntityDao;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.DBGarbageCollectorHelper;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = CarSchemeEntity.TABLE_NAME)
public class CarSchemeEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "CarScheme";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta implements GCNoLinkRemovable {
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public boolean gcHandleNoLinkRemoveData(Database database) {
            // Переопределяем стандартный алгоритм сборщика мусора

            // Удаляем схемы, если у соответствующей схемы нет вагонов (уже удалены вместе с поездом)
            ReferenceInfo carInfoReference = LocalDaoEntities.references.getLink(CarInfoEntity.TABLE_NAME, CarInfoEntity.SchemeIdField);
            String sql = DBGarbageCollectorHelper.getDeleteNoLinkSql(carInfoReference, TABLE_NAME);

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcHandleNoLinkRemoveData(): execute sql" + "\n" + sql);
            database.execSQL(sql);

            // Удаляем элементы не существующих схем (мастер ссылка)
            ReferenceInfo schemeElementReference = LocalDaoEntities.references.getLink(CarSchemeElementEntity.TABLE_NAME, CarSchemeElementEntity.CarSchemeIdField);
            sql = DBGarbageCollectorHelper.getDeleteNoMasterLinkSql(schemeElementReference, CarSchemeElementEntity.TABLE_NAME);

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcHandleNoLinkRemoveData(): execute sql" + "\n" + sql);
            database.execSQL(sql);

            return true;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "Height")
    private int height;
    @Property(nameInDb = "Width")
    private int width;
    @ToMany(referencedJoinProperty = "carSchemeId")
    private List<CarSchemeElementEntity> elements;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1125263380)
    private transient CarSchemeEntityDao myDao;
    @Generated(hash = 584139590)
    public CarSchemeEntity(Long id, int height, int width) {
        this.id = id;
        this.height = height;
        this.width = width;
    }
    @Generated(hash = 696599408)
    public CarSchemeEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getHeight() {
        return this.height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public int getWidth() {
        return this.width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1564319889)
    public List<CarSchemeElementEntity> getElements() {
        if (elements == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CarSchemeElementEntityDao targetDao = daoSession
                    .getCarSchemeElementEntityDao();
            List<CarSchemeElementEntity> elementsNew = targetDao
                    ._queryCarSchemeEntity_Elements(id);
            synchronized (this) {
                if (elements == null) {
                    elements = elementsNew;
                }
            }
        }
        return elements;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1940756541)
    public synchronized void resetElements() {
        elements = null;
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1403186897)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCarSchemeEntityDao() : null;
    }

}
