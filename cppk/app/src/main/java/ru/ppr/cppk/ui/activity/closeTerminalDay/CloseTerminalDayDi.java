package ru.ppr.cppk.ui.activity.closeTerminalDay;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.PrinterManager;

/**
 * @author Aleksandr Brazhkin
 */
class CloseTerminalDayDi {

    private final Di di;

    CloseTerminalDayDi(Di di) {
        this.di = di;
    }

    UiThread uiThread() {
        return di.uiThread();
    }

    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    PosManager posManager() {
        return di.getPosManager();
    }

    PrinterManager printerManager() {
        return di.printerManager();
    }

    CloseTerminalDayPresenter closeTerminalDayPresenter(){
        return new CloseTerminalDayPresenter();
    }
}
