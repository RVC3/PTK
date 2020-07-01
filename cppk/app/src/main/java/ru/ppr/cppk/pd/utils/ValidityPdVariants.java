package ru.ppr.cppk.pd.utils;

public enum ValidityPdVariants {

    NO_TICKETS, //на карте нет билетов
    ONE_OF_TWO_PD_IS_VALID, // если на карте 2 пд и 1 из них валиден
    TWO_PD_IS_VALID, // если на карте 2 пд и обы валидны
    TWO_PD_IS_INVALID, // если на карте 2 пд и оба не валидны
    ONE_PD_VALID, // если на карте 1 пд, или пд считан со штрихкода и он валидный
    ONE_PD_INVALID // если на карте 1 невалидный ПД
}
