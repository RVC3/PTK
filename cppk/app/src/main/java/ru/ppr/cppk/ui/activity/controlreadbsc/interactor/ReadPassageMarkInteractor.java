package ru.ppr.cppk.ui.activity.controlreadbsc.interactor;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsOnePdReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5Impl;
import ru.ppr.logger.Logger;

/**
 * Операция чтения метки прохода с карты.
 * Для карт ЦППК на количество поездок {@link CppkNumberOfTripsOnePdReader}
 * в случае несовпадения показаний счетчика на карте и показаний счетчика в метке
 * выполняется обновление оказаний счетчика в метке значением счетчика на карте.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadPassageMarkInteractor {

    private static final String TAG = Logger.makeLogTag(ReadPassageMarkInteractor.class);

    @Inject
    ReadPassageMarkInteractor() {

    }

    /**
     * Выполянет чтение метки прохода.
     *
     * @param cardReader Ридер
     * @return Результат чтения карты
     */
    public ReadCardResult<PassageMark> readPassageMark(@NonNull ReadPassageMarkReader cardReader) {
        if (cardReader instanceof CppkNumberOfTripsOnePdReader) {
            return getPassageMarkReadCardResult((CppkNumberOfTripsOnePdReader) cardReader);
        } else {
            // Читаем метку для остальных карт
            return cardReader.readPassageMark();
        }
    }

    private ReadCardResult<PassageMark> getPassageMarkReadCardResult(@NonNull CppkNumberOfTripsOnePdReader cardReader) {
        // Читаем метку с карт ЦППК на количество поездок (1 ПД на карту)
        CppkNumberOfTripsOnePdReader cppkNumberOfTripsOnePdReader = cardReader;

        // Читаем счетчик для первого ПД, т.к. на CppkNumberOfTripsOnePdReader только один ПД
        int pdIndexForHwCounter = 0;
        ReadCardResult<Integer> hardwareCounterResult = cppkNumberOfTripsOnePdReader.readHardwareCounter(pdIndexForHwCounter);

        if (hardwareCounterResult.isSuccess()) {
            ReadCardResult<PassageMark> passageMarkResult = cppkNumberOfTripsOnePdReader.readPassageMark();

            if (passageMarkResult.isSuccess()) {
                PassageMark passageMark = passageMarkResult.getData();
                if (passageMark != null) {
                    boolean shouldRewriteCounterValueInMark;
                    if (passageMark instanceof PassageMarkV5Impl) {
                        PassageMarkV5Impl passageMarkV5 = (PassageMarkV5Impl) passageMark;
                        passageMarkV5.setHwCounterValue(hardwareCounterResult.getData());
                        shouldRewriteCounterValueInMark = hardwareCounterResult.getData() != passageMarkV5.getHwCounterValue();
                    } else {
                        shouldRewriteCounterValueInMark = false;
                    }

                    if (shouldRewriteCounterValueInMark) {
                        WriteCardResult writePassageMarkResult = cppkNumberOfTripsOnePdReader.writePassageMark(passageMark);
                        if (!writePassageMarkResult.isSuccess()) {
                            Logger.error(TAG, "Could not rewrite counter in passage mark");
                        }
                    }
                }
                return passageMarkResult;
            } else {
                return passageMarkResult;
            }
        } else {
            return new ReadCardResult<>(hardwareCounterResult.getReadCardErrorType(), hardwareCounterResult.getDescription());
        }
    }
}
