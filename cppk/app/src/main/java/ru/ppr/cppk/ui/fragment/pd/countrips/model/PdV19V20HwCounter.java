package ru.ppr.cppk.ui.fragment.pd.countrips.model;

import ru.ppr.core.dataCarrier.pd.v19.PdV19;
import ru.ppr.core.dataCarrier.pd.v20.PdV20;
import ru.ppr.utils.NumberUtils;

/**
 * Модель - представление хардварного счетчика для {@link PdV19}, {@link PdV20}.
 * <p>
 * http://agile.srvdev.ru/browse/CPPKPP-37914
 * Данные карты являются перезаписываемыми - только для нужд тестирования.
 * При записи билета на карту младшие 12 бит счетчика устанавливаются в соответстии со значением старших 12 бит (изначально там ноль, в проме всегда ноль).
 * Затем старшие 12 бит инкрементируются на максимальное количество поездок, в соответствии с видом билета.
 * Т.о. разница между старшими и младшими 12 бит и есть оставшееся количество поездок.
 * На турникете при списании поездки проверяется разница между старшими и младшими 12 бит,
 * если поездки остались - инкрементируются младшие 12 бит.
 * Старшие 12 бит каждого счетчика подписываются при записи билетов.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV19V20HwCounter {

    private final int hwCounterValue;

    public PdV19V20HwCounter(int hwCounterValue) {
        this.hwCounterValue = hwCounterValue;
    }

    /**
     * Возвращает значение счетчика в сыром формате.
     */
    public int hwCounterValue() {
        return hwCounterValue;
    }

    /**
     * Возвращает максимальное значение счетчика.
     * При записи билета на карту младшие 12 бит счетчика устанавливаются в соответстии со значением старших 12 бит.
     * Конечное значение счетчика исходя из количества поездок.
     * Разница между старшими и младшими 12 бит и есть оставшееся количество поездок
     */
    public int maxCounterValue() {
        return NumberUtils.getBits(hwCounterValue, 12, 12);
    }

    /**
     * Возвращает текущее значение счетчика.
     */
    public int currentCounterValue() {
        return NumberUtils.getBits(hwCounterValue, 12, 0);
    }

    @Override
    public String toString() {
        return "PdV19V20HwCounter{" +
                "hwCounterValue=" + hwCounterValue +
                ", maxCounterValue=" + maxCounterValue() +
                ", currentCounterValue=" + currentCounterValue() +
                '}';
    }
}
