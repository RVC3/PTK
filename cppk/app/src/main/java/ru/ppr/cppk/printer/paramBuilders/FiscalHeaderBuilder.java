package ru.ppr.cppk.printer.paramBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.ikkm.TextStyle;

/**
 * Постороитель заголовка для чека.
 *
 * @author Aleksandr Brazhkin
 */
public class FiscalHeaderBuilder {

    /**
     * Возврщает список строк для заголовка чека.
     *
     * @param params Параметры для заполнения заголовка
     * @return Список строк для заголовка чека.
     */
    public List<String> buildHeaderLines(Params params, TextFormatter textFormatter) {

        if (params.noPrint) {
            // http://agile.srvdev.ru/browse/CPPKPP-41231
            // Не печатаем заголовок, если ненужно
            return Collections.emptyList();
        }

        List<String> headerLines = new ArrayList<>(5);

        String dayCode = "К" + textFormatter.asDayCodeNumber(params.dayCode);
        String headerLine1 = params.carrierName;
        headerLines.add(headerLine1.toUpperCase());
        String headerLine2 = params.areaName;
        headerLines.add(headerLine2.toUpperCase());
        String headerLine3 = textFormatter.alignWidthFiscalText(params.cashierFullName, dayCode);
        headerLines.add(headerLine3.toUpperCase());
        String headerLine4part1 = "ПТК " + params.ptkNumber;
        String headerLine4part2 = "ID:" + textFormatter.asStr(params.deviceId);

        // настраиваем заголовки
        if (headerLine4part1.length() + headerLine4part2.length() + 1 > textFormatter.getWidthForTextStyle(TextStyle.FISCAL_NORMAL)) {
            // Не умещается в одну строку
            headerLines.add(headerLine4part1.toUpperCase());
            headerLines.add(headerLine4part2.toUpperCase());
        } else {
            // Умещается в одну строку
            String headerLine4 = headerLine4part1 + " " + headerLine4part2;
            headerLines.add(headerLine4.toUpperCase());
        }

        return headerLines;
    }

    public static class Params {

        Params(String areaName,
               String cashierFullName,
               int dayCode,
               String carrierName,
               String ptkNumber,
               Long deviceId,
               boolean noPrint) {
            this.areaName = areaName;
            this.cashierFullName = cashierFullName;
            this.dayCode = dayCode;
            this.carrierName = carrierName;
            this.ptkNumber = ptkNumber;
            this.deviceId = deviceId;
            this.noPrint = noPrint;
        }

        /**
         * Флаг, что заголовок не должен печататься
         */
        private boolean noPrint;
        /**
         * Название участка установки ПТК
         */
        private final String areaName;
        /**
         * Фамилия и инициалы кассира
         */
        private final String cashierFullName;
        /**
         * Код дня
         */
        private final int dayCode;
        /**
         * Наименование организации-перевозчика
         */
        private final String carrierName;
        /**
         * Обозначение ПТК и его заводской номер
         */
        private final String ptkNumber;
        /**
         * ID ПТК для SFT
         */
        private final Long deviceId;

        @Override
        public String toString() {
            return "FiscalHeaderBuilder.Params{" +
                    "noPrint=" + noPrint +
                    ", areaName='" + areaName + '\'' +
                    ", cashierFullName='" + cashierFullName + '\'' +
                    ", dayCode=" + dayCode +
                    ", carrierName='" + carrierName + '\'' +
                    ", ptkNumber='" + ptkNumber + '\'' +
                    ", deviceId=" + deviceId +
                    '}';
        }

        public static class Builder {
            private boolean noPrint;
            private String areaName;
            private String cashierFullName;
            private int dayCode;
            private String carrierName;
            private String ptkNumber;
            private Long deviceId;

            public Builder setNoPrint(boolean noPrint) {
                this.noPrint = noPrint;
                return this;
            }

            public Builder setAreaName(String areaName) {
                this.areaName = areaName;
                return this;
            }

            public Builder setCashierFullName(String cashierFullName) {
                this.cashierFullName = cashierFullName;
                return this;
            }

            public Builder setDayCode(int dayCode) {
                this.dayCode = dayCode;
                return this;
            }

            public Builder setCarrierName(String carrierName) {
                this.carrierName = carrierName;
                return this;
            }

            public Builder setPtkNumber(String ptkNumber) {
                this.ptkNumber = ptkNumber;
                return this;
            }

            public Builder setDeviceId(Long deviceId) {
                this.deviceId = deviceId;
                return this;
            }

            public FiscalHeaderBuilder.Params build() {
                return new FiscalHeaderBuilder.Params(
                        areaName,
                        cashierFullName,
                        dayCode,
                        carrierName,
                        ptkNumber,
                        deviceId,
                        noPrint
                );
            }
        }
    }
}
