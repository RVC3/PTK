package ru.ppr.chit.nsidb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCVD;

/**
 * Категория ПД.
 *
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = "TicketCategories")
public class TicketCategoryEntity implements NsiEntityWithCVD<Long> {

    @Property(nameInDb = "Name")
    private String name;
    @Property(nameInDb = "ExpressTicketCategoryCode")
    private String expressTicketCategoryCode;
    @Property(nameInDb = "Abbreviation")
    private String abbreviation;
    @Property(nameInDb = "Presale")
    private int presale;
    @Property(nameInDb = "DelayPassback")
    private int delayPassback;
    @Property(nameInDb = "ChangedDateTime")
    private long changedDateTime;
    @Property(nameInDb = "IsCompositeType")
    private boolean compositeType;
    @Property(nameInDb = "Code")
    private Long code;
    @Property(nameInDb = "VersionId")
    private int versionId;
    @Property(nameInDb = "DeleteInVersionId")
    private Integer deleteInVersionId;

    @Generated(hash = 970909450)
    public TicketCategoryEntity(String name, String expressTicketCategoryCode,
                                String abbreviation, int presale, int delayPassback,
                                long changedDateTime, boolean compositeType, Long code, int versionId,
                                Integer deleteInVersionId) {
        this.name = name;
        this.expressTicketCategoryCode = expressTicketCategoryCode;
        this.abbreviation = abbreviation;
        this.presale = presale;
        this.delayPassback = delayPassback;
        this.changedDateTime = changedDateTime;
        this.compositeType = compositeType;
        this.code = code;
        this.versionId = versionId;
        this.deleteInVersionId = deleteInVersionId;
    }

    @Generated(hash = 1226863685)
    public TicketCategoryEntity() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpressTicketCategoryCode() {
        return this.expressTicketCategoryCode;
    }

    public void setExpressTicketCategoryCode(String expressTicketCategoryCode) {
        this.expressTicketCategoryCode = expressTicketCategoryCode;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public int getPresale() {
        return this.presale;
    }

    public void setPresale(int presale) {
        this.presale = presale;
    }

    public int getDelayPassback() {
        return this.delayPassback;
    }

    public void setDelayPassback(int delayPassback) {
        this.delayPassback = delayPassback;
    }

    public long getChangedDateTime() {
        return this.changedDateTime;
    }

    public void setChangedDateTime(long changedDateTime) {
        this.changedDateTime = changedDateTime;
    }

    public boolean getCompositeType() {
        return this.compositeType;
    }

    public void setCompositeType(boolean compositeType) {
        this.compositeType = compositeType;
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
