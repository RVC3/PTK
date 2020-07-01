package ru.ppr.chit.localdb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.localdb.entity.base.BaseLocalMeta;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Dmitry Nevolin
 */
@Entity(nameInDb = CarSchemeElementEntity.TABLE_NAME)
public class CarSchemeElementEntity implements LocalEntityWithId<Long> {

    public static final String TABLE_NAME = "CarSchemeElement";
    public static final String CarSchemeIdField = "CarSchemeId";

    public static BaseLocalMeta createMeta(){
        return new Meta();
    }

    private static class Meta extends BaseLocalMeta {

        public Meta(){
            registerReference(CarSchemeEntity.TABLE_NAME, CarSchemeIdField);
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = CarSchemeIdField)
    private Long carSchemeId;
    @Property(nameInDb = "CarSchemeElementKind")
    private Integer carSchemeElementKind;
    @Property(nameInDb = "X")
    private Integer x;
    @Property(nameInDb = "Y")
    private Integer y;
    @Property(nameInDb = "Height")
    private Integer height;
    @Property(nameInDb = "Width")
    private Integer width;
    @Property(nameInDb = "PlaceNumber")
    private String placeNumber;
    @Property(nameInDb = "PlaceDirection")
    private Integer placeDirection;
    @Generated(hash = 610668255)
    public CarSchemeElementEntity(Long id, Long carSchemeId,
            Integer carSchemeElementKind, Integer x, Integer y, Integer height,
            Integer width, String placeNumber, Integer placeDirection) {
        this.id = id;
        this.carSchemeId = carSchemeId;
        this.carSchemeElementKind = carSchemeElementKind;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.placeNumber = placeNumber;
        this.placeDirection = placeDirection;
    }
    @Generated(hash = 970965976)
    public CarSchemeElementEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getCarSchemeId() {
        return this.carSchemeId;
    }
    public void setCarSchemeId(Long carSchemeId) {
        this.carSchemeId = carSchemeId;
    }
    public Integer getCarSchemeElementKind() {
        return this.carSchemeElementKind;
    }
    public void setCarSchemeElementKind(Integer carSchemeElementKind) {
        this.carSchemeElementKind = carSchemeElementKind;
    }
    public Integer getX() {
        return this.x;
    }
    public void setX(Integer x) {
        this.x = x;
    }
    public Integer getY() {
        return this.y;
    }
    public void setY(Integer y) {
        this.y = y;
    }
    public Integer getHeight() {
        return this.height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }
    public Integer getWidth() {
        return this.width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public String getPlaceNumber() {
        return this.placeNumber;
    }
    public void setPlaceNumber(String placeNumber) {
        this.placeNumber = placeNumber;
    }
    public Integer getPlaceDirection() {
        return this.placeDirection;
    }
    public void setPlaceDirection(Integer placeDirection) {
        this.placeDirection = placeDirection;
    }


}
