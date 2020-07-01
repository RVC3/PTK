package ru.ppr.cppk.utils;

/**
 * Created by Александр on 19.01.2016.
 */
public class MathUtils {

    public static double getVATRateIncludedFromValue(double sum, double vatIncluded) {
        double netSum = sum - vatIncluded;
        double rate = vatIncluded / (netSum / 100);
        double rateInPercents = Math.round(rate * 10) / 10;
        return rateInPercents;
    }

    public static double getVATValueExcludedFromRate(double netSum, double rate) {
        double vat = netSum + netSum * (rate / 100);
        vat = Math.round(vat * 100) / 100;
        double sum = netSum + vat;
        return sum;
    }

    public static double getVATValueIncludedFromRate(double sum, double rate) {
        double vat = sum / (100 + rate) * rate;
        return vat;
    }
}
