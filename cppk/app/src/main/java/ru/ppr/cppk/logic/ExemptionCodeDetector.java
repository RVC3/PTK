package ru.ppr.cppk.logic;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithExemption;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.EttCardInformation;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.SkmSkmoIpkCardInformation;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;

/**
 * Определяет код льготы из ПД и информации о карте
 *
 * @author Dmitry Nevolin
 */
public class ExemptionCodeDetector {

    @Inject
    ExemptionCodeDetector() {

    }

    @Nullable
    public Long detect(@Nullable Pd pd, @Nullable CardInformation cardInformation) {
        // Чтобы не делать лишних проверок
        if (pd == null && cardInformation == null) {
            return null;
        }
        // Сперва проверяем сам ПД на наличие льготы
        if (pd instanceof PdWithExemption) {
            return (long) ((PdWithExemption) pd).getExemptionCode();
        }
        // Теперь проверяем все карты у которых есть BscInformation, в которых указана льгота
        if (cardInformation instanceof EttCardInformation) {
            return getExemptionCodeFromBscInformation(((EttCardInformation) cardInformation).getBscInformation());
        }
        if (cardInformation instanceof SkmSkmoIpkCardInformation) {
            return getExemptionCodeFromBscInformation(((SkmSkmoIpkCardInformation) cardInformation).getBscInformation());
        }

        return null;
    }

    private Long getExemptionCodeFromBscInformation(@Nullable BscInformation bscInformation) {
        Integer exemptionCode = bscInformation != null ? bscInformation.getExemptionCode() : null;
        return exemptionCode != null ? Long.valueOf(exemptionCode) : null;
    }

}
