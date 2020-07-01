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
@Entity(nameInDb = UserEntity.TABLE_NAME)
public class UserEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "User";

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
    @Property(nameInDb = "Name")
    private String name;
    @Generated(hash = 2146553439)
    public UserEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    @Generated(hash = 1433178141)
    public UserEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }


}
