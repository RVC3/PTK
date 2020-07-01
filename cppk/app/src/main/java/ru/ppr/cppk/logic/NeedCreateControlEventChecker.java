package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.cppk.dataCarrier.PdFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.logger.Logger;

/**
 * Определяет, нужно ли создавать событие контроля ПД
 *
 * @author Dmitry Nevolin
 */
public class NeedCreateControlEventChecker {

    private static final String TAG = Logger.makeLogTag(NeedCreateControlEventChecker.class);

    private final TransferPdChecker transferPdChecker;
    private final PtkModeChecker ptkModeChecker;
    private final ServiceFeePdChecker serviceFeePdChecker;

    @Inject
    public NeedCreateControlEventChecker(@NonNull TransferPdChecker transferPdChecker,
                                         @NonNull PtkModeChecker ptkModeChecker,
                                         @NonNull ServiceFeePdChecker serviceFeePdChecker) {
        this.transferPdChecker = transferPdChecker;
        this.ptkModeChecker = ptkModeChecker;
        this.serviceFeePdChecker = serviceFeePdChecker;
    }

    @Deprecated
    public boolean check(@NonNull PD legacyPd) {
        Pd pd = new PdFromLegacyMapper().fromLegacyPd(legacyPd);

        boolean isTransferPd = transferPdChecker.check(pd);
        boolean isTransferControlMode = ptkModeChecker.isTransferControlMode();

        Logger.info(TAG, "isTransferPd=" + isTransferPd + ", isTransferControlMode=" + isTransferControlMode);
        // Если мы в режиме работы трансфера и билет трансфер,
        // то надо создавать событие, аналогично
        // если мы НЕ в режиме трансфера и билет НЕ трансфер
        boolean transferCheck = isTransferPd && isTransferControlMode || !isTransferPd && !isTransferControlMode;
        // Если данный ПД это НЕ услуга, то надо создавать событие
        boolean serviceFeeCheck = !serviceFeePdChecker.check(legacyPd);

        return transferCheck && serviceFeeCheck;
    }

    public boolean check2(@NonNull Pd pd) {
        boolean isTransferPd = transferPdChecker.check(pd);
        boolean isTransferControlMode = ptkModeChecker.isTransferControlMode();

        Logger.info(TAG, "isTransferPd=" + isTransferPd + ", isTransferControlMode=" + isTransferControlMode);
        // Если мы в режиме работы трансфера и билет трансфер,
        // то надо создавать событие, аналогично
        // если мы НЕ в режиме трансфера и билет НЕ трансфер
        boolean transferCheck = isTransferPd && isTransferControlMode || !isTransferPd && !isTransferControlMode;
        // Если данный ПД это НЕ услуга, то надо создавать событие
        boolean serviceFeeCheck = !serviceFeePdChecker.check2(pd);

        return transferCheck && serviceFeeCheck;
    }

}
