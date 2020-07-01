package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;

import ru.ppr.chit.localdb.daosession.LocalDaoEntities;
import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import ru.ppr.chit.localdb.greendao.DaoSession;
import ru.ppr.chit.localdb.greendao.TicketBoardingEntityDao;
import ru.ppr.chit.localdb.greendao.TicketDataEntityDao;
import ru.ppr.chit.localdb.greendao.TicketIdEntityDao;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.DBGarbageCollectorHelper;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = TicketBoardingEntity.TABLE_NAME)
public class TicketBoardingEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "TicketBoarding";
    private static final String TicketIdIdField = "TicketIdId";
    private static final String TicketDataIdField = "TicketDataId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta implements GCNoLinkRemovable {

        public Meta(){
            registerReference(TicketIdEntity.TABLE_NAME, TicketIdIdField);
            registerReference(TicketDataEntity.TABLE_NAME, TicketDataIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public boolean gcHandleNoLinkRemoveData(Database database) {
            // Дополняем стандартный алгоритм сборщика мусора

            // Удаляем посадки, на которые нет ссылок в события контроля TicketControlEvent
            ReferenceInfo ticketControlEventReference = LocalDaoEntities.references.getLink(TicketControlEventEntity.TABLE_NAME, TicketControlEventEntity.TicketBoardingIdField);
            String sql = DBGarbageCollectorHelper.getDeleteNoLinkSql(ticketControlEventReference, TABLE_NAME);

            Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcHandleNoLinkRemoveData(): execute sql" + "\n" + sql);
            database.execSQL(sql);

            return true;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = TicketIdIdField)
    private Long ticketIdId;
    @ToOne(joinProperty = "ticketIdId")
    private TicketIdEntity ticketId;
    @Property(nameInDb = "TrainNumber")
    private String trainNumber;
    @Property(nameInDb = "TrainThreadId")
    private String trainThreadId;
    @Property(nameInDb = "TerminalDeviceId")
    private String terminalDeviceId;
    @Property(nameInDb = "OperatorName")
    private String operatorName;
    @Property(nameInDb = "ControlStationCode")
    private Long controlStationCode;
    @Property(nameInDb = "EdsKeyNumber")
    private long edsKeyNumber;
    @Property(nameInDb = "EdsValid")
    private boolean edsValid;
    @Property(nameInDb = "InWhiteList")
    private boolean inWhiteList;
    @Property(nameInDb = "StopListRefusalCode")
    private Long stopListRefusalCode;
    @Property(nameInDb = "CheckDate")
    private Date checkDate;
    @Property(nameInDb = "WasBoarded")
    private boolean wasBoarded;
    @Property(nameInDb = TicketDataIdField)
    private Long ticketDataId;
    @Property(nameInDb = "BoardingByList")
    private boolean boardingByList;
    @ToOne(joinProperty = "ticketDataId")
    private TicketDataEntity ticketData;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1838765246)
    private transient TicketBoardingEntityDao myDao;

    @Generated(hash = 1551008148)
    public TicketBoardingEntity(Long id, Long ticketIdId, String trainNumber, String trainThreadId,
            String terminalDeviceId, String operatorName, Long controlStationCode, long edsKeyNumber,
            boolean edsValid, boolean inWhiteList, Long stopListRefusalCode, Date checkDate,
            boolean wasBoarded, Long ticketDataId, boolean boardingByList) {
        this.id = id;
        this.ticketIdId = ticketIdId;
        this.trainNumber = trainNumber;
        this.trainThreadId = trainThreadId;
        this.terminalDeviceId = terminalDeviceId;
        this.operatorName = operatorName;
        this.controlStationCode = controlStationCode;
        this.edsKeyNumber = edsKeyNumber;
        this.edsValid = edsValid;
        this.inWhiteList = inWhiteList;
        this.stopListRefusalCode = stopListRefusalCode;
        this.checkDate = checkDate;
        this.wasBoarded = wasBoarded;
        this.ticketDataId = ticketDataId;
        this.boardingByList = boardingByList;
    }

    @Generated(hash = 710040688)
    public TicketBoardingEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketIdId() {
        return this.ticketIdId;
    }

    public void setTicketIdId(Long ticketIdId) {
        this.ticketIdId = ticketIdId;
    }

    public String getTrainNumber() {
        return this.trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainThreadId() {
        return this.trainThreadId;
    }

    public void setTrainThreadId(String trainThreadId) {
        this.trainThreadId = trainThreadId;
    }

    public String getTerminalDeviceId() {
        return this.terminalDeviceId;
    }

    public void setTerminalDeviceId(String terminalDeviceId) {
        this.terminalDeviceId = terminalDeviceId;
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Long getControlStationCode() {
        return this.controlStationCode;
    }

    public void setControlStationCode(Long controlStationCode) {
        this.controlStationCode = controlStationCode;
    }

    public long getEdsKeyNumber() {
        return this.edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }

    public boolean getEdsValid() {
        return this.edsValid;
    }

    public void setEdsValid(boolean edsValid) {
        this.edsValid = edsValid;
    }

    public boolean getInWhiteList() {
        return this.inWhiteList;
    }

    public void setInWhiteList(boolean inWhiteList) {
        this.inWhiteList = inWhiteList;
    }

    public boolean isBoardingByList() {
        return boardingByList;
    }

    public void setBoardingByList(boolean boardingByList) {
        this.boardingByList = boardingByList;
    }

    public Long getStopListRefusalCode() {
        return this.stopListRefusalCode;
    }

    public void setStopListRefusalCode(Long stopListRefusalCode) {
        this.stopListRefusalCode = stopListRefusalCode;
    }

    public Date getCheckDate() {
        return this.checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public boolean getWasBoarded() {
        return this.wasBoarded;
    }

    public void setWasBoarded(boolean wasBoarded) {
        this.wasBoarded = wasBoarded;
    }

    public Long getTicketDataId() {
        return this.ticketDataId;
    }

    public void setTicketDataId(Long ticketDataId) {
        this.ticketDataId = ticketDataId;
    }

    @Generated(hash = 1172539595)
    private transient Long ticketId__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 2102290187)
    public TicketIdEntity getTicketId() {
        Long __key = this.ticketIdId;
        if (ticketId__resolvedKey == null || !ticketId__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TicketIdEntityDao targetDao = daoSession.getTicketIdEntityDao();
            TicketIdEntity ticketIdNew = targetDao.load(__key);
            synchronized (this) {
                ticketId = ticketIdNew;
                ticketId__resolvedKey = __key;
            }
        }
        return ticketId;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1700979074)
    public void setTicketId(TicketIdEntity ticketId) {
        synchronized (this) {
            this.ticketId = ticketId;
            ticketIdId = ticketId == null ? null : ticketId.getId();
            ticketId__resolvedKey = ticketIdId;
        }
    }

    @Generated(hash = 797777293)
    private transient Long ticketData__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 207996401)
    public TicketDataEntity getTicketData() {
        Long __key = this.ticketDataId;
        if (ticketData__resolvedKey == null
                || !ticketData__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TicketDataEntityDao targetDao = daoSession.getTicketDataEntityDao();
            TicketDataEntity ticketDataNew = targetDao.load(__key);
            synchronized (this) {
                ticketData = ticketDataNew;
                ticketData__resolvedKey = __key;
            }
        }
        return ticketData;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 238250799)
    public void setTicketData(TicketDataEntity ticketData) {
        synchronized (this) {
            this.ticketData = ticketData;
            ticketDataId = ticketData == null ? null : ticketData.getId();
            ticketData__resolvedKey = ticketDataId;
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
    @Generated(hash = 2023679452)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTicketBoardingEntityDao() : null;
    }

    public boolean getBoardingByList() {
        return this.boardingByList;
    }


}
