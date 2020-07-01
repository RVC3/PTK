package ru.ppr.cppk.export.mapper;

import ru.ppr.cppk.entity.event.model.Cashier;

/**
 * @author Grigoriy Kashka
 */
public class CashierMapper {

    public ru.ppr.cppk.sync.kpp.model.Cashier toExportCashier(Cashier localCashier) {
        if (localCashier == null)
            return null;
        ru.ppr.cppk.sync.kpp.model.Cashier cashier = new ru.ppr.cppk.sync.kpp.model.Cashier();
        cashier.fio = localCashier.getFio();
        cashier.officialCode = localCashier.getOfficialCode();
        cashier.userLogin = localCashier.getLogin();
        return cashier;
    }

}
