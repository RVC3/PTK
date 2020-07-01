package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageTime;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithTurnstileNumber;
import ru.ppr.core.dataCarrier.smartCard.passageMark.troyka.PassageMarkTroyka;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v7.PassageMarkV7;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Билдер информации о последнем проходе.
 *
 * @author Aleksandr Brazhkin
 */
public class LastPassageInfoBuilder {

    @Inject
    LastPassageInfoBuilder() {

    }

    public Result build(@Nullable PassageMark passageMark, @Nullable List<Pd> pdList, @Nullable ServiceData serviceData) {

        if (passageMark == null) {
            return new Result(null, null);
        } else if (pdList != null) {
            return buildForPdList(passageMark, pdList);
        } else if (serviceData != null) {
            return buildForServiceData(passageMark, serviceData);
        } else {
            return new Result(null, null);
        }
    }

    private Result buildForPdList(@NonNull PassageMark passageMark, @NonNull List<Pd> pdList) {

        PassageMarkWithFlags passageMarkWithFlags = null;
        if (passageMark instanceof PassageMarkWithFlags) {
            passageMarkWithFlags = (PassageMarkWithFlags) passageMark;
        }

        PassageMarkWithPassageTime passageMarkWithPassageTime = null;
        if (passageMark instanceof PassageMarkWithPassageTime) {
            passageMarkWithPassageTime = (PassageMarkWithPassageTime) passageMark;
        }

        PassageMarkWithTurnstileNumber passageMarkWithTurnstileNumber = null;
        if (passageMark instanceof PassageMarkWithTurnstileNumber) {
            passageMarkWithTurnstileNumber = (PassageMarkWithTurnstileNumber) passageMark;
        }

        Long maxPassageTime = null;
        Integer maxPassageTimeIndex = null;

        if (passageMarkWithFlags != null && passageMarkWithPassageTime != null) {
            for (int i = 0; i < pdList.size(); i++) {
                Pd pd = pdList.get(i);
                if (pd != null && pd.getVersion() != PdVersion.V64) {
                    if (passageMarkWithFlags.getPassageStatusForPd(i) == PassageMarkWithFlags.PASSAGE_STATUS_EXISTS) {
                        long relativeTime = passageMarkWithPassageTime.getPdPassageTime(i);
                        Date saleTime = pd.getSaleDateTime();
                        long passageTime = saleTime.getTime() + TimeUnit.SECONDS.toMillis(relativeTime);
                        if (maxPassageTime == null || passageTime > maxPassageTime) {
                            maxPassageTime = passageTime;
                            maxPassageTimeIndex = i;
                        }
                    }
                }
            }
        }

        Integer turnstileNumber = null;
        if (maxPassageTimeIndex != null && passageMarkWithTurnstileNumber != null) {
            turnstileNumber = passageMarkWithTurnstileNumber.getPdTurnstileNumber(maxPassageTimeIndex);
        }

        return new Result(maxPassageTime, turnstileNumber);
    }

    private Result buildForServiceData(@NonNull PassageMark passageMark, @NonNull ServiceData serviceData) {
        if (passageMark instanceof PassageMarkV7) {
            PassageMarkV7 passageMarkV7 = (PassageMarkV7) passageMark;
            Date initTime = serviceData.getInitDateTime();
            long relativeTime = passageMarkV7.getPassageTime();
            long passageTime = initTime.getTime() + TimeUnit.SECONDS.toMillis(relativeTime);
            int turnstileNumber = passageMarkV7.getTurnstileNumber();
            return new Result(passageTime, turnstileNumber);
        }  else if (passageMark instanceof PassageMarkTroyka) {
            PassageMarkTroyka passageMarkV6 = (PassageMarkTroyka) passageMark;
            return new Result(passageMarkV6.getIntersectionLongTime(), 0);
        } else {
            return new Result(null, null);
        }
    }

    public static class Result {
        private final Long passageTime;
        private final Integer turnstileNumber;

        public Result(Long passageTime, Integer turnstileNumber) {
            this.passageTime = passageTime;
            this.turnstileNumber = turnstileNumber;
        }

        public Long getPassageTime() {
            return passageTime;
        }

        public Integer getTurnstileNumber() {
            return turnstileNumber;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "passageTime=" + passageTime +
                    ", turnstileNumber=" + turnstileNumber +
                    '}';
        }
    }
}
