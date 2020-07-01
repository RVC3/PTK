package ru.ppr.cppk.ui.activity.extraPayment;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.logic.DocumentNumberProvider;

/**
 * @author Aleksandr Brazhkin
 */
public class ExtraPaymentDi {

    private final Di di;

    ExtraPaymentDi(Di di) {
        this.di = di;
    }

    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    DocumentNumberProvider documentNumberProvider() {
        return di.documentNumberProvider();
    }
}
