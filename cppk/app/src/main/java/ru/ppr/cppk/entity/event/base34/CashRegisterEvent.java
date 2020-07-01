package ru.ppr.cppk.entity.event.base34;

/**
 * Created by Кашка Григорий on 13.12.2015.
 */
public class CashRegisterEvent {

    /**
     * Первичный ключ для таблицы
     */
    private long id;

    /**
     * Локальный id фискального устройства(регистратора)
     */
    private long cashRegisterId;

    /**
     * Локальный id кассира
     */
    private long cashierId;

    public long getCashRegisterId() {
        return cashRegisterId;
    }

    public void setCashRegisterId(long cashRegisterId) {
        this.cashRegisterId = cashRegisterId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getCashierId() {
        return cashierId;
    }

    public void setCashierId(long cashierId) {
        this.cashierId = cashierId;
    }
}
