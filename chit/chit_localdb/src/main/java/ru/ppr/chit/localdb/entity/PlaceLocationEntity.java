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
@Entity(nameInDb = PlaceLocationEntity.TABLE_NAME)
public class PlaceLocationEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "PlaceLocation";

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
    @Property(nameInDb = "CarNumber")
    private String carNumber;
    @Property(nameInDb = "PlaceNumber")
    private String placeNumber;
    @Generated(hash = 1742101400)
    public PlaceLocationEntity(Long id, String carNumber, String placeNumber) {
        this.id = id;
        this.carNumber = carNumber;
        this.placeNumber = placeNumber;
    }
    @Generated(hash = 1511512341)
    public PlaceLocationEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCarNumber() {
        return this.carNumber;
    }
    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }
    public String getPlaceNumber() {
        return this.placeNumber;
    }
    public void setPlaceNumber(String placeNumber) {
        this.placeNumber = placeNumber;
    }

}
