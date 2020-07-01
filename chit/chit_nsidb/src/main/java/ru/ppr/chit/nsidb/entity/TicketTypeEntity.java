package ru.ppr.chit.nsidb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCVD;

/**
 * Тип ПД.
 *
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = "TicketTypes")
public class TicketTypeEntity implements NsiEntityWithCVD<Long> {

    /**
     * Категория ПД
     */
    @Property(nameInDb = "TicketCategoryCode")
    private long ticketCategoryCode;
    /**
     * Признак «ПД с местом».
     */
    @Property(nameInDb = "WithPlace")
    private boolean withPlace;
    /**
     * Идентификатор периода действия ПД.
     */
    @Property(nameInDb = "ValidityPeriodCode")
    private Long validityPeriodCode;
    /**
     * Продолжительность периода действия.
     */
    @Property(nameInDb = "DurationOfValidity")
    private Integer durationOfValidity;
    /**
     * Количество поездок для абонемента на кол-во поездок.
     */
    @Property(nameInDb = "TripsNumber")
    private int tripsNumber;
    /**
     * Короткое название типа ПД
     */
    @Property(nameInDb = "ShortName")
    private String shortName;
    @Property(nameInDb = "Code")
    private Long code;
    @Property(nameInDb = "VersionId")
    private int versionId;
    @Property(nameInDb = "DeleteInVersionId")
    private Integer deleteInVersionId;

    @Generated(hash = 522347985)
    public TicketTypeEntity(long ticketCategoryCode, boolean withPlace, Long validityPeriodCode,
            Integer durationOfValidity, int tripsNumber, String shortName, Long code, int versionId,
            Integer deleteInVersionId) {
        this.ticketCategoryCode = ticketCategoryCode;
        this.withPlace = withPlace;
        this.validityPeriodCode = validityPeriodCode;
        this.durationOfValidity = durationOfValidity;
        this.tripsNumber = tripsNumber;
        this.shortName = shortName;
        this.code = code;
        this.versionId = versionId;
        this.deleteInVersionId = deleteInVersionId;
    }

    @Generated(hash = 12314679)
    public TicketTypeEntity() {
    }

    public long getTicketCategoryCode() {
        return this.ticketCategoryCode;
    }

    public void setTicketCategoryCode(long ticketCategoryCode) {
        this.ticketCategoryCode = ticketCategoryCode;
    }

    public boolean getWithPlace() {
        return this.withPlace;
    }

    public void setWithPlace(boolean withPlace) {
        this.withPlace = withPlace;
    }

    public Long getValidityPeriodCode() {
        return this.validityPeriodCode;
    }

    public void setValidityPeriodCode(Long validityPeriodCode) {
        this.validityPeriodCode = validityPeriodCode;
    }

    public Integer getDurationOfValidity() {
        return this.durationOfValidity;
    }

    public void setDurationOfValidity(Integer durationOfValidity) {
        this.durationOfValidity = durationOfValidity;
    }

    public int getTripsNumber() {
        return this.tripsNumber;
    }

    public void setTripsNumber(int tripsNumber) {
        this.tripsNumber = tripsNumber;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public Long getCode() {
        return this.code;
    }

    @Override
    public void setCode(Long code) {
        this.code = code;
    }

    @Override
    public int getVersionId() {
        return this.versionId;
    }

    @Override
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    @Override
    public Integer getDeleteInVersionId() {
        return this.deleteInVersionId;
    }

    @Override
    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }
}
