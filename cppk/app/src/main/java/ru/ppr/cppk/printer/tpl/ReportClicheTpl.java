package ru.ppr.cppk.printer.tpl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.logger.Logger;

/**
 * Клише для отчетов (п.1.1.1)
 *
 * @author Brazhkin A.V.
 */
public class ReportClicheTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;
    private final boolean fnEklzSerialAllowed;

    public ReportClicheTpl(Params params, TextFormatter textFormatter, boolean fnEklzSerialAllowed) {
        this.params = params;
        this.textFormatter = textFormatter;
        this.fnEklzSerialAllowed = fnEklzSerialAllowed;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(params.carrierName.toUpperCase());
        printer.printTextInNormalMode(params.PTKAreaName.toUpperCase());
        String dayCode = "К" + textFormatter.asDayCodeNumber(params.dayCode);
        printer.printTextInNormalMode(textFormatter.alignWidthText(params.cashierFullName, dayCode));

        String headerLine1 = "ПТК " + params.PTKNumber;
        String headerLine2 = "ID:" + textFormatter.asStr(params.deviceId);

        // настраиваем заголовки
        if (headerLine1.length() + headerLine2.length() + 1 > textFormatter.getWidthForTextStyle(TextStyle.FISCAL_NORMAL)) {
            // Не умещается в одну строку, печатаем отдельно
            // В расчетах используется доступная ширина для фискального чека, хотя она и меньше.
            // Сделано для однобразия заголовка в фискальных и нефискальных документах.
            printer.printTextInNormalMode(headerLine1);
            printer.printTextInNormalMode(headerLine2);
        } else {
            // Умещается в одну строку, печатаем вместе
            printer.printTextInNormalMode(headerLine1 + " " + headerLine2);
        }
        if (params.INN != null) {
            printer.printTextInNormalMode("ИНН " + params.INN);
        }

        if (fnEklzSerialAllowed) {
            if (printer.isFederalLaw54Supported()) {
                if (params.FNSerial != null) {
                    printer.printTextInNormalMode("ФН " + params.FNSerial);
                    if( ! params.FNSerial.equals(printer.getFNSerial())) {
                        Log.d("====", "printToDriver: " + params.FNSerial + " " + printer.getFNSerial());

                        SharedPreferences preferences = Di.INSTANCE.getApp().getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        // Сохранение настроек
                        editor.putString(GlobalConstants.CashRegisterFnSerial, printer.getFNSerial());
                        Logger.info("====", "setCashRegister(" + printer.getFNSerial() + ")");
                        editor.apply();
                    }
                }
            } else {
                if (params.EKLZNumber != null)
                    printer.printTextInNormalMode("ЭКЛЗ " + params.EKLZNumber);
            }
        }

        printer.printTextInNormalMode(" ");
    }

    public static class Params {

        /**
         * Наименование организации-перевозчика
         */
        public String carrierName;
        /**
         * Название участка установки ПТК или станции в зависимости от режима работы ПТК
         */
        public String PTKAreaName;
        /**
         * Фамилия и инициалы кассира
         */
        public String cashierFullName;
        /**
         * Код дня
         */
        public Integer dayCode;
        /**
         * Обозначение ПТК и его заводской номер
         */
        public String PTKNumber;
        /**
         * ID ПТК для SFT
         */
        public Long deviceId;
        /**
         * Идентификационный номер налогоплательщика (ИНН) организации продавца
         */
        public String INN;
        /**
         * Наименование программно-аппаратного средства, обеспечивающего
         * некорректируемую регистрацию фискальных данных и его регистрационный
         * номер (номер ЭКЛЗ)
         */
        public String EKLZNumber;
        /**
         * Номер фискального накопителя
         */
        public String FNSerial;

    }

}
