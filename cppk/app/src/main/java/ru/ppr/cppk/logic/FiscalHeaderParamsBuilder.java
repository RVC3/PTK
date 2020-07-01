package ru.ppr.cppk.logic;

import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.CashierSessionInfo;
import ru.ppr.cppk.helpers.DeviceSessionInfo;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.nsi.entity.ProductionSection;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.repository.ProductionSectionRepository;
import ru.ppr.nsi.repository.StationRepository;

/**
 * Билдер параметров для заголовка фискального чека.
 *
 * @author Aleksandr Brazhkin
 */
public class FiscalHeaderParamsBuilder {

    private final PrivateSettings mPrivateSettings;
    private final CommonSettings mCommonSettings;
    private final DeviceSessionInfo mDeviceSessionInfo;
    private final CashierSessionInfo mCashierSessionInfo;
    private final NsiVersionManager nsiVersionManager;
    private final StationRepository stationRepository;
    private final ProductionSectionRepository productionSectionRepository;

    public FiscalHeaderParamsBuilder(PrivateSettings privateSettings, CommonSettings commonSettings,
                                     DeviceSessionInfo deviceSessionInfo, CashierSessionInfo cashierSessionInfo, NsiVersionManager nsiVersionManager,
                                     StationRepository stationRepository, ProductionSectionRepository productionSectionRepository) {
        mPrivateSettings = privateSettings;
        mCommonSettings = commonSettings;
        mDeviceSessionInfo = deviceSessionInfo;
        mCashierSessionInfo = cashierSessionInfo;
        this.nsiVersionManager = nsiVersionManager;
        this.stationRepository = stationRepository;
        this.productionSectionRepository = productionSectionRepository;
    }

    /**
     * Собирает параметры для шаблона шапки фискального документа.
     *
     * @return Параметры
     * @throws Exception
     */
    public FiscalHeaderBuilder.Params build() {
        FiscalHeaderBuilder.Params.Builder builder = new FiscalHeaderBuilder.Params.Builder();

        if (mCashierSessionInfo.getCurrentCashier() == null) {
            // http://agile.srvdev.ru/browse/CPPKPP-41231
            // Возможно, здесь должна быть более сложная логика, но
            // пока так проверяем на то, что ПТК еще не содержит информации для печати заголовка
            // Ситуация возникает в случае, когда на свежем ПТК пытаются привязать принтер,
            // на котором открыта смена, и пытаются напечатать Z-отчет
            builder.setNoPrint(true);
            return builder.build();
        }

        builder.setDayCode(mPrivateSettings.getDayCode());

        int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();
        if (mPrivateSettings.isMobileCashRegister()) {
            Station station = stationRepository.load((long) mPrivateSettings.getCurrentStationCode(), nsiVersion);
            builder.setAreaName(station == null ? "" : station.getName());
        } else {
            ProductionSection productionSection = productionSectionRepository.load((long) mPrivateSettings.getProductionSectionId(), nsiVersion);
            builder.setAreaName(productionSection == null ? "" : productionSection.getName());
        }

        StationDevice stationDevice = mDeviceSessionInfo.getCurrentStationDevice();
        builder.setPtkNumber(stationDevice.getSerialNumber());
        builder.setDeviceId(stationDevice.getDeviceId());

        builder.setCarrierName(mCommonSettings.getCarrierName());

        Cashier cashier = mCashierSessionInfo.getCurrentCashier();
        builder.setCashierFullName(cashier.getFio());

        return builder.build();
    }
}
