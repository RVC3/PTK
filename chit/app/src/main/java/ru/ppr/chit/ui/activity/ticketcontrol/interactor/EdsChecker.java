package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.cardinformation.CardInformation;
import ru.ppr.core.manager.eds.CheckSignResult;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.edssft.model.GetKeyInfoResult;
import ru.ppr.edssft.model.VerifySignResult;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;

/**
 * Валидатор ЭЦП.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsChecker {

    private static final String TAG = Logger.makeLogTag(EdsChecker.class);

    private final EdsManagerWrapper edsManager;
    private final PdEncoderFactory pdDecoderFactory;

    @Inject
    EdsChecker(EdsManagerWrapper edsManager, PdEncoderFactory pdDecoderFactory) {
        this.edsManager = edsManager;
        this.pdDecoderFactory = pdDecoderFactory;
    }

    @NonNull
    CheckSignResult check(@NonNull Pd pd, @Nullable byte[] eds, @Nullable CardInformation cardInformation) {
        long edsKeyNumber = pd.getEdsKeyNumber();
        Logger.trace(TAG, "eds: " + CommonUtils.bytesToHexWithoutSpaces(eds));
        CheckSignResult checkSignResult = new CheckSignResult();
        if (eds == null) {
            checkSignResult.setState(CheckSignResultState.INVALID);
            checkSignResult.setDescription("eds == null");
            return checkSignResult;
        }
        byte[] data = buildDataForEdsCheck(pd, cardInformation);
        CheckSignResult verifySignResult = edsManager.verifySignBlocking(data, eds, edsKeyNumber);
        return verifySignResult;
    }

    private byte[] buildDataForEdsCheck(@NonNull Pd pd, @Nullable CardInformation cardInformation) {
        byte[] cardUidForEds = null;
        byte[] outerNumber = null;
        if (cardInformation != null) {
            byte[] cardUid = cardInformation.getCardUid();
            Logger.trace(TAG, "cardUid: " + CommonUtils.bytesToHexWithoutSpaces(cardUid));
            // Дополняем до 8-ми байт
            cardUidForEds = Arrays.copyOf(cardUid, 8);
            String outerNumberStr = cardInformation.getOuterNumberAsString();
            outerNumber = CommonUtils.generateByteArrayFromLong(Long.valueOf(outerNumberStr), ByteOrder.LITTLE_ENDIAN);
            Logger.trace(TAG, "outerNumber: " + CommonUtils.bytesToHexWithoutSpaces(outerNumber));
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            // 1. ПД
            os.write(pdDecoderFactory.create(pd).encodeWithoutEdsKeyNumber(pd));
            if (cardUidForEds != null) {
                // 2. UID карты
                os.write(cardUidForEds);
            }
            if (outerNumber != null) {
                // 3. Внешний номер карты
                os.write(outerNumber);
            }
            byte[] result = os.toByteArray();
            Logger.trace(TAG, "dataForEdsCheck: " + CommonUtils.bytesToHexWithoutSpaces(result));
            return result;
        } catch (IOException e) {
            Logger.error(TAG, e);
            return new byte[0];
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                Logger.error(TAG, e);
            }
        }
    }

}
