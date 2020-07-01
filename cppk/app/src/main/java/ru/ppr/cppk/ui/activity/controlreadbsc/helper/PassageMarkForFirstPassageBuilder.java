package ru.ppr.cppk.ui.activity.controlreadbsc.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.AuthCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsOnePdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsTwoPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.IpkReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithFlags;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMarkWithPassageType;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v5.PassageMarkV5Impl;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v7.PassageMarkV7Impl;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8;
import ru.ppr.core.dataCarrier.smartCard.passageMark.v8.PassageMarkV8Impl;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.logic.NeedCreateControlEventChecker;
import ru.ppr.logger.Logger;

/**
 * Билдер метки прохода для записи на карту в случае, если на карте нет метки прохода (мусор).
 * http://agile.srvdev.ru/browse/CPPKPP-29827
 *
 * @author Aleksandr Brazhkin
 */
public class PassageMarkForFirstPassageBuilder {

    private static final String TAG = Logger.makeLogTag(PassageMarkForFirstPassageBuilder.class);

    /**
     * Номер турникета на станции, через который был совершен проход по ПД. На ПТК записывается 255. При продаже - 0.
     */
    private static final int GATE_NUMBER_FOR_PTK = 255; // В будущем: 07.11.2017 Убрать отсюда

    private final NeedCreateControlEventChecker needCreateControlEventChecker;

    @Inject
    PassageMarkForFirstPassageBuilder(NeedCreateControlEventChecker needCreateControlEventChecker) {
        this.needCreateControlEventChecker = needCreateControlEventChecker;
    }

    @Nullable
    public PassageMark build(@NonNull CardReader cardReader, @NonNull List<PD> legacyPdList) {
        if (cardReader instanceof CppkNumberOfTripsOnePdReader) {
            return buildV5PassageMark(legacyPdList);
        } else if (cardReader instanceof CppkNumberOfTripsTwoPdReader) {
            return buildV8PassageMark(legacyPdList);
        } else if (cardReader instanceof CppkReader) {
            return buildV8PassageMark(legacyPdList);
        } else if (cardReader instanceof IpkReader) {
            return buildV8PassageMark(legacyPdList);
        } else {
            return null;
        }
    }

    @Nullable
    public PassageMark build(@NonNull CardReader cardReader, @NonNull ServiceData serviceData) {
        if (cardReader instanceof AuthCardReader) {
            return buildV7PassageMark(serviceData);
        } else {
            return null;
        }
    }

    @Nullable
    private PassageMarkV8 buildV8PassageMark(@NonNull List<PD> legacyPdList) {
        Logger.trace(TAG, "buildV8PassageMark");

        PD pd1 = legacyPdList != null && legacyPdList.size() > 0 ? legacyPdList.get(0) : null;
        PD pd2 = legacyPdList != null && legacyPdList.size() > 1 ? legacyPdList.get(1) : null;

        PD pdForMark;

        if (pd1 != null && pd2 != null) {
            // Если на карте 2 ПД,  выбираем ПД, по которому запишем проход
            // Теорертически, все проверки ради обработки ПД v.19 и ПД v.20
            if (needCreateControlEventChecker.check(pd1)) {
                // ПД №1 является контролируемым, используем его для записи прохода
                pdForMark = pd1;
            } else if (needCreateControlEventChecker.check(pd2)) {
                // ПД №2 является контролируемым, используем его для записи прохода
                pdForMark = pd2;
            } else {
                // Оба ПД не являются контролируемыми, используем ПД №1 для записи прохода
                pdForMark = pd1;
            }
        } else if (pd1 != null) {
            pdForMark = pd1;
        } else if (pd2 != null) {
            pdForMark = pd2;
        } else {
            // http://agile.srvdev.ru/browse/CPPKPP-42779
            // Если нет ПД на карте, метку прохода восстанавливать не нужно
            pdForMark = null;
        }

        if (pdForMark == null) {
            return null;
        }

        PassageMarkV8Impl passageMarkV8 = new PassageMarkV8Impl();
        // Код станции ставим в 0
        // https://aj.srvdev.ru/browse/CPPKPP-29827
        passageMarkV8.setPassageStationCode(0);
        // 255 говорит о том, что списание было на ПТК
        passageMarkV8.setPdTurnstileNumber(GATE_NUMBER_FOR_PTK, pdForMark.orderNumberPdOnCard);
        passageMarkV8.setPassageTypeForPd(PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION, pdForMark.orderNumberPdOnCard);
        // Показание счетчика не трогаем
        passageMarkV8.setUsageCounterValue(0);
        passageMarkV8.setPassageStatusForPd(PassageMarkWithFlags.PASSAGE_STATUS_EXISTS, pdForMark.orderNumberPdOnCard);
        int pdPassageTime = (int) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - pdForMark.getSaleDate().getTime()));
        if (pdPassageTime <= 0) {
            // http://agile.srvdev.ru/browse/CPPKPP-33731
            // Нужно сохранить хронологический порядок
            // Сделаем так, якобы этот проход был через 1 секунду после продажи
            Logger.trace(TAG, "Silent time modification: pdPassageTime = " + pdPassageTime + ", saleTime = " + pdForMark.getSaleDate());
            pdPassageTime = 1;
        }
        passageMarkV8.setPdPassageTime(pdPassageTime, pdForMark.orderNumberPdOnCard);
        // Установим флаг привязки карты к пассажиру в false
        // Абрамов Андрей:
        // Нужно проверять ПД, записанные на карту
        // Если на карте есть ПД трансфер, писать true
        // Корчак Александр:
        // Исходим из предположения, что, ситуации, когда трансфер записан, а метки нет - не существует
        passageMarkV8.setBoundToPassenger(false);

        return passageMarkV8;
    }

    @Nullable
    private PassageMarkV5 buildV5PassageMark(@NonNull List<PD> legacyPdList) {
        Logger.trace(TAG, "buildV5PassageMark");
        // На карте должен быть только один ПД (v.7, v.18)
        PD pdForMark = legacyPdList.isEmpty() ? null : legacyPdList.get(0);
        if (pdForMark == null) {
            return null;
        }

        PassageMarkV5Impl passageMarkV5 = new PassageMarkV5Impl();
        // Код станции ставим в 0
        // https://aj.srvdev.ru/browse/CPPKPP-29827
        passageMarkV5.setPassageStationCode(0);
        // 255 говорит о том, что списание было на ПТК
        passageMarkV5.setPdTurnstileNumber(GATE_NUMBER_FOR_PTK, pdForMark.orderNumberPdOnCard);
        passageMarkV5.setPassageTypeForPd(PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION, pdForMark.orderNumberPdOnCard);
        // Показание счетчика не трогаем, т.к, он обновляется при вычитывании метки на уровне ридера
        passageMarkV5.setHwCounterValue(0);
        passageMarkV5.setPassageStatusForPd(PassageMarkWithFlags.PASSAGE_STATUS_EXISTS, pdForMark.orderNumberPdOnCard);
        int pdPassageTime = (int) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - pdForMark.getSaleDate().getTime()));
        if (pdPassageTime <= 0) {
            // http://agile.srvdev.ru/browse/CPPKPP-33731
            // Нужно сохранить хронологический порядок
            // Сделаем так, якобы этот проход был через 1 секунду после продажи
            Logger.trace(TAG, "Silent time modification: pdPassageTime = " + pdPassageTime + ", saleTime = " + pdForMark.getSaleDate());
            pdPassageTime = 1;
        }
        passageMarkV5.setPdPassageTime(pdPassageTime, pdForMark.orderNumberPdOnCard);

        return passageMarkV5;
    }

    @NonNull
    private PassageMark buildV7PassageMark(@NonNull ServiceData serviceData) {
        Logger.trace(TAG, "buildV7PassageMark");
        PassageMarkV7Impl passageMarkV7 = new PassageMarkV7Impl();
        // Код станции ставим в 0
        // https://aj.srvdev.ru/browse/CPPKPP-29827
        passageMarkV7.setPassageStationCode(0);
        // 255 говорит о том, что списание было на ПТК
        passageMarkV7.setTurnstileNumber(GATE_NUMBER_FOR_PTK);
        passageMarkV7.setPassageType(PassageMarkWithPassageType.PASSAGE_TYPE_TO_STATION);
        // Показание счетчика не трогаем
        passageMarkV7.setUsageCounterValue(0);
        int passageTime = (int) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - serviceData.getInitDateTime().getTime()));
        if (passageTime <= 0) {
            // http://agile.srvdev.ru/browse/CPPKPP-33731
            // Нужно сохранить хронологический порядок
            // Сделаем так, якобы этот проход был через 1 секунду после инициализации
            Logger.trace(TAG, "Silent time modification: passageTime = " + passageTime + ", initDateTime = " + serviceData.getInitDateTime());
            passageTime = 1;
        }
        passageMarkV7.setPassageTime(passageTime);
        // http://agile.srvdev.ru/browse/CPPKPP-42779
        // Номер зоны (на БСК) который был использован для прохода. При инициализации БСК - 0.
        passageMarkV7.setCoverageAreaNumber(0);

        return passageMarkV7;
    }
}
