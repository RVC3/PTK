package ru.ppr.ikkm;

/**
 * Используется как временное решение
 * при переводе всей техники на 20 процентный налог
 *
 * Показывает текущее состояние прошивки на фискальном регистраторе
 * Если прошивку ФР перешили на 20%
 * необходимо использовать вместо восемнадцити процентного налога двадцатипроцентный
 */
public class TwentyTaxHelper {
    private static final TwentyTaxHelper ourInstance = new TwentyTaxHelper();

    public static TwentyTaxHelper getInstance() {
        return ourInstance;
    }

    public boolean isTwentyDetected() {
        return twentyDetected;
    }

    public void setTwentyDetected() {
        this.twentyDetected = true;
    }

    private TwentyTaxHelper() {
        //   twentyDetected = false;
           twentyDetected = true;
    }

    private boolean twentyDetected;
}
