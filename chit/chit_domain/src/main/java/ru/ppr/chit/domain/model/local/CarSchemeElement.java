package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * Элемент схемы вагона
 *
 * @author Dmitry Nevolin
 */
public class CarSchemeElement implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
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
     * <br/><br/>Note: Устанавливается только для вида элемента CarSchemeElementKind.PassengerPlace
     */
    private String placeNumber;
    /**
     * Ориентация пассажирского/служебного места относительно направления движения поезда
     * <br/><br/>Note: Устанавливается только для вида элементов: CarSchemeElementKind.PassengerPlace и CarSchemeElementKind.StaffPlace
     */
    private PlaceDirection placeDirection;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

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

    public PlaceDirection getPlaceDirection() {
        return placeDirection;
    }

    public void setPlaceDirection(PlaceDirection placeDirection) {
        this.placeDirection = placeDirection;
    }

    /**
     * Вид элемента схемы
     */
    public enum Kind {

        /**
         * Неизвестный вид.
         */
        UNKNOWN(0),

        //region Пассажирское/служебное место

        /**
         * Пассажирское место.
         */
        PASSENGER_PLACE(1),

        /**
         * Служебное место.
         */
        STAFF_PLACE(2),

        //endregion Пассажирское/служебное место

        //region Архитектурные элементы

        /**
         * Корпус вагона.
         */
        CAR_BODY(10000),

        /**
         * Окно вагона.
         */
        CAR_WINDOW(10001),

        /**
         * Перегородка внутри вагона.
         */
        CAR_INTERIOR_PARTITION(10002),

        /**
         * Кабина машиниста.
         */
        DRIVER_CAB(10002),

        /**
         * Тамбур.
         */
        VESTIBULE(10003),

        /**
         * Служебное помещение.
         */
        STAFF_ROOM(10004),

        /**
         * Переход в другой вагон.
         */
        TRANSFER_BETWEEN_CARS(10005),

        /**
         * Стол.
         */
        TABLE(10006),

        //endregion Архитектурные элементы

        //region Функциональные элементы

        /**
         * Туалет.
         */
        WC(20001),

        /**
         * Буфет.
         */
        BUFFET(20002),

        /**
         * Багажный стелаж.
         */
        LUGGAGE(20003),

        /**
         * Гардероб.
         */
        WARDROBE(20004),

        /**
         * Телевизор.
         */
        TV(20005),

        /**
         * Розетка ~220В.
         */
        SOCKET220_V(20006),

        /**
         * Оборудование для инвалидов.
         */
        EQUIPMENT_FOR_DISABLED_PEOPLE(20007),

        //endregion Функциональные элементы

        //region Иконки-надписи

        /**
         * Иконка туалета.
         */
        ICON_WC(30000),

        /**
         * Иконка буфета.
         */
        ICON_BUFFET(30001),

        /**
         * Иконка багажного стелажа.
         */
        ICON_LUGGAGE(30002),

        /**
         * Иконка гардероба.
         */
        ICON_WARDROBE(30003),

        /**
         * Служебное помещение.
         */
        ICON_STAFF_ROOM(30004),

        //endregion Иконки-надписи

        //region Навигационные элементы

        /**
         * Элемент навигации: стрелка вверх.
         */
        NAVIGATION_ARROW_UP(40000),

        /**
         * Элемент навигации: стрелка вниз.
         */
        NAVIGATION_ARROW_DOWN(40001),

        /**
         * Элемент навигации: стрелка налево.
         */
        NAVIGATION_ARROW_LEFT(40002),

        /**
         * Элемент навигации: стрелка направо.
         */
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
