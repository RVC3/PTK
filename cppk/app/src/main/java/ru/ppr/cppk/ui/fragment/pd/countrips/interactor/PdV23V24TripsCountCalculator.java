package ru.ppr.cppk.ui.fragment.pd.countrips.interactor;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.v23.PdV23;
import ru.ppr.core.dataCarrier.pd.v24.PdV24;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.PdV23V24HwCounter;

/**
 * Калькулятор количества доступных поездок по {@link PdV23},{@link PdV24}.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV23V24TripsCountCalculator {


    @Inject
    PdV23V24TripsCountCalculator() {

    }

    public Result calcTripsCount(int hwCounterValue) {
        // http://agile.srvdev.ru/browse/CPPKPP-42009
        // Для комбинированного билета счетчик делится на три части:
        // 1.Счетчик перезаписи карты - увеличивается на 1 при каждой перезаписи карты на кассе или БПА.
        // Является старшей частью аппаратного счетчика карты (соответствующего по номеру номеру ПД на карте).
        // Занимает 9 бит. Дополняется до 16 бит и входит в подпись ПД после данных билета (кода тарифа ПД).
        // 2. Счетчик 7000-х поездок. Занимает 8 бит.
        // При записи ПД на кассе или БПА туда пишется значение равное 0xFF - <количество 7000-х поездок> * 2.
        // 3. Счетчик общий. Занимает 7 бит.
        // При записи ПД на кассе или БПА туда пишется значение равное 0x7F - <общее количество поездок>.
        //
        // Такая схема со стартовым ненулевым значением используется,
        // чтобы можно было вычислить остаток поездок без запроса к НСИ.
        //
        // Количество оставшихся поездок вычисляется следующим образом:
        // Для общего количества поездок: 0x7F - <значение в 3 части аппаратного счетчика>.
        // Для 7000-х поездок: (0xFF - <значение во 2 части аппаратного счетчика>) / 2 - здесь используется округление в меньшую сторону.
        PdV23V24HwCounter pdV23V24HwCounter = new PdV23V24HwCounter(hwCounterValue);

        int availableTripsTotalCount = 0x7f - pdV23V24HwCounter.getTripsTotalCounter();
        int availableTrips7000RawCount = (0xff - pdV23V24HwCounter.getTrips7000Counter()) / 2;

        // Защищаемся от выхода за границы
        int availableTrips7000Count = Math.min(availableTripsTotalCount, availableTrips7000RawCount);

        return new Result(availableTripsTotalCount, availableTrips7000Count, availableTrips7000RawCount);
    }

    public static class Result {
        /**
         * Общее количество оставшихся поездок
         */
        private final int availableTripsTotalCount;
        /**
         * Количество оставшихся поздок на поезд 7000.
         * Скорректированное значение, всегда <= availableTripsTotalCount
         */
        private final int availableTrips7000Count;
        /**
         * Количество оставшихся поздок на поезд 7000.
         * Значение в исходном виде, может быть > availableTripsTotalCount
         */
        private final int availableTrips7000RawCount;

        public Result(int availableTripsTotalCount, int availableTrips7000Count, int availableTrips7000RawCount) {
            this.availableTripsTotalCount = availableTripsTotalCount;
            this.availableTrips7000Count = availableTrips7000Count;
            this.availableTrips7000RawCount = availableTrips7000RawCount;
        }

        public int getAvailableTripsTotalCount() {
            return availableTripsTotalCount;
        }

        public int getAvailableTrips7000Count() {
            return availableTrips7000Count;
        }

        public int getAvailableTrips7000RawCount() {
            return availableTrips7000RawCount;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "availableTripsTotalCount=" + availableTripsTotalCount +
                    ", availableTrips7000Count=" + availableTrips7000Count +
                    ", availableTrips7000RawCount=" + availableTrips7000RawCount +
                    '}';
        }
    }
}
