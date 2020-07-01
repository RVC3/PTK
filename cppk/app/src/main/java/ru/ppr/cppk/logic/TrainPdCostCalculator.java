package ru.ppr.cppk.logic;

import android.util.Pair;

import java.util.List;

import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.logic.base.PdCostCalculator;
import ru.ppr.cppk.model.PdSaleData;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ProcessingFee;
import ru.ppr.nsi.entity.Tariff;

/**
 * Калькулятор стоимости ПД при продаже.
 *
 * @author Aleksandr Brazhkin
 */
public final class TrainPdCostCalculator extends PdCostCalculator {

    private final PdSaleData mPdSaleData;

    public TrainPdCostCalculator(PdSaleData pdSaleData) {
        this.mPdSaleData = pdSaleData;
    }


    @Override
    protected List<Tariff> getTariffsThere() {
        return mPdSaleData.getTariffsThere();
    }

    @Override
    protected List<Tariff> getTariffsBack() {
        return mPdSaleData.getTariffsBack();
    }

    @Override
    protected TicketWayType getDirection() {
        return mPdSaleData.getDirection();
    }

    @Override
    protected List<Pair<ExemptionForEvent, Exemption>> getExemptions() {
        return mPdSaleData.getExemptions();
    }

    @Override
    protected boolean isIncludeFee() {
        return mPdSaleData.isIncludeFee();
    }

    @Override
    protected ProcessingFee getProcessingFee() {
        return mPdSaleData.getProcessingFee();
    }

    @Override
    protected int getPdCount() {
        return mPdSaleData.getPdCount();
    }

}
