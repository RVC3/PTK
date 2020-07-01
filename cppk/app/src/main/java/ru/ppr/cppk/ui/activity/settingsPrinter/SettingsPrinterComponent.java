package ru.ppr.cppk.ui.activity.settingsPrinter;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.logic.interactor.CheckShiftOpenedInDbInteractor;
import ru.ppr.cppk.logic.interactor.CloseShiftInDbInteractor;

/**
 * @author Aleksandr Brazhkin
 */
class SettingsPrinterComponent {

    private final Di di;

    SettingsPrinterComponent(Di di) {
        this.di = di;
    }

    CheckShiftOpenedInDbInteractor checkShiftOpenedInDbInteractor() {
        return new CheckShiftOpenedInDbInteractor(di.getShiftManager());
    }

    CloseShiftInDbInteractor closeShiftInDbInteractor() {
        return new CloseShiftInDbInteractor(di.getShiftManager(), di.getApp().getPaperUsageCounter(), di.nsiVersionManager(), di.localDaoSession().get());
    }

}
