package ru.ppr.cppk.printer.rx.operation.saleCheck;

import java.util.Date;
import java.util.Locale;

import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.event.model34.ConnectionType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.ikkm.TextStyle;
import ru.ppr.ikkm.exception.PrinterException;

/**
 * @author Aleksandr Brazhkin
 */
public class SaleCheckTpl {

    private final Printer printer;
    private final TextFormatter textFormatter;
    private final Params params;

    SaleCheckTpl(Printer printer, TextFormatter textFormatter, Params params) {
        this.printer = printer;
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public void printToDriver() throws Exception {
        printer.printText("Документ № " + textFormatter.asStr06d(params.pdNumber));

        if (params.printCheckDateTime != null) {
            printer.printText(textFormatter.asDate_dd_MM_yyyy_HH_mm(params.printCheckDateTime));
        }

        if (params.smartCard != null) {
            if (params.connectionType == ConnectionType.TRANSFER) {
                printer.printText(textFormatter.alignCenter("ПРОЕЗДНЫМ ДОКУМЕНТОМ", TextStyle.TEXT_LARGE), TextStyle.TEXT_LARGE);
                printer.printText(textFormatter.alignCenter("НЕ ЯВЛЯЕТСЯ", TextStyle.TEXT_LARGE), TextStyle.TEXT_LARGE);
            } else {
                printer.printText(textFormatter.alignCenter("ПРОЕЗДНЫМ ДОКУМЕНТОМ"));
                printer.printText(textFormatter.alignCenter("НЕ ЯВЛЯЕТСЯ"));
            }
        }

        printer.printText(textFormatter.alignCenter(params.ticketTypeName.toUpperCase(Locale.getDefault()), TextStyle.TEXT_LARGE), TextStyle.TEXT_LARGE);

        if (params.parentPdNumber != null && params.connectionType == ConnectionType.TRANSFER) {
            printer.printText(textFormatter.alignCenter("к Док. №" + textFormatter.asStr06d(params.parentPdNumber) + " ID " + params.parentPdDeviceId, TextStyle.TEXT_NORMAL), TextStyle.TEXT_NORMAL);
            printer.printText(textFormatter.alignCenter("от " + textFormatter.asDate_dd_MM_yyyy_HH_mm(params.parentPdSaleDateTime), TextStyle.TEXT_NORMAL), TextStyle.TEXT_NORMAL);
        }

        if (params.connectionType == ConnectionType.SURCHARGE) {
            printer.printText(textFormatter.alignCenter(params.carrierName));
            printer.printText(textFormatter.alignCenter("ДОПЛАТА ДО СК"));
            printer.printText(textFormatter.alignCenter("к Док. №" + textFormatter.asStr06d(params.parentPdNumber)));
        }

        if (params.exemptionCode != null) {
            if (params.exemptionSmartCard != null) {
                printer.printText(textFormatter
                        .asStr(params.exemptionCode) + " " + params.exemptionOrganizationName);
                String smartCardTypeAndNumber = params.exemptionSmartCard
                        .getType().getAbbreviation()
                        + " № " + params.exemptionSmartCard.getOuterNumber();
                if (smartCardTypeAndNumber.length() > textFormatter.getWidth()) {
                    printer.printText(params.exemptionSmartCard.getType().getAbbreviation() + " №");
                    printer.printText(params.exemptionSmartCard.getOuterNumber());
                } else
                    printer.printText(smartCardTypeAndNumber);
            } else {
                if (params.exemptionDocumentNumber == null) {
                    printer.printText(textFormatter.asStr(params.exemptionCode) +
                            " " + params.exemptionOrganizationName + " № ЛЬГ. ДОК: -");
                } else {
                    printer.printText(textFormatter.asStr(params.exemptionCode) +
                            " " + params.exemptionOrganizationName + " № ЛЬГ. ДОК:");
                    printer.printText(params.exemptionDocumentNumber);

                }
            }
        }

        if (params.smartCard != null) {
            if (params.exemptionSmartCard == null) {
                String smartCardTypeAndNumber = params.smartCard
                        .getType().getAbbreviation() + " № " + params.smartCard.getOuterNumber();
                if (smartCardTypeAndNumber.length() > textFormatter.getWidth()) {
                    printer.printText(params.smartCard.getType().getAbbreviation() + " №");
                    printer.printText(params.smartCard.getOuterNumber());
                } else
                    printer.printText(smartCardTypeAndNumber);
            }
            printer.printText("ЗАПИСЬ " + String.valueOf(params.smartCard.getTrack() + 1));
        }

        if (params.connectionType == null) {
            printer.printText(params.carrierName);
            printer.printText(params.tariffPlanName.toUpperCase(Locale.getDefault()));
        }

        String direction;

        if (params.connectionType == ConnectionType.TRANSFER) {
            printer.printText("");
            printer.printText("ДЕЙСТВУЕТ");

            if (params.startDate.equals(params.endDate)) {
                printer.printText(textFormatter.asDate_dd_MMM_yyyy(params.startDate).toUpperCase(Locale.getDefault()));
            } else {
                printer.printText("С  " + textFormatter.asDate_dd_MMM_yyyy(params.startDate).toUpperCase(Locale.getDefault()));
                printer.printText("ПО " + textFormatter.asDate_dd_MMM_yyyy(params.endDate).toUpperCase(Locale.getDefault()));
            }
            // В связи с тем, что в НСИ заведен тариф на трансфер только в одну сторону,
            // то маршрут трансфера в чеке выводить через дефис,
            // не используя указатели направлений <=> или =>. Т.е. выводить АВТО ДОМОДЕД - АВТО Н.ДОМОДЕД
            // см. http://agile.srvdev.ru/browse/CPPKPP-37099
            direction = " - ";
        } else {
            direction = params.direction == TicketWayType.TwoWay ? "<==>" : "==>";
        }

        String departureStation = params.departureStationName.toUpperCase(Locale.getDefault());
        String destinationStation = params.destinationStationName.toUpperCase(Locale.getDefault());

        int stationsStrLength = departureStation.length() + direction.length() + destinationStation.length();

        if (stationsStrLength > textFormatter.getWidth()) {
            printer.printText(departureStation + direction);
            printer.printText(destinationStation);
        } else {
            printer.printText(departureStation + direction + destinationStation);
        }
    }

    public static class Params {
        /**
         * Время печати чека
         */
        Date printCheckDateTime;
        /**
         * Станция назначения
         */
        public String destinationStationName;
        /**
         * Станция отправления
         */
        public String departureStationName;
        /**
         * Направление
         */
        public TicketWayType direction;
        /**
         * Смарт-карта, на которую записан билет
         */
        public SmartCard smartCard = null;
        /**
         * Номер родительского ПД
         */
        public Integer parentPdNumber = null;
        /**
         * Ид оборудования, продавшего исходный ПД
         */
        public Long parentPdDeviceId = null;
        /**
         * Время продажи исходного ПД
         */
        public Date parentPdSaleDateTime = null;
        /**
         * Тип связи с родительским билетом
         */
        public ConnectionType connectionType = null;
        /**
         * Номер документа
         */
        public Integer pdNumber;
        /**
         * Вид ПД
         */
        public String ticketTypeName;
        /**
         * Тарифный план
         */
        public String tariffPlanName;
        /**
         * Код льготы
         */
        public Integer exemptionCode = null;
        /**
         * Наименование оргнанизации для льготы
         */
        public String exemptionOrganizationName = null;
        /**
         * Смарт-карта, с которой считана льгота
         */
        public SmartCard exemptionSmartCard = null;
        /**
         * Номер документа, с которого считана льгота
         */
        public String exemptionDocumentNumber = null;
        /**
         * Дата начала действия ПД
         */
        public Date startDate;
        /**
         * Дата окончания действия ПД
         */
        public Date endDate;
        /**
         * Название юридического лица перевозчика
         */
        public String carrierName;
    }

    interface TextFormatter {
        String asStr06d(int number);

        String alignCenter(String text);

        String alignCenter(String text, TextStyle textStyle);

        String asStr(long number);

        int getWidth();

        String asDate_dd_MM_yyyy_HH_mm(Date dateTime);

        String asDate_dd_MMM_yyyy(Date dateTime);
    }

    interface Printer {
        void printText(String text) throws PrinterException;

        void printText(String text, TextStyle textStyle) throws PrinterException;
    }

}
