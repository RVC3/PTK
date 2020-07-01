package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.logic.pdSale.PdSaleEnv;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParams;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.model.TariffsChain;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.utils.CollectionUtils;

/**
 * Проверяет необходимость показа кнопки "Оформить трансфер" на экране контроля ПД,
 * см. http://agile.srvdev.ru/browse/CPPKPP-37099
 *
 * @author Dmitry Nevolin
 */
public class TransferSaleButtonDetector {

    private final PdSaleEnv pdSaleEnv;
    private final PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder;

    @Inject
    TransferSaleButtonDetector(@NonNull PdSaleEnvFactory pdSaleEnvFactory,
                               @NonNull PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder) {
        this.pdSaleEnv = pdSaleEnvFactory.newPdSaleEnvForTransfer();
        this.pdSaleRestrictionsParamsBuilder = pdSaleRestrictionsParamsBuilder;
    }

    public boolean detect(@NonNull PD pd, @NonNull ReadForTransferParams readForTransferParams) {
        // Обновляем ограничения
        PdSaleRestrictionsParams pdSaleRestrictionsParams = pdSaleRestrictionsParamsBuilder.createForTransfer(readForTransferParams.getTimestamp(), readForTransferParams.getNsiVersion());
        PdSaleRestrictionsParams.TransferSaleData transferSaleData = pdSaleRestrictionsParams.getTransferSaleData();
        transferSaleData.setWithParentPd(true);
        transferSaleData.setParentPdSaleDateTime(pd.saleDatetimePD);
        transferSaleData.setParentPdStartDateTime(pd.getStartPdDate());
        transferSaleData.setParentPdTariffCode(pd.tariffCodePD);
        transferSaleData.setParentPdDirection(pd.wayType);
        pdSaleEnv.pdSaleRestrictions().update(pdSaleRestrictionsParams);

        // Проверяем, доступна ли хотя бы одна станция отправления для оформления трансфера
        List<TariffsChain> foundTariffs = pdSaleEnv.tariffsLoader().loadDirectTariffsThere(
                readForTransferParams.getDepartureStationCode(),
                readForTransferParams.getDestinationStationCode(),
                CollectionUtils.asSet(readForTransferParams.getTariffPlanCodes(), code -> code),
                readForTransferParams.getTicketTypeCode());
        return !foundTariffs.isEmpty();
    }
}
