package ru.ppr.cppk.logic.servicedatacontrol;

import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.inject.Inject;

import ru.ppr.core.manager.eds.CheckSignResult;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.helpers.controlbscstorage.ServiceTicketControlCardData;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ByteUtils;
import ru.ppr.utils.CommonUtils;

/**
 * Валидатор ЭЦП.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsChecker {

    private static final String TAG = Logger.makeLogTag(EdsChecker.class);

    private static final int NET_SERVICE_DATA_START_BYTE = 0;
    private static final int NET_SERVICE_DATA_BYTE_COUNT = 12;

    private final EdsManagerWrapper edsManager;

    @Inject
    EdsChecker(EdsManagerWrapper edsManager) {
        this.edsManager = edsManager;
    }

    @NonNull
    public CheckSignResult check(ServiceTicketControlEvent serviceTicketControlEvent, ServiceTicketControlCardData serviceTicketControlCardData) {
        byte[] eds = serviceTicketControlCardData.getEds();
        if (eds == null) {
            CheckSignResult checkSignResult = new CheckSignResult();
            checkSignResult.setState(CheckSignResultState.INVALID);
            checkSignResult.setDescription("eds == null");
            return checkSignResult;
        }
        byte[] data = buildDataForEdsCheck(serviceTicketControlCardData);
        return edsManager.verifySignBlocking(data, eds, serviceTicketControlEvent.getEdsKeyNumber());
    }

    private byte[] buildDataForEdsCheck(ServiceTicketControlCardData serviceTicketControlCardData) {

        byte[] cardUid = serviceTicketControlCardData.getCardInformation().getCardUid();
        // Дополняем до 8-ми байт
        byte[] cardUidForEds = Arrays.copyOf(cardUid, 8);
        String outerNumberStr = serviceTicketControlCardData.getCardInformation().getOuterNumberAsString();
        byte[] outerNumber = CommonUtils.generateByteArrayFromLong(Long.valueOf(outerNumberStr), ByteOrder.LITTLE_ENDIAN);
        byte[] serviceData = serviceTicketControlCardData.getRawServiceData();
        // Исключаем 4 байта номера ключа ЭЦП
        byte[] serviceDataWithoutEdsKey = ByteUtils.getBytesFromData(serviceData, NET_SERVICE_DATA_START_BYTE, NET_SERVICE_DATA_BYTE_COUNT);
        byte[] coverageAreaList = serviceTicketControlCardData.getRawCoverageAreaList();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            // 1. Служебные данные
            os.write(serviceDataWithoutEdsKey);
            // 2. Зоны действия
            os.write(coverageAreaList);
            // 3. UID карты
            os.write(cardUidForEds);
            // 4. Внешний номер карты
            os.write(outerNumber);
            return os.toByteArray();
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
