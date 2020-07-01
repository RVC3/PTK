package ru.ppr.cppk.printer.paramBuilders;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.nsi.entity.ProductionSection;
import ru.ppr.nsi.entity.Station;

/**
 * Класс-хелпер для сбора данных для шапки отчета.
 */
public class ReportClicheParamsBuilder {

    /**
     * Собирает параметры для шаблона шапки отчета.
     *
     * @return Параметры
     * @throws Exception
     */
    public static ReportClicheTpl.Params build() throws Exception {
        ReportClicheTpl.Params params = new ReportClicheTpl.Params();

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();
        CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().getLastCashRegisterEvent();

        if (cashRegisterEvent != null) {
            CashRegister cashRegister = localDaoSession.cashRegisterDao().load(cashRegisterEvent.getCashRegisterId());
            params.EKLZNumber = cashRegister.getEKLZNumber();
            params.FNSerial = cashRegister.getFNSerial();
            params.INN = cashRegister.getINN();
        }
        PrivateSettings privateSettings = Globals.getInstance().getPrivateSettingsHolder().get();
        params.dayCode = privateSettings.getDayCode();

        if (privateSettings.isMobileCashRegister()) {
            Station station = Dagger.appComponent().stationRepository().load(
                    (long) privateSettings.getCurrentStationCode(),
                    Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId());
            params.PTKAreaName = station == null ? "" : station.getName();
        } else {
            ProductionSection productionSection = Dagger.appComponent().productionSectionRepository().load(
                    (long) privateSettings.getProductionSectionId(),
                    Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId());
            params.PTKAreaName = productionSection == null ? "" : productionSection.getName();
        }

        StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
        CommonSettings commonSettings = Dagger.appComponent().commonSettingsStorage().get();

        params.PTKNumber = stationDevice.getSerialNumber();
        params.deviceId = stationDevice.getDeviceId();
        Cashier cashier = Di.INSTANCE.getCashierSessionInfo().getCurrentCashier();
        params.cashierFullName = cashier.getFio();
        params.carrierName = commonSettings.getCarrierName();
        return params;
    }
}
