package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = SmartCardEntity.TABLE_NAME)
public class SmartCardEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "SmartCard";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta implements GCNoLinkRemovable {
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }

        @Override
        public boolean gcHandleNoLinkRemoveData(Database database) {
            // стандартный алгоритм удаления записей, на которые нет ссылок
            return false;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "OuterNumber")
    private String outerNumber;
    @Property(nameInDb = "CrystalSerialNumber")
    private String crystalSerialNumber;
    @Property(nameInDb = "Type")
    private Integer type;
    @Property(nameInDb = "UsageCount")
    private Integer usageCount;
    @Generated(hash = 519115319)
    public SmartCardEntity(Long id, String outerNumber, String crystalSerialNumber,
            Integer type, Integer usageCount) {
        this.id = id;
        this.outerNumber = outerNumber;
        this.crystalSerialNumber = crystalSerialNumber;
        this.type = type;
        this.usageCount = usageCount;
    }
    @Generated(hash = 839373328)
    public SmartCardEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOuterNumber() {
        return this.outerNumber;
    }
    public void setOuterNumber(String outerNumber) {
        this.outerNumber = outerNumber;
    }
    public String getCrystalSerialNumber() {
        return this.crystalSerialNumber;
    }
    public void setCrystalSerialNumber(String crystalSerialNumber) {
        this.crystalSerialNumber = crystalSerialNumber;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getUsageCount() {
        return this.usageCount;
    }
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

}
