package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;

/**
 * Параметр приложения (Key-Value).
 *
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = AppPropertyEntity.TABLE_NAME)
public class AppPropertyEntity {

    public static final String TABLE_NAME = "AppProperty";
    private static final String KeyField = "Key";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {
        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
        @Override
        public String getPkField() {
            return KeyField;
        }
    }

    @Id
    @Property(nameInDb = KeyField)
    private String key;
    @Property(nameInDb = "Value")
    private String value;
    @Generated(hash = 221027819)
    public AppPropertyEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }
    @Generated(hash = 1608718192)
    public AppPropertyEntity() {
    }
    public String getKey() {
        return this.key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String   getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
