package ru.ppr.cppk.entity.event.base34;

/**
 * Created by Кашка Григорий on 13.12.2015.
 */
public class TestTicketEvent {

    private static final String TAG = TestTicketEvent.class.getSimpleName();

    /**
     * локальный id текущего события печати тестового ПД
     */
    private long id = -1;
    /**
     * ID сменного события, в рамках которого случилось это событие
     * Первичный ключ для таблицы CashRegisterWorkingShift
     */
    private long shiftEventId;

    /**
     * ID сменного события, в рамках которого случилось это событие
     * Первичный ключ для таблицы Check
     */
    private long checkId;

    /**
     * Статус события
     */
    private Status status;

    private long eventId;

    private long ticketTapeEventId;

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getTicketTapeEventId() {
        return ticketTapeEventId;
    }

    public void setTicketTapeEventId(long ticketTapeEventId) {
        this.ticketTapeEventId = ticketTapeEventId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getShiftEventId() {
        return shiftEventId;
    }

    public void setShiftEventId(long shiftEventId) {
        this.shiftEventId = shiftEventId;
    }

    public long getCheckId() {
        return checkId;
    }

    public void setCheckId(long checkId) {
        this.checkId = checkId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Статус события
     * Структура:
     * <pre>
     *            CREATED
     *               |
     *          PRE_PRINTING
     *         |           |
     * CHECK_PRINTED     BROKEN
     *      |
     *  COMPLETED
     * </pre>
     */
    public enum Status {
        /**
         * Создано в БД, может быть свободно удалено.
         */
        CREATED(0),
        /**
         * Напечатан чек, деньги дегли на фискальник.
         */
        CHECK_PRINTED(1),
        /**
         * Событие полностью сформировано.
         */
        COMPLETED(2),
        /**
         * Статус перед отправкой на ФР.
         */
        PRE_PRINTING(3),
        /**
         * Статус после синхронизации чека, когда нам известно, что данный чек не лёг на фискальник.
         */
        BROKEN(4);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status fromCode(int code) {
            for (Status item : values()) {
                if (item.code == code) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Unknown code = " + code);
        }
    }
}
