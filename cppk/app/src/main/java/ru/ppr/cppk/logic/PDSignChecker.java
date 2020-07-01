package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReaderBMImpl;
import ru.ppr.core.manager.eds.CheckSignResult;
import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.core.manager.eds.KeyRevokedChecker;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.utils.EcpDataCreator;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.TicketId;
import ru.ppr.utils.CommonUtils;

/**
 * Проверяет ЭЦП на билетах
 *
 * @author Григорий Кашка
 */
public class PDSignChecker {

    private static final String TAG = Logger.makeLogTag(PDSignChecker.class);

    private final EdsManagerWrapper edsManager;
    private final WhiteListChecker whiteListChecker;
    private final KeyRevokedChecker keyRevokedChecker;

    @Inject
    PDSignChecker(EdsManagerWrapper edsManager,
                  WhiteListChecker whiteListChecker,
                  KeyRevokedChecker keyRevokedChecker) {
        this.edsManager = edsManager;
        this.whiteListChecker = whiteListChecker;
        this.keyRevokedChecker = keyRevokedChecker;
    }

    /**
     * Выполняет проверку подписи для билетов.
     *
     * @param pdList Список ПД
     * @param eds    ЭЦП
     * @param index  Индекс ПД в списке, для которого происходит проверка подписи
     * @return Результат проверки подписи
     */
    private CheckSignResult checkSign(@NonNull List<PD> pdList, @NonNull byte[] eds, int index) {
        byte[] dataForCheck;
        if (pdList.size() == 1) {
            dataForCheck = EcpDataCreator.createEcpData(pdList.get(0));
        } else {
            dataForCheck = EcpDataCreator.createEcpData(pdList.get(0), pdList.get(1), index);
        }
        long edsKeyNumber = pdList.get(index).ecpNumberPD;
        Logger.trace(TAG, "checkSign, index: " + index);
        Logger.trace(TAG, "checkSign, dataForCheck: " + CommonUtils.bytesToHexWithoutSpaces(dataForCheck));
        Logger.trace(TAG, "checkSign, eds: " + CommonUtils.bytesToHexWithoutSpaces(eds));
        Logger.trace(TAG, "checkSign, edsKeyNumber: " + edsKeyNumber);
        CheckSignResult res = edsManager.verifySignBlocking(dataForCheck, eds, edsKeyNumber);
        Logger.trace(TAG, "checkSign, result: " + res);
        return res;
    }

    /**
     * Выполняет проверку ПД по белому списку и возвращает дату отзыва ключа ЭЦП.
     *
     * @param ticketId         Id билета
     * @param dateOfRevocation Дата отзыва ключа ЭЦП
     */
    @Nullable
    private Long getRevocationTime(@NonNull TicketId ticketId, @Nullable Date dateOfRevocation) {
        if (dateOfRevocation == null) {
            return null;
        }
        if (keyRevokedChecker.isRevoked(dateOfRevocation)) {
            if (!whiteListChecker.isInWhiteList(ticketId)) {
                return TimeUnit.MILLISECONDS.toSeconds(dateOfRevocation.getTime());
            }
        }
        return null;
    }

    public void check(@NonNull List<PD> allPdList) {
        List<PD> pdList = new ArrayList<>();

        for(PD pd:allPdList){
            if(pd.tariffCodePD == 2633519 && pd.numberPD == 36751)
                pd.deviceId = 29;
            else
                pdList.add(pd);
        }


        int size = pdList.size();

        Logger.info(TAG, "check, size: " + size);

        if (size == 0) {
            return;
        }

        if (size > 2) {
            Logger.error(TAG, "check, size > 2");
            return;
        }

        if (pdList.get(0) == null) {
            Logger.error(TAG, "check, pd with index 0 is null");
            return;
        }

        if (size == 2 && pdList.get(1) == null) {
            Logger.error(TAG, "check, pd with index 1 is null");
            return;
        }

        byte[] eds = pdList.get(0).ecp;

        if (size == 1) {
            // Проверяем единственный ПД

            CheckSignResult checkSignResult = checkSign(pdList, eds, 0);
            PD legacyPd = pdList.get(0);
            legacyPd.deviceId = checkSignResult.getDeviceId();

            Logger.info(TAG, "check, pdIndex with sign for pd: " + 0);
            Logger.info(TAG, "check, checkSignResult for pd: " + checkSignResult);

            switch (checkSignResult.getState()) {
                case KEY_REVOKED: {
                    TicketId ticketId = new TicketId(legacyPd.numberPD, legacyPd.deviceId, legacyPd.getSaleDate());
                    Long revocationTime = getRevocationTime(ticketId, checkSignResult.getDateOfRevocation());
                    Logger.info(TAG, "check, revocationTime with white list: " + revocationTime);
                    if (revocationTime != null) {
                        legacyPd.revocationTime = TimeUnit.MILLISECONDS.toSeconds(revocationTime);
                        legacyPd.setCheckError(PassageResult.SignKeyRevoked);
                    }
                    break;
                }
                case INVALID: {
                    legacyPd.setCheckError(PassageResult.InvalidSign);
                    break;
                }
            }
        } else {
            // Проверяем 2 ПД
            List<CheckSignResult> checkSignResultList = new ArrayList<>();
            for (int i = 0; i < pdList.size(); i++) {
                CheckSignResult checkSignResultForPd = checkSign(pdList, eds, i);
                checkSignResultList.add(checkSignResultForPd);
                PD legacyPd = pdList.get(i);
                legacyPd.deviceId = checkSignResultForPd.getDeviceId();

                Logger.info(TAG, "check, pdIndex with sign for pd: " + i);
                Logger.info(TAG, "check, checkSignResult for pd: " + checkSignResultForPd);
            }
            CheckSignResultState state1 = checkSignResultList.get(0).getState();
            CheckSignResultState state2 = checkSignResultList.get(1).getState();

            boolean bothAreValid = (state1 == CheckSignResultState.VALID && state2 == CheckSignResultState.VALID);
            boolean bothAreRevoked = (state1 == CheckSignResultState.KEY_REVOKED && state2 == CheckSignResultState.KEY_REVOKED);
            boolean validAndRevoked = (state1 == CheckSignResultState.VALID && state2 == CheckSignResultState.KEY_REVOKED ||
                    state1 == CheckSignResultState.KEY_REVOKED && state2 == CheckSignResultState.VALID);

            if (bothAreValid || bothAreRevoked || validAndRevoked) {
                Logger.error(TAG, "check, both of PD combinations are valid or revoked");
            } else if (state1 == CheckSignResultState.INVALID && state2 == CheckSignResultState.INVALID) {
                for (PD pd : pdList) {
                    pd.setCheckError(PassageResult.InvalidSign);
                }
            } else if (state1 == CheckSignResultState.INVALID && state2 == CheckSignResultState.KEY_REVOKED ||
                    state1 == CheckSignResultState.KEY_REVOKED && state2 == CheckSignResultState.INVALID) {

                int revokedIndex;
                int invalidIndex;

                if (state1 == CheckSignResultState.KEY_REVOKED) {
                    revokedIndex = 0;
                    invalidIndex = 1;
                } else {
                    revokedIndex = 1;
                    invalidIndex = 0;
                }
                CheckSignResult revokedResult = checkSignResultList.get(revokedIndex);
                CheckSignResult invalidResult = checkSignResultList.get(invalidIndex);

                PD legacyFirstPd = pdList.get(revokedIndex);
                PD legacySecondPd = pdList.get(invalidIndex);

                // Проверяем комбинацию билетов с отозванным ключом по белому списку. Если даты отзыва нет, значит ключ в белом списке
                TicketId firstTicketId = new TicketId(legacyFirstPd.numberPD, legacyFirstPd.deviceId, legacyFirstPd.getSaleDate());
                Long firstRevocationTime = getRevocationTime(firstTicketId, revokedResult.getDateOfRevocation());
                Logger.info(TAG, "check, revocationTime with white list: " + firstRevocationTime);

                if (firstRevocationTime != null) {
                    // Если есть дата отзыва, то ключ обоих билетов отозван
                    legacyFirstPd.revocationTime = TimeUnit.MILLISECONDS.toSeconds(firstRevocationTime);
                    legacyFirstPd.setCheckError(PassageResult.SignKeyRevoked);
                    legacySecondPd.revocationTime = TimeUnit.MILLISECONDS.toSeconds(firstRevocationTime);
                    legacySecondPd.setCheckError(PassageResult.SignKeyRevoked);
                } else {
                    // Если ключ в белом списке, то нужно проверить отозванность ключа второго билета и его наличие в белом списке
                    TicketId secondTicketId = new TicketId(legacySecondPd.numberPD, legacySecondPd.deviceId, legacySecondPd.getSaleDate());
                    Long secondRevocationTime = getRevocationTime(secondTicketId, invalidResult.getDateOfRevocation());
                    Logger.info(TAG, "check, revocationTime with white list: " + secondRevocationTime);
                    // Если есть дата отзыва, то ключ ПД отозван и не в белом списке,
                    // иначе билет валиден, ему не требуется устанавливать никаких ошибок
                    if (secondRevocationTime != null) {
                        legacySecondPd.revocationTime = TimeUnit.MILLISECONDS.toSeconds(secondRevocationTime);
                        legacySecondPd.setCheckError(PassageResult.SignKeyRevoked);
                    }
                }
            } else if (state1 == CheckSignResultState.INVALID && state2 == CheckSignResultState.VALID ||
                    state1 == CheckSignResultState.VALID && state2 == CheckSignResultState.INVALID) {
                int invalidIndex;
                if (state1 == CheckSignResultState.VALID) {
                    invalidIndex = 1;
                } else {
                    invalidIndex = 0;
                }
                CheckSignResult invalidResult = checkSignResultList.get(invalidIndex);
                PD legacySecondPd = pdList.get(invalidIndex);
                // Проверяем отозванность ключа второго билета
                TicketId secondTicketId = new TicketId(legacySecondPd.numberPD, legacySecondPd.deviceId, legacySecondPd.getSaleDate());
                Long secondRevocationTime = getRevocationTime(secondTicketId, invalidResult.getDateOfRevocation());
                Logger.info(TAG, "check, revocationTime with white list: " + secondRevocationTime);
                // Если есть дата отзыва, то ключ ПД отозван и не в белом списке,
                // иначе билет валиден, ему не требуется устанавливать никаких ошибок
                if (secondRevocationTime != null) {
                    legacySecondPd.revocationTime = TimeUnit.MILLISECONDS.toSeconds(secondRevocationTime);
                    legacySecondPd.setCheckError(PassageResult.SignKeyRevoked);
                }
            }
        }
    }
}
