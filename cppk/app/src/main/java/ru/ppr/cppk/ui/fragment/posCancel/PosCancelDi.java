package ru.ppr.cppk.ui.fragment.posCancel;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.PrinterManager;

/**
 * @author Aleksandr Brazhkin
 */
class PosCancelDi {

    private final Di di;

    PosCancelDi(Di di) {
        this.di = di;
    }

    UiThread uiThread() {
        return di.uiThread();
    }

    PosManager posManager() {
        return di.getPosManager();
    }

    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    PrinterManager printerManager() {
        return di.printerManager();
    }
}
