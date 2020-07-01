package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import ru.ppr.database.references.ReferenceInfo;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = StationInfoEntity.TABLE_NAME)
public class StationInfoEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "StationInfo";
    private static final String TrainInfoIdField = "TrainInfoId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(TrainInfoEntity.TABLE_NAME, TrainInfoIdField, ReferenceInfo.ReferencesType.CASCADE);
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
    @Property(nameInDb = "Code")
    private Long code;
    @Property(nameInDb = "Number")
    private int number;
    @Property(nameInDb = "StationStateCode")
    private Integer stationStateCode;
    @Property(nameInDb = "ArrivalDate")
    private Date arrivalDate;
    @Property(nameInDb = "DepartureDate")
    private Date departureDate;
    @Generated(hash = 505156661)
    public StationInfoEntity(Long id, Long trainInfoId, Long code, int number,
            Integer stationStateCode, Date arrivalDate, Date departureDate) {
        this.id = id;
        this.trainInfoId = trainInfoId;
        this.code = code;
        this.number = number;
        this.stationStateCode = stationStateCode;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }
    @Generated(hash = 814053835)
    public StationInfoEntity() {
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
    public Long getCode() {
        return this.code;
    }
    public void setCode(Long code) {
        this.code = code;
    }
    public int getNumber() {
        return this.number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public Integer getStationStateCode() {
        return this.stationStateCode;
    }
    public void setStationStateCode(Integer stationStateCode) {
        this.stationStateCode = stationStateCode;
    }
    public Date getArrivalDate() {
        return this.arrivalDate;
    }
    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }
    public Date getDepartureDate() {
        return this.departureDate;
    }
    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

}
