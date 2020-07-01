package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = TicketIdEntity.TABLE_NAME)
public class TicketIdEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "TicketId";

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
    @Property(nameInDb = "TicketNumber")
    private long ticketNumber;
    @Property(nameInDb = "SaleDate")
    private Date saleDate;
    @Property(nameInDb = "DeviceId")
    private String deviceId;
    @Generated(hash = 293819247)
    public TicketIdEntity(Long id, long ticketNumber, Date saleDate,
            String deviceId) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.saleDate = saleDate;
        this.deviceId = deviceId;
    }
    @Generated(hash = 848869865)
    public TicketIdEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public long getTicketNumber() {
        return this.ticketNumber;
    }
    public void setTicketNumber(long ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
    public Date getSaleDate() {
        return this.saleDate;
    }
    public void setSaleDate(Date saleDate) {
        this.saleDate = saleDate;
    }
    public String getDeviceId() {
        return this.deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
