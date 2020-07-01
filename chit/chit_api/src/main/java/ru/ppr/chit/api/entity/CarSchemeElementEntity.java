package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * @author Dmitry Nevolin
 */
public class CarSchemeElementEntity {

    /**
     * Вид элемента
     */
    private Kind carSchemeElementKind;
    /**
     * Координата вдоль схемы вагона
     */
    private Integer x;
    /**
     * Координата поперек схемы вагона
     */
    private Integer y;
    /**
     * Высота элемента, для типов элементов которым можно менять размер
     */
    private Integer height;
    /**
     * Ширина элемента, для типов элементов которым можно менять размер
     */
    private Integer width;
    /**
     * Номер места или <c>null</c>, если элемент НЕ является продаваемым местом
     * <br/><br/>Note: Устанавливается только для вида элемента Enums.CarSchemeElementKind.PassengerPlace
     */
    private String placeNumber;
    /**
     * Ориентация пассажирского/служебного места относительно направления движения поезда
     * <br/><br/>Note: Устанавливается только для вида элементов: Enums.CarSchemeElementKind.PassengerPlace и Enums.CarSchemeElementKind.StaffPlace
     */
    private PlaceDirectionEntity placeDirection;

    public Kind getCarSchemeElementKind() {
        return carSchemeElementKind;
    }

    public void setCarSchemeElementKind(Kind carSchemeElementKind) {
        this.carSchemeElementKind = carSchemeElementKind;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getPlaceNumber() {
        return placeNumber;
    }

    public void setPlaceNumber(String placeNumber) {
        this.placeNumber = placeNumber;
    }

    public PlaceDirectionEntity getPlaceDirection() {
        return placeDirection;
    }

    public void setPlaceDirection(PlaceDirectionEntity placeDirection) {
        this.placeDirection = placeDirection;
    }

    /**
     * Вид элемента схемы
     */
    public enum Kind {
        
        /**
         * Неизвестный вид.
         */
        @SerializedName("0")
        UNKNOWN(0),

        //region Пассажирское/служебное место

        /**
         * Пассажирское место.
         */
        @SerializedName("1")
        PASSENGER_PLACE(1),

        /**
         * Служебное место.
         */
        @SerializedName("2")
        STAFF_PLACE(2),

        //endregion Пассажирское/служебное место

        //region Архитектурные элементы

        /**
         * Корпус вагона.
         */
        @SerializedName("10000")
        CAR_BODY(10000),

        /**
         * Окно вагона.
         */
        @SerializedName("10001")
        CAR_WINDOW(10001),

        /**
         * Перегородка внутри вагона.
         */
        @SerializedName("10002")
        CAR_INTERIOR_PARTITION(10002),

        /**
         * Кабина машиниста.
         */
        @SerializedName("10002")
        DRIVER_CAB(10002),

        /**
         * Тамбур.
         */
        @SerializedName("10003")
        VESTIBULE(10003),

        /**
         * Служебное помещение.
         */
        @SerializedName("10004")
        STAFF_ROOM(10004),

        /**
         * Переход в другой вагон.
         */
        @SerializedName("10005")
        TRANSFER_BETWEEN_CARS(10005),

        /**
         * Стол.
         */
        @SerializedName("10006")
        TABLE(10006),

        //endregion Архитектурные элементы

        //region Функциональные элементы

        /**
         * Туалет.
         */
        @SerializedName("20001")
        WC(20001),

        /**
         * Буфет.
         */
        @SerializedName("20002")
        BUFFET(20002),

        /**
         * Багажный стелаж.
         */
        @SerializedName("20003")
        LUGGAGE(20003),

        /**
         * Гардероб.
         */
        @SerializedName("20004")
        WARDROBE(20004),

        /**
         * Телевизор.
         */
        @SerializedName("20005")
        TV(20005),

        /**
         * Розетка ~220В.
         */
        @SerializedName("20006")
        SOCKET220_V(20006),

        /**
         * Оборудование для инвалидов.
         */
        @SerializedName("20007")
        EQUIPMENT_FOR_DISABLED_PEOPLE(20007),

        //endregion Функциональные элементы

        //region Иконки-надписи

        /**
         * Иконка туалета.
         */
        @SerializedName("30000")
        ICON_WC(30000),

        /**
         * Иконка буфета.
         */
        @SerializedName("30001")
        ICON_BUFFET(30001),

        /**
         * Иконка багажного стелажа.
         */
        @SerializedName("30002")
        ICON_LUGGAGE(30002),

        /**
         * Иконка гардероба.
         */
        @SerializedName("30003")
        ICON_WARDROBE(30003),

        /**
         * Служебное помещение.
         */
        @SerializedName("30004")
        ICON_STAFF_ROOM(30004),

        //endregion Иконки-надписи

        //region Навигационные элементы

        /**
         * Элемент навигации: стрелка вверх.
         */
        @SerializedName("40000")
        NAVIGATION_ARROW_UP(40000),

        /**
         * Элемент навигации: стрелка вниз.
         */
        @SerializedName("40001")
        NAVIGATION_ARROW_DOWN(40001),

        /**
         * Элемент навигации: стрелка налево.
         */
        @SerializedName("40002")
        NAVIGATION_ARROW_LEFT(40002),

        /**
         * Элемент навигации: стрелка направо.
         */
        @SerializedName("40003")
        NAVIGATION_ARROW_RIGHT(40003);

        //endregion Навигационные элементы

        private final int code;

        Kind(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Kind valueOf(int code) {
            for(Kind kind : Kind.values()) {
                if (kind.getCode() == code) {
                    return kind;
                }
            }
            return null;
        }

    }

}
