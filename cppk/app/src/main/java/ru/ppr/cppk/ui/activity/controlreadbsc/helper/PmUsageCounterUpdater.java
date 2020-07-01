package ru.ppr.cppk.ui.activity.controlreadbsc.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsOnePdReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithUsageCounterValue;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v4.PassageMarkV4Impl;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v7.PassageMarkV7;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v7.PassageMarkV7Impl;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8Impl;
import ru.ppr.logger.Logger;

/**
 * Класс-помощник для обновления счетчика использования карты в метке прохода.
 * http://agile.srvdev.ru/browse/CPPKPP-42779
 *
 * @author Aleksandr Brazhkin
 */
public class PmUsageCounterUpdater {

    private static final String TAG = Logger.makeLogTag(PmUsageCounterUpdater.class);

    @Inject
    PmUsageCounterUpdater() {

    }

    /**
     * Производит увеличение счетчика использования карты в метке прохода
     *
     * @param cardReader     Ридер
     * @param oldPassageMark Метка прохода до увеличения счетчика
     * @return Новую метку прохода, записанную на карту
     * {@code null} в случае возникновения ошибки
     */
    @Nullable
    public PassageMark incrementUsageCounter(@NonNull CardReader cardReader, @NonNull PassageMark oldPassageMark) {
        PassageMark targetPassageMark;
        Logger.trace(TAG, "Old passage mark = " + oldPassageMark);
        if (oldPassageMark instanceof PassageMarkV4) {
            PassageMarkV4 passageMarkV4 = (PassageMarkV4) oldPassageMark;
            targetPassageMark = buildPassageMarkV4(passageMarkV4, getTargetPmUsageCounterValue(passageMarkV4));
        } else if (oldPassageMark instanceof PassageMarkV7) {
            PassageMarkV7 passageMarkV7 = (PassageMarkV7) oldPassageMark;
            targetPassageMark = buildPassageMarkV7(passageMarkV7, getTargetPmUsageCounterValue(passageMarkV7));
        } else if (oldPassageMark instanceof PassageMarkV8) {
            PassageMarkV8 passageMarkV8 = (PassageMarkV8) oldPassageMark;
            if (cardReader instanceof CppkNumberOfTripsOnePdReader) {
                // http://agile.srvdev.ru/browse/CPPKPP-41000
                // Специфичное формирование метки V8 для карты с одним ПД V7
                // В данном случае поле метки V8 "Счетчик использования карты" интерпретируется как
                // "Последнее показание счетчика при проходе", т.е. как поле метки V5
                // Считается, что такая ситуация может возникать только в тестовых условиях
                return oldPassageMark;
            } else {
                targetPassageMark = buildPassageMarkV8(passageMarkV8, getTargetPmUsageCounterValue(passageMarkV8));
            }
        } else if (oldPassageMark instanceof PassageMarkV5) {
            Logger.info(TAG, "Passage mark does not contain usage counter field");
            return oldPassageMark;
        } else {
            // На карте записана метка прохода неподдерживаемой версии
            // Не перезаписываем её
            Logger.info(TAG, "Unsupported passage mark version" + oldPassageMark.getVersion());
            return oldPassageMark;
        }
        Logger.trace(TAG, "New passage mark = " + targetPassageMark);

        if (!(cardReader instanceof WritePassageMarkReader)) {
            Logger.warning(TAG, "Reader doesn't support writing of passage mark");
            return null;
        }

        WritePassageMarkReader writePassageMarkReader = (WritePassageMarkReader) cardReader;
        WriteCardResult writePassageMarkResult = writePassageMarkReader.writePassageMark(targetPassageMark);

        if (!writePassageMarkResult.isSuccess()) {
            // Ошибка при записи новой метки прохода
            Logger.trace(TAG, "Write new passage mark error = " + writePassageMarkResult);
            return null;
        }

        if (!(cardReader instanceof ReadPassageMarkReader)) {
            Logger.warning(TAG, "Reader doesn't support reading of passage mark");
            return null;
        }

        ReadPassageMarkReader readPassageMarkReader = (ReadPassageMarkReader) cardReader;
        ReadCardResult<PassageMark> readNewPassageMarkResult = readPassageMarkReader.readPassageMark();
        if (!readNewPassageMarkResult.isSuccess() || readNewPassageMarkResult.getData() == null) {
            // Ошибка при считывании новой метки прохода
            Logger.trace(TAG, "Read new passage mark error = " + readNewPassageMarkResult);
            return null;
        }

        // Списание поездки произведено успешно
        return readNewPassageMarkResult.getData();
    }

    /**
     * Возвращает значение счетчика использования карты в метке прохода, которое мы стремимся получить.
     */
    private int getTargetPmUsageCounterValue(@NonNull PassageMarkWithUsageCounterValue passageMark) {
        return passageMark.getUsageCounterValue() + 1;
    }

    @NonNull
    private PassageMarkV4 buildPassageMarkV4(@NonNull PassageMarkV4 oldPassageMark, int usageCounterValue) {
        Logger.trace(TAG, "buildPassageMarkV4, usageCounterValue: " + usageCounterValue);
        PassageMarkV4Impl passageMarkV4 = new PassageMarkV4Impl();
        passageMarkV4.setUsageCounterValue(usageCounterValue);
        passageMarkV4.setPd1TurnstileNumber(oldPassageMark.getPd1TurnstileNumber());
        passageMarkV4.setPd2TurnstileNumber(oldPassageMark.getPd2TurnstileNumber());
        passageMarkV4.setPassageStationCode(oldPassageMark.getPassageStationCode());
        passageMarkV4.setPassageTypeForPd1(oldPassageMark.getPassageTypeForPd1());
        passageMarkV4.setPassageStatusForPd1(oldPassageMark.getPassageStatusForPd1());
        passageMarkV4.setPd1PassageTime(oldPassageMark.getPd1PassageTime());
        passageMarkV4.setPassageTypeForPd2(oldPassageMark.getPassageTypeForPd2());
        passageMarkV4.setPassageStatusForPd2(oldPassageMark.getPassageStatusForPd2());
        passageMarkV4.setPd2PassageTime(oldPassageMark.getPd2PassageTime());
        return passageMarkV4;
    }

    @NonNull
    private PassageMarkV7 buildPassageMarkV7(@NonNull PassageMarkV7 oldPassageMark, int usageCounterValue) {
        Logger.trace(TAG, "buildPassageMarkV7, usageCounterValue: " + usageCounterValue);
        PassageMarkV7Impl passageMarkV7 = new PassageMarkV7Impl();
        passageMarkV7.setUsageCounterValue(usageCounterValue);
        passageMarkV7.setTurnstileNumber(oldPassageMark.getTurnstileNumber());
        passageMarkV7.setPassageStationCode(oldPassageMark.getPassageStationCode());
        passageMarkV7.setPassageTime(oldPassageMark.getPassageTime());
        passageMarkV7.setPassageType(oldPassageMark.getPassageType());
        passageMarkV7.setCoverageAreaNumber(oldPassageMark.getCoverageAreaNumber());
        return passageMarkV7;
    }

    @NonNull
    private PassageMarkV8 buildPassageMarkV8(PassageMarkV8 oldPassageMark, int usageCounterValue) {
        Logger.trace(TAG, "buildPassageMarkV8, usageCounterValue: " + usageCounterValue);
        PassageMarkV8Impl passageMarkV8 = new PassageMarkV8Impl();
        passageMarkV8.setUsageCounterValue(usageCounterValue);
        passageMarkV8.setPd1TurnstileNumber(oldPassageMark.getPd1TurnstileNumber());
        passageMarkV8.setPd2TurnstileNumber(oldPassageMark.getPd2TurnstileNumber());
        passageMarkV8.setPassageStationCode(oldPassageMark.getPassageStationCode());
        passageMarkV8.setPassageTypeForPd1(oldPassageMark.getPassageTypeForPd1());
        passageMarkV8.setPassageStatusForPd1(oldPassageMark.getPassageStatusForPd1());
        passageMarkV8.setPd1PassageTime(oldPassageMark.getPd1PassageTime());
        passageMarkV8.setPassageTypeForPd2(oldPassageMark.getPassageTypeForPd2());
        passageMarkV8.setPassageStatusForPd2(oldPassageMark.getPassageStatusForPd2());
        passageMarkV8.setPd2PassageTime(oldPassageMark.getPd2PassageTime());
        passageMarkV8.setBoundToPassenger(oldPassageMark.isBoundToPassenger());
        return passageMarkV8;
    }
}
