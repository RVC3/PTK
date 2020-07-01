package ru.ppr.chit.securitydb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = "PtkDataContractsVersion")
public class PtkDataContractsVersionEntity {

    @Id
    @Property(nameInDb = "Version")
    private Integer version;

    @Generated(hash = 144153946)
    public PtkDataContractsVersionEntity(Integer version) {
        this.version = version;
    }

    @Generated(hash = 49496695)
    public PtkDataContractsVersionEntity() {
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
