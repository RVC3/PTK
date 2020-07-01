package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Событие открытия/закрытия месяца.
 *
 * @author Artem Ushakov
 */
public class MonthEvent implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Id месяца
     */
    private String monthId;
    /**
     * Номер месяца
     */
    private int monthNumber;
    /**
     * Дата открытия месяца
     */
    private Date openDate;
    /**
     * Дата закрытия месяца
     */
    private Date closeDate;
    /**
     * Id связанной сущности из таблицы Event
     */
    private long eventId;
    /**
     * Id связанной сущности из таблицы CashRegisterEvent
     */
    private long cashRegisterEventId;
    /**
     * Статус события
     */
    private Status status;

    /**
     * отметка, что запись будет удалена сборщиком мусора
     */
    private boolean deletedMark = false;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getMonthId() {
        return monthId;
    }

    public void setMonthId(String monthId) {
        this.monthId = monthId;
    }

    public int getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(int monthNumber) {
        this.monthNumber = monthNumber;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public void setDeletedMark(boolean value) {
        this.deletedMark = value;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getCashRegisterEventId() {
        return cashRegisterEventId;
    }

    public void setCashRegisterEventId(long cashRegisterEventId) {
        this.cashRegisterEventId = cashRegisterEventId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean getDeletedMark(){
        return deletedMark;
    }

    /**
     * Статус события
     */
    public enum Status {
        /**
         * Открытие месяца
         */
        OPENED(0),
        /**
         * Закрытие месяца
         */
        CLOSED(1);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Nullable
        public static Status valueOf(int code) {
            for (Status item : values()) {
                if (item.code == code) {
                    return item;
                }
            }
            return null;
        }
    }

}
