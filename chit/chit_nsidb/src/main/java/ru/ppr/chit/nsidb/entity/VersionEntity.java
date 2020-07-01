package ru.ppr.chit.nsidb.entity;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

import ru.ppr.chit.nsidb.entity.converter.DateTimeConverter;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = "Versions")
public class VersionEntity {

    @Id
    @Property(nameInDb = "VersionId")
    private int versionId;
    @Property(nameInDb = "Description")
    private String description;
    @Convert(converter = DateTimeConverter.class, columnType = String.class)
    @Property(nameInDb = "CreatedDateTime")
    private Date createdDateTime;
    @Convert(converter = DateTimeConverter.class, columnType = String.class)
    @Property(nameInDb = "StartingDateTime")
    private Date startingDateTime;
    @Property(nameInDb = "Status")
    private Integer status;
    @Property(nameInDb = "IsCriticalChange")
    private boolean isCriticalChange;
    @Generated(hash = 2080436545)
    public VersionEntity(int versionId, String description, Date createdDateTime,
            Date startingDateTime, Integer status, boolean isCriticalChange) {
        this.versionId = versionId;
        this.description = description;
        this.createdDateTime = createdDateTime;
        this.startingDateTime = startingDateTime;
        this.status = status;
        this.isCriticalChange = isCriticalChange;
    }
    @Generated(hash = 1554881461)
    public VersionEntity() {
    }
    public int getVersionId() {
        return this.versionId;
    }
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Date getCreatedDateTime() {
        return this.createdDateTime;
    }
    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
    public Date getStartingDateTime() {
        return this.startingDateTime;
    }
    public void setStartingDateTime(Date startingDateTime) {
        this.startingDateTime = startingDateTime;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public boolean getIsCriticalChange() {
        return this.isCriticalChange;
    }
    public void setIsCriticalChange(boolean isCriticalChange) {
        this.isCriticalChange = isCriticalChange;
    }


}
