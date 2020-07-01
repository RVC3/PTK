package ru.ppr.cppk.ui.fragment.pd.countrips.interactor;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.v19.PdV19;
import ru.ppr.core.dataCarrier.pd.v20.PdV20;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.PdV19V20HwCounter;

/**
 * Калькулятор количества доступных поездок по {@link PdV19},{@link PdV20}.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV19V20TripsCountCalculator {

    @Inject
    PdV19V20TripsCountCalculator() {

    }

    public int calcTripsCount(int hwCounterValue) {
        // http://agile.srvdev.ru/browse/CPPKPP-37914
        // Данные карты являются перезаписываемыми - только для нужд тестирования.
        // При записи билета на карту младшие 12 бит счетчика устанавливаются в соответстии со значением старших 12 бит (изначально там ноль, в проме всегда ноль).
        // Затем старшие 12 бит инкрементируются на максимальное количество поездок, в соответствии с видом билета.
        //  Т.о. разница между старшими и младшими 12 бит и есть оставшееся количество поездок.
        // На турникете при списании поездки проверяется разница между старшими и младшими 12 бит,
        // если поездки остались - инкрементируются младшие 12 бит.
        // Старшие 12 бит каждого счетчика подписываются при записи билетов.
        PdV19V20HwCounter pdV19V20HwCounter = new PdV19V20HwCounter(hwCounterValue);
        int totalTripsCount = pdV19V20HwCounter.maxCounterValue() + 1; // Включительно
        // Защищаемся от отрицательных значений
        totalTripsCount = Math.max(totalTripsCount, 0);
        int spentTripsCount = pdV19V20HwCounter.currentCounterValue();
        int availableTripsCount = totalTripsCount - spentTripsCount;
        // Защищаемся от отрицательных значений
        availableTripsCount = Math.max(availableTripsCount, 0);
        return availableTripsCount;
    }
}
