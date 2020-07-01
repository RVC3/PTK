package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.nsi.entity.Exemption;

/**
 * Черек на безденежный билет
 *
 * @author Grigoriy Kashka
 */
public class NoMoneyTicketChecker {

    /**
     * Выполнит проверку на безденежность билета
     *
     * @param fullPrice   -  прайс
     * @param fee         - сбор
     * @param isExemption - флаг наличия льготы
     * @return
     */
    public boolean check(@NonNull Price fullPrice, @Nullable Fee fee, boolean isExemption) {
        if (!isExemption) {
            return false;
        }
        BigDecimal payedSum = fullPrice.getPayed();
        BigDecimal payedSumExcludeFee = payedSum.subtract(fee == null ? BigDecimal.ZERO : fee.getTotal());
        return Decimals.isZero(payedSumExcludeFee);
    }

    /**
     * Выполнит проверку на безденежность билета
     *
     * @param exemption - льгота
     * @return
     */
    public boolean check(@Nullable Exemption exemption) {
        if (exemption == null) {
            return false;
        }
        return exemption.getPercentage() == 100;
    }

}
