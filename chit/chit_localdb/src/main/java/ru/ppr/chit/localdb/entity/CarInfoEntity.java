package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.CarSchemeEntityDao;
import ru.ppr.chit.localdb.greendao.CarInfoEntityDao;
import ru.ppr.database.references.ReferenceInfo;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = CarInfoEntity.TABLE_NAME)
public class CarInfoEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "CarInfo";
    private static final String TrainInfoIdField = "TrainInfoId";
    public static final String SchemeIdField = "SchemeId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(TrainInfoEntity.TABLE_NAME, TrainInfoIdField, ReferenceInfo.ReferencesType.CASCADE);
            registerReference(CarSchemeEntity.TABLE_NAME, SchemeIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = TrainInfoIdField)
    private Long trainInfoId;
    @Property(nameInDb = "Number")
    private String number;
    @Property(nameInDb = SchemeIdField)
    private Long schemeId;
    @ToOne(joinProperty = "schemeId")
    private CarSchemeEntity scheme;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 2066827038)
    private transient CarInfoEntityDao myDao;
    @Generated(hash = 1971589016)
    public CarInfoEntity(Long id, Long trainInfoId, String number, Long schemeId) {
        this.id = id;
        this.trainInfoId = trainInfoId;
        this.number = number;
        this.schemeId = schemeId;
    }
    @Generated(hash = 566385202)
    public CarInfoEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTrainInfoId() {
        return this.trainInfoId;
    }
    public void setTrainInfoId(Long trainInfoId) {
        this.trainInfoId = trainInfoId;
    }
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public Long getSchemeId() {
        return this.schemeId;
    }
    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }
    @Generated(hash = 1589641699)
    private transient Long scheme__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1100745492)
    public CarSchemeEntity getScheme() {
        Long __key = this.schemeId;
        if (scheme__resolvedKey == null || !scheme__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CarSchemeEntityDao targetDao = daoSession.getCarSchemeEntityDao();
            CarSchemeEntity schemeNew = targetDao.load(__key);
            synchronized (this) {
                scheme = schemeNew;
                scheme__resolvedKey = __key;
            }
        }
        return scheme;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 327092862)
    public void setScheme(CarSchemeEntity scheme) {
        synchronized (this) {
            this.scheme = scheme;
            schemeId = scheme == null ? null : scheme.getId();
            scheme__resolvedKey = schemeId;
        }
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
    @Generated(hash = 283057355)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCarInfoEntityDao() : null;
    }

}
