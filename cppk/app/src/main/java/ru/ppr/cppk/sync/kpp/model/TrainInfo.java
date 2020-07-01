package ru.ppr.cppk.sync.kpp.model;

import android.support.annotation.Nullable;

/**
 * Информация о поезде
 *
 * @author Grigoriy Kashka
 */
public class TrainInfo {

    /**
     * Категория поезда
     * («О» – пригородные пассажирские поезда, «С» - скорые пригородные поезда типа «Спутник» и т.д.)
     */
    public String TrainCategory;

    /**
     * Номер поезда
     * ПТК не знает номера поезда
     */
    //public string TrainNumber;

    /**
     * Класс вагона скорого пригородного поезда 7000-й нумерации без предоставления мест
     */
    public String CarClass;

    /**
     * Код RDS версии
     */
    @Nullable
    public Integer TrainCategoryCode;
}
