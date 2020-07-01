package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumSet;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Событие открытия/передачи/закрытия смены.
 *
 * @author Grigoriy Kashka
 */
public class ShiftEvent implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Id смены
     */
    private String shiftId;
    /**
     * Время открытяи смены
     */
    private Date startTime;
    /**
     * Время закрытия смены
     */
    private Date closeTime;
    /**
     * Время выполнения операции
     */
    private Date operationTime;
    /**
     * Порядковый номер смены внутри месяца
     */
    private int shiftNumber;
    /**
     * Id события открытия/закрытия месяца
     */
    private long monthEventId;
    /**
     * Статус события
     */
    private Status status;
    /**
     * Текущее состояние операции. С привязкой к ФР
     */
    private ShiftProgressStatus progressStatus;
    /**
     * Идентификатор чека, сформированного при выполнении события
     */
    @Nullable
    private Long checkId;
    /**
     * Id связанной сущности из таблицы CashRegisterEvent
     */
    private long cashRegisterEventId;
    /**
     * Id связанной сущности из таблицы Event
     */
    private transient long eventId;
    /**
     * Расход билетной ленты за смену
     */
    private long paperConsumption;
    /**
     * Флаг, говорящий о том, что счетчик был сброшен
     */
    private boolean paperCounterRestarted;
    /**
     * Сумма в ФР на момент события
     */
    private BigDecimal cashInFR;

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

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public long getMonthEventId() {
        return monthEventId;
    }

    public void setMonthEventId(long monthEventId) {
        this.monthEventId = monthEventId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ShiftProgressStatus getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(ShiftProgressStatus progressStatus) {
        this.progressStatus = progressStatus;
    }

    @Nullable
    public Long getCheckId() {
        return checkId;
    }

    public void setCheckId(@Nullable Long checkId) {
        this.checkId = checkId;
    }

    public long getCashRegisterEventId() {
        return cashRegisterEventId;
    }

    public void setCashRegisterEventId(long cashRegisterEventId) {
        this.cashRegisterEventId = cashRegisterEventId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getPaperConsumption() {
        return paperConsumption;
    }

    public void setPaperConsumption(long paperConsumption) {
        this.paperConsumption = paperConsumption;
    }

    public boolean isPaperCounterRestarted() {
        return paperCounterRestarted;
    }

    public void setPaperCounterRestarted(boolean paperCounterRestarted) {
        this.paperCounterRestarted = paperCounterRestarted;
    }

    public BigDecimal getCashInFR() {
        return cashInFR;
    }

    public void setCashInFR(BigDecimal cashInFR) {
        this.cashInFR = cashInFR;
    }

    public void setDeletedMark(boolean value) {
        this.deletedMark = value;
    }

    public boolean getDeletedMark(){
        return deletedMark;
    }

    /**
     * Статус события
     */
    public enum Status {
        /**
         * Открыта
         */
        STARTED(0),
        /**
         * Передана
         * (С точки зрения пользователя - открыта)
         */
        TRANSFERRED(5),
        /**
         * Закрыта
         */
        ENDED(10);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Nullable
        public static Status valueOf(int code) {
            for (Status status : Status.values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * Статус сменного события
     * <p>
     * Структура:
     * <pre>
     *          PRE_PRINTING
     *         |       |    |
     * CHECK_PRINTED   |  BROKEN
     *                 |
     *              COMPLETED
     * </pre>
     *
     * @author Grigoriy Kashka
     */
    public enum ShiftProgressStatus {
        /**
         * Статус перед отправкой на ФР.
         */
        PRE_PRINTING(1),
        /**
         * Событие легло на фискальник
         */
        CHECK_PRINTED(3),
        /**
         * Событие полностью сформировано.
         */
        COMPLETED(5),
        /**
         * Статус после синхронизации, когда нам известно, что операция не легла на фискальник.
         */
        BROKEN(7);

        private final int code;

        ShiftProgressStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ShiftProgressStatus fromCode(int code) {
            for (ShiftProgressStatus item : values()) {
                if (item.code == code) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Unknown code = " + code);
        }

        /**
         * Статусы событий, которые легли на ФР
         */
        public static EnumSet<ShiftProgressStatus> FINISHED_STATUSES = EnumSet.of(CHECK_PRINTED, COMPLETED);
    }
}
