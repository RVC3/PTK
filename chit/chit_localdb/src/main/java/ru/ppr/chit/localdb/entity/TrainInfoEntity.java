package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.Date;
import java.util.List;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.CarInfoEntityDao;
import ru.ppr.chit.localdb.greendao.StationInfoEntityDao;
import ru.ppr.chit.localdb.greendao.TrainInfoEntityDao;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCCascadeLinksRemovable;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = TrainInfoEntity.TABLE_NAME)
public class TrainInfoEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "TrainInfo";
    private static final String DepartureDateField = "DepartureDate";
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
        public String getDeletedMarkField() {
            return deletedMarkField;
        }

        @Override
        public void gcRemoveOldData(Database database, Date dateBefore) {
            // Помечаем для удаления старые поезда по дате отправления
            StringBuilder sql = new StringBuilder();
            sql.append("update ").append(TABLE_NAME).append(" set ").append(deletedMarkField).append(" = 1 ").append(" where ")
                    .append(DepartureDateField).append(" < ").append(dateBefore.getTime());

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
            database.execSQL(sql.toString());
        }

        @Override
        public boolean gcHandleRemoveCascadeLink(Database database, String referenceTable, String referenceField) {
            return false;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "TrainThreadId")
    private String trainThreadId;
    @Property(nameInDb = "TrainNumber")
    private String trainNumber;
    @Property(nameInDb = "DepartureStationCode")
    private Long departureStationCode;
    @Property(nameInDb = "DestinationStationCode")
    private Long destinationStationCode;
    @Property(nameInDb = DepartureDateField)
    private Date departureDate;
    @Property(nameInDb = "DestinationDate")
    private Date destinationDate;
    @ToMany(referencedJoinProperty = "trainInfoId")
    @OrderBy("number ASC")
    private List<StationInfoEntity> stations;
    @ToMany(referencedJoinProperty = "trainInfoId")
    private List<CarInfoEntity> cars;
    @Property(nameInDb = "Legacy")
    private boolean legacy;
    @Property(nameInDb = deletedMarkField)
    private boolean deletedMark;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1449592714)
    private transient TrainInfoEntityDao myDao;
    @Generated(hash = 735536107)
    public TrainInfoEntity(Long id, String trainThreadId, String trainNumber, Long departureStationCode, Long destinationStationCode,
            Date departureDate, Date destinationDate, boolean legacy, boolean deletedMark) {
        this.id = id;
        this.trainThreadId = trainThreadId;
        this.trainNumber = trainNumber;
        this.departureStationCode = departureStationCode;
        this.destinationStationCode = destinationStationCode;
        this.departureDate = departureDate;
        this.destinationDate = destinationDate;
        this.legacy = legacy;
        this.deletedMark = deletedMark;
    }
    @Generated(hash = 1952556835)
    public TrainInfoEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTrainThreadId() {
        return this.trainThreadId;
    }
    public void setTrainThreadId(String trainThreadId) {
        this.trainThreadId = trainThreadId;
    }
    public String getTrainNumber() {
        return this.trainNumber;
    }
    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }
    public Long getDepartureStationCode() {
        return this.departureStationCode;
    }
    public void setDepartureStationCode(Long departureStationCode) {
        this.departureStationCode = departureStationCode;
    }
    public Long getDestinationStationCode() {
        return this.destinationStationCode;
    }
    public void setDestinationStationCode(Long destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }
    public Date getDepartureDate() {
        return this.departureDate;
    }
    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }
    public Date getDestinationDate() {
        return this.destinationDate;
    }
    public void setDestinationDate(Date destinationDate) {
        this.destinationDate = destinationDate;
    }
    public boolean getLegacy() {
        return this.legacy;
    }
    public void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 317697616)
    public List<StationInfoEntity> getStations() {
        if (stations == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            StationInfoEntityDao targetDao = daoSession.getStationInfoEntityDao();
            List<StationInfoEntity> stationsNew = targetDao
                    ._queryTrainInfoEntity_Stations(id);
            synchronized (this) {
                if (stations == null) {
                    stations = stationsNew;
                }
            }
        }
        return stations;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 307598014)
    public synchronized void resetStations() {
        stations = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 759429784)
    public List<CarInfoEntity> getCars() {
        if (cars == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CarInfoEntityDao targetDao = daoSession.getCarInfoEntityDao();
            List<CarInfoEntity> carsNew = targetDao._queryTrainInfoEntity_Cars(id);
            synchronized (this) {
                if (cars == null) {
                    cars = carsNew;
                }
            }
        }
        return cars;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1055213807)
    public synchronized void resetCars() {
        cars = null;
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
    @Generated(hash = 694900550)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTrainInfoEntityDao() : null;
    }
    public boolean getDeletedMark() {
        return this.deletedMark;
    }
    public void setDeletedMark(boolean deletedMark) {
        this.deletedMark = deletedMark;
    }


}
