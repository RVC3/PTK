package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import org.greenrobot.greendao.annotation.Generated;

import java.util.Date;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = ControlStationEntity.TABLE_NAME)
public class ControlStationEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "ControlStation";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "Code")
    private Long code;
    @Property(nameInDb = "DepartureDate")
    private Date departureDate;
    @Generated(hash = 1090204523)
    public ControlStationEntity(Long id, Long code, Date departureDate) {
        this.id = id;
        this.code = code;
        this.departureDate = departureDate;
    }
    @Generated(hash = 1866355220)
    public ControlStationEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getCode() {
        return this.code;
    }
    public void setCode(Long code) {
        this.code = code;
    }
    public Date getDepartureDate() {
        return this.departureDate;
    }
    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

}
