package ru.ppr.cppk.ui.fragment.pd.countrips.model;

import ru.ppr.core.dataCarrier.pd.v23.PdV23;
import ru.ppr.core.dataCarrier.pd.v24.PdV24;
import ru.ppr.utils.NumberUtils;

/**
 * Модель - представление хардварного счетчика для {@link PdV23}, {@link PdV24}.
 * <p>
 * http://agile.srvdev.ru/browse/CPPKPP-42009
 * Для комбинированного билета счетчик делится на три части:
 * 1.Счетчик перезаписи карты - увеличивается на 1 при каждой перезаписи карты на кассе или БПА.
 * Является старшей частью аппаратного счетчика карты (соответствующего по номеру номеру ПД на карте).
 * Занимает 9 бит. Дополняется до 16 бит и входит в подпись ПД после данных билета (кода тарифа ПД).
 * 2. Счетчик 7000-х поездок. Занимает 8 бит.
 * При записи ПД на кассе или БПА туда пишется значение равное 0xFF - <количество 7000-х поездок> * 2.
 * 3. Счетчик общий. Занимает 7 бит.
 * При записи ПД на кассе или БПА туда пишется значение равное 0x7F - <общее количество поездок>.
 * <p>
 * Такая схема со стартовым ненулевым значением используется,
 * чтобы можно было вычислить остаток поездок без запроса к НСИ.
 * <p>
 * Количество оставшихся поездок вычисляется следующим образом:
 * Для общего количества поездок: 0x7F - <значение в 3 части аппаратного счетчика>.
 * Для 7000-х поездок: (0xFF - <значение во 2 части аппаратного счетчика>) / 2 - здесь используется округление в меньшую сторону.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV23V24HwCounter {

    private int hwCounterValue;

    public PdV23V24HwCounter(int hwCounterValue) {
        this.hwCounterValue = hwCounterValue;
    }

    /**
     * Возвращает значение счетчика в сыром формате.
     */
    public int getHwCounterValue() {
        return hwCounterValue;
    }

    /**
     * Возвращает счетчик перезаписи карты.
     * Входит в ЭЦП, инкрементируется только при перезаписи карты
     */
    public int getRewriteCounter() {
        return NumberUtils.getBits(hwCounterValue, 9, 15);
    }

    /**
     * Устанавливает счетчик перезаписи карты.
     */
    public void setRewriteCounter(int rewriteCounter) {
        if (rewriteCounter >> 9 != 0) {
            throw new IllegalArgumentException("rewriteCounter out of bounds: " + rewriteCounter);
        }
        hwCounterValue = NumberUtils.setBits(hwCounterValue, rewriteCounter, 9, 15);
    }

    /**
     * Возвращает счетчик 7000.
     * Используется для поездок на 7000 поезд.
     * При записи карты пишется как FF - количество поездок на 7000 * 2.
     * При списывании поездок исходя из этой разницы можно понимать сколько поездок осталось.
     */
    public int getTrips7000Counter() {
        return NumberUtils.getBits(hwCounterValue, 8, 7);
    }

    /**
     * Устанавливает счетчик 7000.
     */
    public void setTrips7000Counter(int trips7000Counter) {
        if (trips7000Counter >> 8 != 0) {
            throw new IllegalArgumentException("trips7000Counter out of bounds: " + trips7000Counter);
        }
        hwCounterValue = NumberUtils.setBits(hwCounterValue, trips7000Counter, 8, 7);
    }

    /**
     * Возвращает счетчик общий.
     * Общий счетчик поездок на 6000 поезд.
     * При записи карты пишется как 7F - количество поездок на 6000.
     * При списывании поездок исходя из этой разницы можно понимать сколько поездок осталось.
     */
    public int getTripsTotalCounter() {
        return NumberUtils.getBits(hwCounterValue, 7, 0);
    }

    /**
     * Устанавливает общий счетчик.
     */
    public void setTripsTotalCounter(int tripsTotalCounter) {
        if (tripsTotalCounter >> 7 != 0) {
            throw new IllegalArgumentException("tripsTotalCounter out of bounds: " + tripsTotalCounter);
        }
        hwCounterValue = NumberUtils.setBits(hwCounterValue, tripsTotalCounter, 7, 0);
    }

    @Override
    public String toString() {
        return "PdV23V24HwCounter{" +
                "hwCounterValue=" + hwCounterValue +
                ", getRewriteCounter=" + getRewriteCounter() +
                ", getTrips7000Counter=" + getTrips7000Counter() +
                ", getTripsTotalCounter=" + getTripsTotalCounter() +
                '}';
    }
}
