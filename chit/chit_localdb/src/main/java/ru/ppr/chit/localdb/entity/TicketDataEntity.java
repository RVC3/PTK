package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.SmartCardEntityDao;
import ru.ppr.chit.localdb.greendao.LocationEntityDao;
import ru.ppr.chit.localdb.greendao.PassengerEntityDao;
import ru.ppr.chit.localdb.greendao.TicketDataEntityDao;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.logger.Logger;

/**
 * Данные билета
 *
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = TicketDataEntity.TABLE_NAME)
public class TicketDataEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "TicketData";
    private static final String PassengerIdField = "PassengerId";
    private static final String LocationIdField = "LocationId";
    private static final String SmartCardIdField = "SmartCardId";
    private static final String DepartureDateField = "DepartureDate";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta implements GCOldDataRemovable {

        public Meta(){
            registerReference(PassengerEntity.TABLE_NAME, PassengerIdField);
            registerReference(LocationEntity.TABLE_NAME, LocationIdField);
            registerReference(SmartCardEntity.TABLE_NAME, SmartCardIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public void gcRemoveOldData(Database database, Date dateBefore) {
            // Удаляем старые билеты по дате билета
            StringBuilder sql = new StringBuilder();
            sql.append("delete from ").append(TABLE_NAME).append(" where ").append(DepartureDateField).append(" < ").append(dateBefore.getTime());

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
            database.execSQL(sql.toString());
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "TicketTypeCode")
    private Long ticketTypeCode;
    @Property(nameInDb = PassengerIdField)
    private Long passengerId;
    @ToOne(joinProperty = "passengerId")
    private PassengerEntity passenger;
    @Property(nameInDb = LocationIdField)
    private Long locationId;
    @ToOne(joinProperty = "locationId")
    private LocationEntity location;
    @Property(nameInDb = DepartureDateField)
    private Date departureDate;
    @Property(nameInDb = "DepartureStationCode")
    private Long departureStationCode;
    @Property(nameInDb = "DestinationStationCode")
    private Long destinationStationCode;
    @Property(nameInDb = "TariffId")
    private Long tariffId;
    @Property(nameInDb = "ExemptionExpressCode")
    private Integer exemptionExpressCode;
    @Property(nameInDb = SmartCardIdField)
    private Long smartCardId;
    @ToOne(joinProperty = "smartCardId")
    private SmartCardEntity smartCard;
    @Property(nameInDb = "NsiVersion")
    private int nsiVersion;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 575513464)
    private transient TicketDataEntityDao myDao;
    @Generated(hash = 1288674310)
    public TicketDataEntity(Long id, Long ticketTypeCode, Long passengerId,
            Long locationId, Date departureDate, Long departureStationCode,
            Long destinationStationCode, Long tariffId,
            Integer exemptionExpressCode, Long smartCardId, int nsiVersion) {
        this.id = id;
        this.ticketTypeCode = ticketTypeCode;
        this.passengerId = passengerId;
        this.locationId = locationId;
        this.departureDate = departureDate;
        this.departureStationCode = departureStationCode;
        this.destinationStationCode = destinationStationCode;
        this.tariffId = tariffId;
        this.exemptionExpressCode = exemptionExpressCode;
        this.smartCardId = smartCardId;
        this.nsiVersion = nsiVersion;
    }
    @Generated(hash = 1048685991)
    public TicketDataEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTicketTypeCode() {
        return this.ticketTypeCode;
    }
    public void setTicketTypeCode(Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }
    public Long getPassengerId() {
        return this.passengerId;
    }
    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }
    public Long getLocationId() {
        return this.locationId;
    }
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
    public Date getDepartureDate() {
        return this.departureDate;
    }
    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
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
    public Long getTariffId() {
        return this.tariffId;
    }
    public void setTariffId(Long tariffId) {
        this.tariffId = tariffId;
    }
    public Integer getExemptionExpressCode() {
        return this.exemptionExpressCode;
    }
    public void setExemptionExpressCode(Integer exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }
    public Long getSmartCardId() {
        return this.smartCardId;
    }
    public void setSmartCardId(Long smartCardId) {
        this.smartCardId = smartCardId;
    }
    public int getNsiVersion() {
        return this.nsiVersion;
    }
    public void setNsiVersion(int nsiVersion) {
        this.nsiVersion = nsiVersion;
    }
    @Generated(hash = 2101617364)
    private transient Long passenger__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 2104120453)
    public PassengerEntity getPassenger() {
        Long __key = this.passengerId;
        if (passenger__resolvedKey == null
                || !passenger__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PassengerEntityDao targetDao = daoSession.getPassengerEntityDao();
            PassengerEntity passengerNew = targetDao.load(__key);
            synchronized (this) {
                passenger = passengerNew;
                passenger__resolvedKey = __key;
            }
        }
        return passenger;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 598610400)
    public void setPassenger(PassengerEntity passenger) {
        synchronized (this) {
            this.passenger = passenger;
            passengerId = passenger == null ? null : passenger.getId();
            passenger__resolvedKey = passengerId;
        }
    }
    @Generated(hash = 1068795426)
    private transient Long location__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 912980303)
    public LocationEntity getLocation() {
        Long __key = this.locationId;
        if (location__resolvedKey == null || !location__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LocationEntityDao targetDao = daoSession.getLocationEntityDao();
            LocationEntity locationNew = targetDao.load(__key);
            synchronized (this) {
                location = locationNew;
                location__resolvedKey = __key;
            }
        }
        return location;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 856048568)
    public void setLocation(LocationEntity location) {
        synchronized (this) {
            this.location = location;
            locationId = location == null ? null : location.getId();
            location__resolvedKey = locationId;
        }
    }
    @Generated(hash = 1669578687)
    private transient Long smartCard__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1450171642)
    public SmartCardEntity getSmartCard() {
        Long __key = this.smartCardId;
        if (smartCard__resolvedKey == null
                || !smartCard__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SmartCardEntityDao targetDao = daoSession.getSmartCardEntityDao();
            SmartCardEntity smartCardNew = targetDao.load(__key);
            synchronized (this) {
                smartCard = smartCardNew;
                smartCard__resolvedKey = __key;
            }
        }
        return smartCard;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1734233754)
    public void setSmartCard(SmartCardEntity smartCard) {
        synchronized (this) {
            this.smartCard = smartCard;
            smartCardId = smartCard == null ? null : smartCard.getId();
            smartCard__resolvedKey = smartCardId;
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
    @Generated(hash = 1429673041)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTicketDataEntityDao() : null;
    }

}
