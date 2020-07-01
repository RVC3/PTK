package ru.ppr.nsi.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import ru.ppr.nsi.NsiDaoSession;

/**
 * Класс хранит информацию о типе билета
 * Т.к. этот клас используется в качетсве ключа для HasMap,
 * то здесь переопределяем методы equals и hasMap
 *
 * @author A.Ushakov
 */
public class TicketType {

    private int code;
    private int versionId;

    /**
     * Код категории поезда
     */
    private int ticketCategoryCode = -1;
    /**
     * Категория поезда
     */
    private TicketCategory ticketCategory;
    /**
     * Тип периода срока действия ПД.
     */
    private ValidityPeriod validityPeriod;
    /**
     * Срок действия ПД
     */
    private Integer durationOfValidity;
    /**
     * Количество возможных поездок по ПД
     */
    private Integer tripsNumber;
    /**
     * Краткое наименование
     */
    private String shortName;
    /**
     * НДС в процентах
     */
    private BigDecimal tax = null;
    /**
     * Экспресс-код
     */
    private String expressTicketTypeCode;
    /**
     * Признак совместной продажи
     */
    private boolean jointSale;
    /**
     * Привязка ФИО к БСК
     */
    private boolean requireBindedFio;

    /**
     * Требовать исходный ПД
     */
    private boolean requireSourceTicket;


    public TicketType() {

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    //region TicketCategory getters and setters
    public int getTicketCategoryCode() {
        return ticketCategoryCode;
    }

    public void setTicketCategoryCode(int ticketCategoryCode) {
        this.ticketCategoryCode = ticketCategoryCode;
        if (ticketCategory != null && ticketCategory.getCode() != ticketCategoryCode) {
            ticketCategory = null;
        }
    }

    public TicketCategory getTicketCategory(@NonNull NsiDaoSession nsiDaoSession) {
        TicketCategory local = ticketCategory;
        if (local == null && ticketCategoryCode >= 0) {
            synchronized (this) {
                if (ticketCategory == null) {
                    ticketCategory = nsiDaoSession.getTicketCategoryDao().load(ticketCategoryCode, versionId);
                }
            }
            return ticketCategory;
        }
        return local;
    }

    public void setTicketCategory(TicketCategory ticketCategory) {
        this.ticketCategory = ticketCategory;
        ticketCategoryCode = ticketCategory == null ? -1 : ticketCategory.getCode();
    }
    //endregion

    public @Nullable
    ValidityPeriod getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(@Nullable ValidityPeriod validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public void setDurationOfValidity(Integer durationOfValidity) {
        this.durationOfValidity = durationOfValidity;
    }

    public Integer getTripsNumber() {
        return tripsNumber;
    }

    public void setTripsNumber(Integer tripsNumber) {
        this.tripsNumber = tripsNumber;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public String getExpressTicketTypeCode() {
        return expressTicketTypeCode;
    }

    public void setExpressTicketTypeCode(String expressTicketTypeCode) {
        this.expressTicketTypeCode = expressTicketTypeCode;
    }

    public Integer getDurationOfValidity() {
        return durationOfValidity;
    }

    public boolean isJointSale() {
        return jointSale;
    }

    public void setJointSale(boolean jointSale) {
        this.jointSale = jointSale;
    }

    public boolean isRequireBindedFio() {
        return requireBindedFio;
    }

    public void setRequireBindedFio(boolean requireBindedFio) {
        this.requireBindedFio = requireBindedFio;
    }

    public boolean isRequireSourceTicket() {
        return requireSourceTicket;
    }

    public void setRequireSourceTicket(boolean requireSourceTicket) {
        this.requireSourceTicket = requireSourceTicket;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TicketType that = (TicketType) o;

        if (code != that.code) return false;
        if (versionId != that.versionId) return false;
        if (ticketCategoryCode != that.ticketCategoryCode) return false;
        if (jointSale != that.jointSale) return false;
        if (requireBindedFio != that.requireBindedFio) return false;
        if (requireSourceTicket != that.requireSourceTicket) return false;
        if (ticketCategory != null ? !ticketCategory.equals(that.ticketCategory) : that.ticketCategory != null)
            return false;
        if (validityPeriod != that.validityPeriod) return false;
        if (durationOfValidity != null ? !durationOfValidity.equals(that.durationOfValidity) : that.durationOfValidity != null)
            return false;
        if (tripsNumber != null ? !tripsNumber.equals(that.tripsNumber) : that.tripsNumber != null)
            return false;
        if (shortName != null ? !shortName.equals(that.shortName) : that.shortName != null)
            return false;
        if (tax != null ? !tax.equals(that.tax) : that.tax != null) return false;
        return expressTicketTypeCode != null ? expressTicketTypeCode.equals(that.expressTicketTypeCode) : that.expressTicketTypeCode == null;
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + versionId;
        result = 31 * result + ticketCategoryCode;
        result = 31 * result + (ticketCategory != null ? ticketCategory.hashCode() : 0);
        result = 31 * result + (validityPeriod != null ? validityPeriod.hashCode() : 0);
        result = 31 * result + (durationOfValidity != null ? durationOfValidity.hashCode() : 0);
        result = 31 * result + (tripsNumber != null ? tripsNumber.hashCode() : 0);
        result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
        result = 31 * result + (tax != null ? tax.hashCode() : 0);
        result = 31 * result + (expressTicketTypeCode != null ? expressTicketTypeCode.hashCode() : 0);
        result = 31 * result + (jointSale ? 1 : 0);
        result = 31 * result + (requireBindedFio ? 1 : 0);
        result = 31 * result + (requireSourceTicket ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return textToLowerWithTitle(shortName);
    }

    /**
     * Форматирует текст любого регистра в нижний регистр с первой заглавной буквой
     *
     * @param string
     * @return
     */
    private String textToLowerWithTitle(@Nullable String string) {

        if (string == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        int lenght = string.length();

        builder.append(string.substring(0, 1).toUpperCase()).append(string.substring(1, lenght).toLowerCase());
        return builder.toString();

    }

    public static class Code {
        //http://agile.srvdev.ru/browse/CPPKPP-43912
        // В будущем надо выпилить все зависимости от этого списка, так как заказчик может динамически создавать сущности TicketType через ЦОД
        /**
         * Разовый полный
         */
        public static final long SINGLE_FULL = 1;
        /**
         * Разовый детский
         */
        public static final long SINGLE_CHILD = 2;
        /**
         * Ручная кладь
         */
        public static final long BAG = 3;
        /**
         * Живность
         */
        public static final long ANIMAL = 4;
        /**
         * Велосипед
         */
        public static final long BIKE = 6;
        /**
         * Велосипед 0
         */
        public static final long BIKE_0 = 57;
        /**
         * Разовый полный с местом
         */
        public static final long FULL_WITH_SEAT = 58;
        /**
         * Разовый детский с местом
         */
        public static final long CHILDREN_WITH_SEAT = 59;
        /**
         * Разовый детский без места
         */
        public static final long CHILDREN_WITH_SEAT_WITHOUT_SEAT = 60;
        /**
         * Трансфер разовый «туда»
         */
        public static final long SINGLE_TRANSFER_ONE_WAY = 111;
        /**
         * Трансфер разовый «Туда» совм оф.
         */
        public static final long SINGLE_TRANSFER_ONE_WAY_WITH_TICKET = 112;
        /**
         * Трансфер разовый «туда-обратно»
         */
        public static final long SINGLE_TRANSFER_TWO_WAY = 113;
        /**
         * Трансфер разовый «Туда и обратно» совм оф.
         */
        public static final long SINGLE_TRANSFER_TWO_WAY_WITH_TICKET = 114;
        /**
         * Трансфер разовый детский «туда»
         */
        public static final long SINGLE_CHILD_TRANSFER_ONE_WAY = 115;
        /**
         * Трансфер разовый детский «туда-обратно»
         */
        public static final long SINGLE_CHILD_TRANSFER_TWO_WAY = 116;
        /**
         * Трансфер абонемент на 1месяц
         */
        public static final long ONE_MONTH_TRANSFER_SEASON = 117;
        /**
         * Трансфер абонемент на 5 дней
         */
        public static final long FIVE_DAYS_TRANSFER_SEASON = 119;
        /**
         * Трансфер абонемент рабочего дня
         */
        public static final long WORK_DAY_TRANSFER_SEASON = 121;
        /**
         * Трансфер МИКС абонемент
         */
        public static final long MIKS_TRANSFER_SEASON = 123;

        //region Жуковский

        /**
         * Трансфер разовый «Туда» до аэропорта
         */
        public static final long SINGLE_TRANSFER_AIRPORT_FULL = 150;
        /**
         * Трансфер разовый «Туда» до аэропорта совм.оф.
         */
        public static final long SINGLE_TRANSFER_AIRPORT_FULL_WITH_TICKET = 151;
        /**
         * Трансфер разовый детский «Туда» до аэропорта
         */
        public static final long SINGLE_TRANSFER_AIRPORT_CHILD = 152;
        /**
         * Трансфер разовый детский «Туда» до аэропорта совм. оф.
         */
        public static final long SINGLE_TRANSFER_AIRPORT_CHILD_WITH_TICKET = 163;
        /**
         * Трансфер абонемент сроком на 1 месяц до аэропорта
         */
        public static final long ONE_MONTH_TRANSFER_AIRPORT = 153;
        /**
         * Трансфер абонемент сроком на 1 месяц до аэропорта совм. оф.
         */
        public static final long ONE_MONTH_TRANSFER_AIRPORT_JOINT = 154;
        /**
         * Трансфер абонемент «Рабочего дня»
         */
        public static final long WORK_DAY_TRANSFER_AIRPORT = 155;
        /**
         * Трансфер абонемент «Рабочего дня» совм. оф.
         */
        public static final long WORK_DAY_TRANSFER_AIRPORT_JOINT = 156;
        /**
         * Трансфер абонемент выходного дня сроком на 1 месяц до аэропорта 160
         */
        public static final long WEEKEND_TRANSFER_AIRPORT = 160;
        /**
         * Трансфер абонемент выходного дня сроком на 1 месяц до аэропорта совм. оф. 161
         */
        public static final long WEEKEND_TRANSFER_AIRPORT_JOINT = 161;

        //endregion
    }
}
