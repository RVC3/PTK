package ru.ppr.cppk.utils;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import ru.ppr.cppk.localdb.model.Fee;

/**
 * Created by Dmitry Nevolin on 24.02.2016.
 */
public class Decimals {

    /**
     * Тип округления
     */
    public enum RoundMode {
        /**
         * Без округления
         */
        WITHOUT,
        /**
         * До десятых, число будет иметь вид *.2
         */
        TENTH,
        /**
         * До сотых, число будет иметь вид *.20
         */
        HUNDREDTH
    }

    //точность округления для операций деления
    private static final int PRECISION = 10;
    private static final int ROUND_TYPE = BigDecimal.ROUND_HALF_UP;

    public static final BigDecimal HUNDRED = new BigDecimal("100");
    public static final BigDecimal THOUSAND = new BigDecimal("1000");

    /**
     * Делит первый операнд на второй с точностью до 10 знаков, округляя по принципу BigDecimal.ROUND_HALF_UP
     *
     * @param first  первый операнд
     * @param second второй операнд
     * @return результат деления
     */
    public static BigDecimal divide(@NonNull BigDecimal first, @NonNull BigDecimal second) {
        return first.divide(second, PRECISION, ROUND_TYPE);
    }

    /**
     * Округляет число, в соответствии с переданным типом округления
     *
     * @param target    число для округления
     * @param roundMode тип округления
     * @return результат округления
     */
    public static BigDecimal round(@NonNull BigDecimal target, @NonNull RoundMode roundMode) {
        switch (roundMode) {
            case TENTH:
                return target.setScale(1, ROUND_TYPE).setScale(2, ROUND_TYPE);

            case HUNDREDTH:
                return target.setScale(2, ROUND_TYPE);

            case WITHOUT:
            default:
                return target;
        }
    }

    /**
     * Считает процентное значение от заданного числа, тип округления передается в параметрах
     *
     * @param percentage процент, может иметь значения от 0 до 100, иначе метод бросит IllegalArgumentException
     * @param target     заданное число, от которого надо посчитать процент, может быть только >= 0, иначе метод бросит IllegalArgumentException
     * @param roundMode  тип округления
     * @return процентное значение
     */
    public static BigDecimal percentage(@NonNull BigDecimal percentage, @NonNull BigDecimal target, @NonNull RoundMode roundMode) {
        if (lessThanZero(percentage) || percentage.compareTo(HUNDRED) == 1)
            throw new IllegalArgumentException("Percentage is less then 0 or more then 100");

        if (lessThanZero(percentage))
            throw new IllegalArgumentException("Target is less then 0");

        return round(target.multiply(divide(percentage, HUNDRED)), roundMode);
    }

    /**
     * Считает сколько процентов составляет входное значение от заданного числа, тип округления передается в параметрах
     *
     * @param value     входное значение, может быть только >= 0, иначе метод бросит IllegalArgumentException
     * @param target    заданное число, от которого надо посчитать процент, может быть только > 0, иначе метод бросит IllegalArgumentException
     * @param roundMode тип округления
     * @return процент
     */
    public static BigDecimal percentOfValue(@NonNull BigDecimal value, @NonNull BigDecimal target, @NonNull RoundMode roundMode) {
        if (lessThanZero(value))
            throw new IllegalArgumentException("Value is less then 0");

        if (!moreThanZero(target))
            throw new IllegalArgumentException("Target is less or equals then 0");

        return round(divide(value, target).multiply(HUNDRED), roundMode);
    }

    /**
     * Сравнивает число с нулём
     *
     * @param target сравниваемое число
     * @return true, если число = 0, false в противном случае
     */
    public static boolean isZero(@NonNull BigDecimal target) {
        return target.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Сравнивает число с нулём
     *
     * @param target сравниваемое число
     * @return true, если число > 0, false в противном случае
     */
    public static boolean moreThanZero(@NonNull BigDecimal target) {
        return target.compareTo(BigDecimal.ZERO) == 1;
    }

    /**
     * Сравнивает число с нулём
     *
     * @param target сравниваемое число
     * @return true, если число < 0, false в противном случае
     */
    public static boolean lessThanZero(@NonNull BigDecimal target) {
        return target.compareTo(BigDecimal.ZERO) == -1;
    }

    /**
     * Рассчитывает величину налоговой ставки в процентах на основе абсолютных значений.
     * Использовать только для тех случаев, когда величина налоговой ставки нигде явно не указана, например для Сбора {@link Fee}
     * http://agile.srvdev.ru/browse/CPPKPP-36768
     *
     * @param sum         - сумма включая НДС
     * @param vatIncluded - величина НДС в рублях
     * @param roundMode   - тип округления
     * @return
     */
    public static BigDecimal getVATRateIncludedFromValue(@NonNull BigDecimal sum, @NonNull BigDecimal vatIncluded, @NonNull RoundMode roundMode) {
        return isZero(sum) && isZero(vatIncluded) ? BigDecimal.ZERO : percentOfValue(vatIncluded, sum.subtract(vatIncluded), roundMode).setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal getVATValueExcludedFromRate(@NonNull BigDecimal netSum, @NonNull BigDecimal rate, @NonNull RoundMode roundMode) {
        return percentage(rate, netSum, roundMode);
    }

    /**
     * Вернет значение которое составляет процент в общей сумме
     *
     * @param sum       - сумма с включенным процентом
     * @param rate      - процент
     * @param roundMode - тип округления
     * @return
     */
    public static BigDecimal getVATValueIncludedFromRate(@NonNull BigDecimal sum, @NonNull BigDecimal rate, @NonNull RoundMode roundMode) {
        return round(divide(sum, rate.add(HUNDRED)).multiply(rate), roundMode);
    }

}
