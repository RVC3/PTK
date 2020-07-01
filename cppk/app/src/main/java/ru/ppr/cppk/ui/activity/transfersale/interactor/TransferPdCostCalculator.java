package ru.ppr.cppk.ui.activity.transfersale.interactor;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.dagger.ActivityScope;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.logic.base.PdCostCalculator;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleData;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Tariff;

/**
 * Калькулятор цен.
 *
 * @author Dmitry Nevolin
 */
public final class TransferPdCostCalculator extends PdCostCalculator {

    private final TransferSaleData transferSaleData;

    @Inject
    TransferPdCostCalculator(@NonNull TransferSaleData transferSaleData) {
        this.transferSaleData = transferSaleData;
    }

    @Override
    protected List<Tariff> getTariffsThere() {
        return transferSaleData.getTariffsThere();
    }

    @Override
    protected List<Tariff> getTariffsBack() {
        return transferSaleData.getTariffsBack();
    }

    @Override
    protected TicketWayType getDirection() {
        return transferSaleData.getDirection();
    }

    @Override
    protected List<Pair<ExemptionForEvent, Exemption>> getExemptions() {
        return null;
    }

    @Override
    protected boolean isIncludeFee() {
        return transferSaleData.isIncludeFee();
    }

    @Override
    protected ProcessingFee getProcessingFee() {
        return transferSaleData.getProcessingFee();
    }

    @Override
    protected int getPdCount() {
        return 1;
    }

}
