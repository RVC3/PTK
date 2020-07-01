package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * @author Dmitry Nevolin
 */
public class ErrorEntity {

    private Code code;
    private String description;

    public ErrorEntity(Code code, String description){
        this.code = code;
        this.description = description;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 1xx - ошибки авторизации
     * 2xx - ошибки подписи
     * 3xx - ошибки посадки
     * 4xx - ошибки обновления
     */
    public enum Code {

        /**
         * Неизвестная ошибка
         */
        @SerializedName("0")
        UNKNOWN(0),
        /**
         * Не указан идентификатор билета
         */
        @SerializedName("1")
        TICKET_ID_NOT_SPECIFIED(1),
        /**
         * Не указан номер поезда
         */
        @SerializedName("2")
        TRAIN_NUMBER_NOT_SPECIFIED(2),
        /**
         * Не указан идентификатор терминала посадки
         */
        @SerializedName("3")
        TERMINAL_DEVICE_ID_NOT_SPECIFIED(3),
        /**
         * Не указано ФИО контролёра
         */
        @SerializedName("4")
        OPERATOR_NAME_NOT_SPECIFIED(4),
        /**
         * Не указан код станции контроля
         */
        @SerializedName("5")
        CONTROL_STATION_CODE_NOT_SPECIFIED(5),
        /**
         * Не указана дата/время контроля
         */
        @SerializedName("6")
        CHECK_DATE_TIME_NOT_SPECIFIED(6),
        /**
         * Не указаны данные билета
         */
        @SerializedName("7")
        TICKET_DATA_NOT_SPECIFIED(7),
        /**
         * Не указан код типа билета
         */
        @SerializedName("8")
        TICKET_TYPE_CODE_NOT_SPECIFIED(8),
        /**
         * Не указаны данные пассажира
         */
        @SerializedName("9")
        PASSENGER_NOT_SPECIFIED(9),
        /**
         * Не указана дата/время отправления
         */
        @SerializedName("10")
        DEPARTURE_DATE_TIME_NOT_SPECIFIED(10),
        /**
         * Не указан код станции отправления
         */
        @SerializedName("11")
        DEPARTURE_STATION_CODE_NOT_SPECIFIED(11),
        /**
         * Не указан код станции назначения
         */
        @SerializedName("12")
        DESTINATION_STATION_CODE_NOT_SPECIFIED(12),
        /**
         * Не указана версия НСИ
         */
        @SerializedName("13")
        RDS_VERSION_ID_NOT_SPECIFIED(13),
        /**
         * Не указан внешний номер БСК
         */
        @SerializedName("14")
        SMART_CARD_OUTER_NUMBER_NOT_SPECIFIED(14),
        /**
         * Не указан номер кристалла БСК
         */
        @SerializedName("15")
        SMART_CARD_CRYSTAL_SERIAL_NUMBER_NOT_SPECIFIED(15),
        /**
         * Не указан номер вагона
         */
        @SerializedName("16")
        CAR_NUMBER_NOT_SPECIFIED(16),
        /**
         * Не указан номер места
         */
        @SerializedName("17")
        PLACE_NUMBER_NOT_SPECIFIED(17),
        /**
         * Не указана фамилия пассажира
         */
        @SerializedName("18")
        LAST_NAME_NOT_SPECIFIED(18),
        /**
         * Не указано имя пассажира
         */
        @SerializedName("19")
        FIRST_NAME_NOT_SPECIFIED(19),
        /**
         * Имя пассажира должно содержать один символ
         */
        @SerializedName("20")
        FIRST_NAME_LENGTH_IS_NOT_VALID(20),
        /**
         * Не указан код типа документа пассажира
         */
        @SerializedName("21")
        DOCUMENT_TYPE_CODE_NOT_SPECIFIED(21),
        /**
         * Не указан номер документа пассажира
         */
        @SerializedName("22")
        DOCUMENT_NUMBER_NOT_SPECIFIED(22),
        /**
         * Не указан список файлов запроса
         */
        @SerializedName("23")
        FILE_REQUEST_LIST_NOT_SPECIFIED(23),
        /**
         * Не указано имя файла
         */
        @SerializedName("24")
        FILE_NOT_SPECIFIED(24),
        /**
         * Не указаны данные файла
         */
        @SerializedName("25")
        FILE_DATA_NOT_SPECIFIED(25),
        /**
         * Недопустимое количество символов в номере вагона
         */
        @SerializedName("26")
        CAR_NUMBER_LENGTH_IS_INCORRECT(26),
        /**
         * Недопустимое количество символов в номере места
         */
        @SerializedName("27")
        PLACE_NUMBER_LENGTH_IS_INCORRECT(27),
        /**
         * Отказано в доступе
         */
        @SerializedName("100")
        ACCESS_DENIED(100),
        /**
         * Устройство не зарегистрировано
         */
        @SerializedName("101")
        DEVICE_NOT_REGISTRED(101),
        /**
         * Невозможно удалить устройство
         */
        @SerializedName("102")
        UNABLE_TO_UNREGISTER_DEVICE(102),
        /**
         * Станция не найдена в маршруте текущего поезда
         */
        @SerializedName("103")
        STATION_NOT_FOUND_IN_CURRENT_ROUTE(103),
        /**
         * Невалидная подпись
         */
        @SerializedName("200")
        INVALID_SIGNATURE(200),
        /**
         * Неизвестный билет
         */
        @SerializedName("302")
        UNKNOWN_TICKET(302),
        /**
         * Неизвестный идентификатор пакета
         */
        @SerializedName("400")
        UNKNOWN_PACKET_ID(400),
        /**
         * Не задана текущая нить поезда
         */
        @SerializedName("500")
        TRAIN_THREAD_CODE_NOT_SET(500);

        private final int code;

        Code(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }

    @Override
    public String toString() {
        return String.format("[%s] %s",  getCode(), getDescription());
    }


}
