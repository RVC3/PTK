package ru.ppr.cppk.helpers;

import ru.ppr.cppk.entity.event.model.Cashier;

/**
 * In-memory хранилище информации о текущем кассире.
 *
 * @author Aleksandr Brazhkin
 */
public class CashierSessionInfo {

    /**
     * Текущий кассир
     */
    private Cashier currentCashier;

    public Cashier getCurrentCashier() {
        return currentCashier;
    }

    public void setCurrentCashier(Cashier currentCashier) {
        this.currentCashier = currentCashier;
    }
}
