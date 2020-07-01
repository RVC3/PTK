package ru.ppr.cppk.logic.pdSale;

import android.support.annotation.NonNull;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.logic.PtkModeChecker;

/**
 * Билдер для {@link PdSaleRestrictionsParams}
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleRestrictionsParamsBuilder {

    private final PrivateSettings privateSettings;
    private final CommonSettings commonSettings;
    private final PtkModeChecker ptkModeChecker;

    @Inject
    public PdSaleRestrictionsParamsBuilder(PrivateSettings privateSettings,
                                           CommonSettings commonSettings,
                                           PtkModeChecker ptkModeChecker) {
        this.privateSettings = privateSettings;
        this.commonSettings = commonSettings;
        this.ptkModeChecker = ptkModeChecker;
    }

    @NonNull
    public PdSaleRestrictionsParams create(Date timestamp, int nsiVersion) {
        PdSaleRestrictionsParams pdSaleRestrictionsParams = new PdSaleRestrictionsParams();
        pdSaleRestrictionsParams.setTimestamp(timestamp);
        pdSaleRestrictionsParams.setNsiVersion(nsiVersion);
        pdSaleRestrictionsParams.setAllowedStationsCodes(commonSettings.getAllowedStationsCodes());
        pdSaleRestrictionsParams.setMobileCashRegister(ptkModeChecker.isMobileCashRegisterMode());
        pdSaleRestrictionsParams.setOutsideProductionSectionSaleEnabled(privateSettings.isOutsideProductionSectionSaleEnabled());
        pdSaleRestrictionsParams.setProductionSectionCode(privateSettings.getProductionSectionId());
        return pdSaleRestrictionsParams;
    }


    @NonNull
    public PdSaleRestrictionsParams createForTransfer(Date timestamp, int nsiVersion) {
        PdSaleRestrictionsParams pdSaleRestrictionsParams = create(timestamp, nsiVersion);
        PdSaleRestrictionsParams.TransferSaleData transferSaleData = new PdSaleRestrictionsParams.TransferSaleData();
        if (ptkModeChecker.isMobileCashRegisterInputMode()) {
            transferSaleData.setMobileCashRegisterStationCode((long) privateSettings.getCurrentStationCode());
        }

        pdSaleRestrictionsParams.setTransferSaleData(transferSaleData);
        return pdSaleRestrictionsParams;
    }
}
