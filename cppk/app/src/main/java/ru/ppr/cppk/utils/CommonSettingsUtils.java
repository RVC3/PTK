package ru.ppr.cppk.utils;

import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.ReportType;

/**
 * Created by Кашка Григорий on 30.03.2016.
 */
public class CommonSettingsUtils {

    public static CommonSettings loadCommonSettingsFromXmlFile(File xml) throws ParserConfigurationException, SAXException, IOException {
        CommonSettingsXmlHandler commonSettingsXmlHandler = new CommonSettingsXmlHandler();

        XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        xmlReader.setContentHandler(commonSettingsXmlHandler);
        xmlReader.parse(new InputSource(new FileReader(xml)));

        return commonSettingsXmlHandler.commonSettings;
    }

    public static void saveCommonSettingsToXmlFile(CommonSettings commonSettings, File xml) throws IOException {
        final String EMPTY = "";

        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(new FileWriter(xml));
        serializer.startDocument("UTF-8", true);

        serializer.startTag(EMPTY, CommonSettingsEntities.ptkCommonSettings);

        serializer.comment(CommonSettingsEntities.Comments.ppReportOpenShift);
        serializer.startTag(EMPTY, CommonSettings.Entities.REPORT_OPEN_SHIFT);
        for (ReportType reportType : commonSettings.getReportOpenShift()) {
            serializer.comment(reportType.getName());
            serializer.startTag(EMPTY, CommonSettingsEntities.reportType);
            serializer.text(reportType.name());
            serializer.endTag(EMPTY, CommonSettingsEntities.reportType);
        }
        serializer.endTag(EMPTY, CommonSettings.Entities.REPORT_OPEN_SHIFT);

        serializer.comment(CommonSettingsEntities.Comments.ppReportCloseShift);
        serializer.startTag(EMPTY, CommonSettings.Entities.REPORT_CLOSE_SHIFT);
        for (ReportType reportType : commonSettings.getReportCloseShift()) {
            serializer.comment(reportType.getName());
            serializer.startTag(EMPTY, CommonSettingsEntities.reportType);
            serializer.text(reportType.name());
            serializer.endTag(EMPTY, CommonSettingsEntities.reportType);
        }
        serializer.endTag(EMPTY, CommonSettings.Entities.REPORT_CLOSE_SHIFT);

        serializer.comment(CommonSettingsEntities.Comments.ppReportCloseMonth);
        serializer.startTag(EMPTY, CommonSettings.Entities.REPORT_CLOSE_MONTH);
        for (ReportType reportType : commonSettings.getReportCloseMonth()) {
            serializer.comment(reportType.getName());
            serializer.startTag(EMPTY, CommonSettingsEntities.reportType);
            serializer.text(reportType.name());
            serializer.endTag(EMPTY, CommonSettingsEntities.reportType);
        }
        serializer.endTag(EMPTY, CommonSettings.Entities.REPORT_CLOSE_MONTH);

        serializer.comment(CommonSettingsEntities.Comments.maxTimeAgoMark);
        serializer.startTag(EMPTY, CommonSettings.Entities.MAX_TIME_AGO_MARK);
        serializer.text(String.valueOf(commonSettings.getMaxTimeAgoMark()));
        serializer.endTag(EMPTY, CommonSettings.Entities.MAX_TIME_AGO_MARK);

        serializer.comment(CommonSettingsEntities.Comments.pBTMonthlySheetClosingMonthReq);
        serializer.startTag(EMPTY, CommonSettings.Entities.BT_MONTHLY_SHEET_CLOSING_MONTH_REQ);
        serializer.text(String.valueOf(commonSettings.isBtMonthlySheetClosingMonthReq()));
        serializer.endTag(EMPTY, CommonSettings.Entities.BT_MONTHLY_SHEET_CLOSING_MONTH_REQ);

        serializer.comment(CommonSettingsEntities.Comments.pDiscountMonthShiftSheetClosingMonthReq);
        serializer.startTag(EMPTY, CommonSettings.Entities.DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ);
        serializer.text(String.valueOf(commonSettings.isDiscountMonthShiftSheetClosingMonthReq()));
        serializer.endTag(EMPTY, CommonSettings.Entities.DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ);

        serializer.comment(CommonSettingsEntities.Comments.pDiscountShiftSheetClosingShiftReq);
        serializer.startTag(EMPTY, CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ);
        serializer.text(String.valueOf(commonSettings.isDiscountShiftSheetClosingShiftReq()));
        serializer.endTag(EMPTY, CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ);

        serializer.comment(CommonSettingsEntities.Comments.pDiscountShiftSheetOpeningShift);
        serializer.startTag(EMPTY, CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_OPENING_SHIFT);
        serializer.text(String.valueOf(commonSettings.isDiscountShiftSheetOpeningShift()));
        serializer.endTag(EMPTY, CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_OPENING_SHIFT);

        serializer.comment(CommonSettingsEntities.Comments.pMonthSheetClosingMonthReq);
        serializer.startTag(EMPTY, CommonSettings.Entities.MONTH_SHEET_CLOSING_MONTH_REQ);
        serializer.text(String.valueOf(commonSettings.isMonthSheetClosingMonthReq()));
        serializer.endTag(EMPTY, CommonSettings.Entities.MONTH_SHEET_CLOSING_MONTH_REQ);

        serializer.comment(CommonSettingsEntities.Comments.pSheetBlankingMonthClosingMonthReq);
        serializer.startTag(EMPTY, CommonSettings.Entities.SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ);
        serializer.text(String.valueOf(commonSettings.isSheetBlankingMonthClosingMonthReq()));
        serializer.endTag(EMPTY, CommonSettings.Entities.SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ);

        serializer.comment(CommonSettingsEntities.Comments.pSheetBlankingShiftClosingShiftReq);
        serializer.startTag(EMPTY, CommonSettings.Entities.SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ);
        serializer.text(String.valueOf(commonSettings.isSheetBlankingShiftClosingShiftReq()));
        serializer.endTag(EMPTY, CommonSettings.Entities.SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ);

        serializer.comment(CommonSettingsEntities.Comments.pSheetShiftCloseShiftReq);
        serializer.startTag(EMPTY, CommonSettings.Entities.SHEET_SHIFT_CLOSE_SHIFT_REQ);
        serializer.text(String.valueOf(commonSettings.isSheetShiftCloseShiftReq()));
        serializer.endTag(EMPTY, CommonSettings.Entities.SHEET_SHIFT_CLOSE_SHIFT_REQ);

        serializer.comment(CommonSettingsEntities.Comments.pTestPdPrintReq);
        serializer.startTag(EMPTY, CommonSettings.Entities.TEST_PD_PRINT_REQ);
        serializer.text(String.valueOf(commonSettings.isTestPdPrintReq()));
        serializer.endTag(EMPTY, CommonSettings.Entities.TEST_PD_PRINT_REQ);

        serializer.comment(CommonSettingsEntities.Comments.enableAnnulateAfterTimeOver);
        serializer.startTag(EMPTY, CommonSettings.Entities.ENABLE_ANNULATE_AFTER_TIME_OVER);
        serializer.text(String.valueOf(commonSettings.isEnableAnnulateAfterTimeOver()));
        serializer.endTag(EMPTY, CommonSettings.Entities.ENABLE_ANNULATE_AFTER_TIME_OVER);

        serializer.comment(CommonSettingsEntities.Comments.durationOfPdNextDay);
        serializer.startTag(EMPTY, CommonSettings.Entities.DURATION_OF_PD_NEXT_DAY);
        serializer.text(String.valueOf(commonSettings.getDurationOfPdNextDay()));
        serializer.endTag(EMPTY, CommonSettings.Entities.DURATION_OF_PD_NEXT_DAY);

        serializer.comment(CommonSettingsEntities.Comments.bankCode);
        serializer.startTag(EMPTY, CommonSettings.Entities.BANK_CODE);
        serializer.text(String.valueOf(commonSettings.getBankCode()));
        serializer.endTag(EMPTY, CommonSettings.Entities.BANK_CODE);

        serializer.comment(CommonSettingsEntities.Comments.carrierName);
        serializer.startTag(EMPTY, CommonSettings.Entities.CARRIER_NAME);
        serializer.text(String.valueOf(commonSettings.getCarrierName()));
        serializer.endTag(EMPTY, CommonSettings.Entities.CARRIER_NAME);

        serializer.comment(CommonSettingsEntities.Comments.termStoragePd);
        serializer.startTag(EMPTY, CommonSettings.Entities.TERM_STORAGE_PD);
        serializer.text(String.valueOf(commonSettings.getTermStoragePd()));
        serializer.endTag(EMPTY, CommonSettings.Entities.TERM_STORAGE_PD);

        serializer.comment(CommonSettingsEntities.Comments.timeChangesPeriod);
        serializer.startTag(EMPTY, CommonSettings.Entities.TIME_CHANGES_PERIOD);
        serializer.text(String.valueOf(commonSettings.getTimeChangesPeriod()));
        serializer.endTag(EMPTY, CommonSettings.Entities.TIME_CHANGES_PERIOD);

        serializer.comment(CommonSettingsEntities.Comments.timeElectronicRegistration);
        serializer.startTag(EMPTY, CommonSettings.Entities.TIME_ELECTRONIC_REGISTRATION);
        serializer.text(String.valueOf(commonSettings.getTimeElectronicRegistration()));
        serializer.endTag(EMPTY, CommonSettings.Entities.TIME_ELECTRONIC_REGISTRATION);

        if (commonSettings.getAllowedStationsCodes() != null) {
            serializer.comment(CommonSettingsEntities.Comments.allowedStationsCodes);
            serializer.startTag(EMPTY, CommonSettings.Entities.ALLOWED_STATIONS_CODES);
            for (long code : commonSettings.getAllowedStationsCodes()) {
                serializer.startTag(EMPTY, CommonSettingsEntities.code);
                serializer.text(String.valueOf(code));
                serializer.endTag(EMPTY, CommonSettingsEntities.code);
            }
            serializer.endTag(EMPTY, CommonSettings.Entities.ALLOWED_STATIONS_CODES);
        }

        serializer.comment(CommonSettingsEntities.Comments.screenOffTimeout);
        serializer.startTag(EMPTY, CommonSettings.Entities.SCREEN_OFF_TIMEOUT);
        serializer.text(String.valueOf(commonSettings.getScreenOffTimeout()));
        serializer.endTag(EMPTY, CommonSettings.Entities.SCREEN_OFF_TIMEOUT);

        serializer.comment(CommonSettingsEntities.Comments.posTerminalCheckPeriod);
        serializer.startTag(EMPTY, CommonSettings.Entities.POS_TERMINAL_CHECK_PERIOD);
        serializer.text(String.valueOf(commonSettings.getPosTerminalCheckPeriod()));
        serializer.endTag(EMPTY, CommonSettings.Entities.POS_TERMINAL_CHECK_PERIOD);

        serializer.comment(CommonSettingsEntities.Comments.autoCloseTime);
        serializer.startTag(EMPTY, CommonSettings.Entities.AUTO_CLOSE_TIME);
        serializer.text(String.valueOf(commonSettings.getAutoCloseTime()));
        serializer.endTag(EMPTY, CommonSettings.Entities.AUTO_CLOSE_TIME);

        serializer.comment(CommonSettingsEntities.Comments.selectDraftNsi);
        serializer.startTag(EMPTY, CommonSettings.Entities.SELECT_DRAFT_NSI);
        serializer.text(String.valueOf(commonSettings.isSelectDraftNsi()));
        serializer.endTag(EMPTY, CommonSettings.Entities.SELECT_DRAFT_NSI);

        serializer.comment(CommonSettingsEntities.Comments.logFullSQL);
        serializer.startTag(EMPTY, CommonSettings.Entities.LOG_FULL_SQL);
        serializer.text(String.valueOf(commonSettings.isLogFullSQL()));
        serializer.endTag(EMPTY, CommonSettings.Entities.LOG_FULL_SQL);

        serializer.comment(CommonSettingsEntities.Comments.autoBlockingTimeout);
        serializer.startTag(EMPTY, CommonSettings.Entities.AUTO_BLOCKING_TIMEOUT);
        serializer.text(String.valueOf(commonSettings.getAutoBlockingTimeout()));
        serializer.endTag(EMPTY, CommonSettings.Entities.AUTO_BLOCKING_TIMEOUT);

        serializer.comment(CommonSettingsEntities.Comments.autoBlockingEnabled);
        serializer.startTag(EMPTY, CommonSettings.Entities.AUTO_BLOCKING_ENABLED);
        serializer.text(String.valueOf(commonSettings.isAutoBlockingEnabled()));
        serializer.endTag(EMPTY, CommonSettings.Entities.AUTO_BLOCKING_ENABLED);

        serializer.comment(CommonSettingsEntities.Comments.timeZoneOffset);
        serializer.startTag(EMPTY, CommonSettings.Entities.TIME_ZONE_OFFSET);
        serializer.text(String.valueOf(commonSettings.getTimeZoneOffset()));
        serializer.endTag(EMPTY, CommonSettings.Entities.TIME_ZONE_OFFSET);

        serializer.comment(CommonSettingsEntities.Comments.ignoreCardValidityPeriod);
        serializer.startTag(EMPTY, CommonSettings.Entities.IGNORE_CARD_VALIDITY_PERIOD);
        serializer.text(String.valueOf(commonSettings.isIgnoreCardValidityPeriod()));
        serializer.endTag(EMPTY, CommonSettings.Entities.IGNORE_CARD_VALIDITY_PERIOD);

        serializer.comment(CommonSettingsEntities.Comments.extraPaymentWithCardAllowed);
        serializer.startTag(EMPTY, CommonSettings.Entities.EXTRA_PAYMENT_WITH_CARD_ALLOWED);
        serializer.text(String.valueOf(commonSettings.isExtraPaymentWithCardAllowed()));
        serializer.endTag(EMPTY, CommonSettings.Entities.EXTRA_PAYMENT_WITH_CARD_ALLOWED);

        serializer.comment(CommonSettingsEntities.Comments.decrementTripAllowed);
        serializer.startTag(EMPTY, CommonSettings.Entities.DECREMENT_TRIP_ALLOWED);
        serializer.text(String.valueOf(commonSettings.isDecrementTripAllowed()));
        serializer.endTag(EMPTY, CommonSettings.Entities.DECREMENT_TRIP_ALLOWED);

        serializer.comment(CommonSettingsEntities.Comments.extraSaleForPdWithExemptionAllowed);
        serializer.startTag(EMPTY, CommonSettings.Entities.EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED);
        serializer.text(String.valueOf(commonSettings.isExtraSaleForPdWithExemptionAllowed()));
        serializer.endTag(EMPTY, CommonSettings.Entities.EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED);

        serializer.comment(CommonSettingsEntities.Comments.couponValidityTime);
        serializer.startTag(EMPTY, CommonSettings.Entities.COUPON_VALIDITY_TIME);
        serializer.text(String.valueOf(commonSettings.getCouponValidityTime()));
        serializer.endTag(EMPTY, CommonSettings.Entities.COUPON_VALIDITY_TIME);

        serializer.comment(CommonSettingsEntities.Comments.printerSendToOfdCountTrigger);
        serializer.startTag(EMPTY, CommonSettings.Entities.PRINTER_SEND_TO_OFD_COUNT_TRIGGER);
        serializer.text(String.valueOf(commonSettings.getPrinterSendToOfdCountTrigger()));
        serializer.endTag(EMPTY, CommonSettings.Entities.PRINTER_SEND_TO_OFD_COUNT_TRIGGER);

        serializer.comment(CommonSettingsEntities.Comments.printerSendToOfdPeriodTrigger);
        serializer.startTag(EMPTY, CommonSettings.Entities.PRINTER_SEND_TO_OFD_PERIOD_TRIGGER);
        serializer.text(String.valueOf(commonSettings.getPrinterSendToOfdPeriodTrigger()));
        serializer.endTag(EMPTY, CommonSettings.Entities.PRINTER_SEND_TO_OFD_PERIOD_TRIGGER);

        serializer.comment(CommonSettingsEntities.Comments.ticketTapeAttentionLength);
        serializer.startTag(EMPTY, CommonSettings.Entities.TICKET_TAPE_ATTENTION_LENGTH);
        serializer.text(String.valueOf(commonSettings.getTicketTapeAttentionLength()));
        serializer.endTag(EMPTY, CommonSettings.Entities.TICKET_TAPE_ATTENTION_LENGTH);

        serializer.endTag(EMPTY, CommonSettingsEntities.ptkCommonSettings);

        serializer.endDocument();
    }

    public static class CommonSettingsEntities {
        private static final String ptkCommonSettings = "ptkCommonSettings";
        private static final String reportType = "reportType";
        private static final String code = "code";

        public static class Comments {
            public static final String ppReportOpenShift = "Последовательность печати отчетов при открытии смены.";
            public static final String ppReportCloseShift = "Последовательность печати отчетов при закрытии смены.";
            public static final String ppReportCloseMonth = "Последовательность печати отчетов при закрытии месяца.";
            public static final String allowedStationsCodes = "Список станций до которых можно продать билет";
            public static final String pTestPdPrintReq = "Обязательность печати пробного ПД при открытии смены.";
            public static final String pDiscountShiftSheetOpeningShift = "Обязательность печати пробной сменной ведомости при открытии смены.";
            public static final String pDiscountShiftSheetClosingShiftReq = "Обязательность печати льготной сменной ведомости при закрытии смены.";
            public static final String pSheetShiftCloseShiftReq = "Обязательность печати сменной ведомости при закрытии смены.";
            public static final String pSheetBlankingShiftClosingShiftReq = "Обязательность печати ведомости гашения смены при закрытии смены.";
            public static final String pDiscountMonthShiftSheetClosingMonthReq = "Обязательность печати льготной месячной ведомости при закрытии месяца.";
            public static final String pMonthSheetClosingMonthReq = "Обязательность печати месячной ведомости при закрытии месяца.";
            public static final String pSheetBlankingMonthClosingMonthReq = "Обязательность печати ведомости гашения месяца при закрытии месяца.";
            public static final String pBTMonthlySheetClosingMonthReq = "Обязательность печати отчёта месячного отчёта по операциям на БТ.";
            public static final String timeChangesPeriod = "Допустимый период изменения текущего времени кассиром-контролером в минутах.";
            public static final String enableAnnulateAfterTimeOver = "Разрешать аннулирование ПД после истечения времени аннулирования.";
            public static final String termStoragePd = "Срок хранения данных в БД оформленных ПД в месяцах.";
            public static final String durationOfPdNextDay = "Время действия ПД на следующий день в часах.";
            public static final String maxTimeAgoMark = "Максимальное время давности метки в часах.";
            public static final String timeElectronicRegistration = "Окончание электронной регистрации до отправления поезда в часах.";
            public static final String bankCode = "Код банка с которым работает БТ.";
            public static final String carrierName = "Имя перевозчика для печати в заголовке чека";
            public static final String screenOffTimeout = "Таймаут на отключение экрана (в секундах)";
            public static final String posTerminalCheckPeriod = "Период опроса доступности Pos терминала после сбоев в секундах.";
            public static final String autoCloseTime = "Время автозакрытия окна после печати/записи ПД (в секундах)";
            public static final String selectDraftNsi = "Использовать ли тестовые версии НСИ";
            public static final String logFullSQL = "Логирование всех SQL запросов.";
            public static final String autoBlockingTimeout = "Время до автоброкировки в секундах, по умолчанию 30м";
            public static final String autoBlockingEnabled = "Возможность автоматической блокировки ПТК, по умолчанию true";
            public static final String timeZoneOffset = "Смещение Часового пояса от UTC, в миллисекундах (по умолчанию Москва +3ч = 10800000 mc)";
            public static final String ignoreCardValidityPeriod = "Игнорировать срок действия карты (нужно только для тестирования)";
            public static final String extraPaymentWithCardAllowed = "Возможность оформления доплаты по банковской карте";
            public static final String decrementTripAllowed = "Возможность списания поездки";
            public static final String extraSaleForPdWithExemptionAllowed = "Разрешена доплата к разовым льготным, безденежным ПД";
            public static final String couponValidityTime = "Срок действия талона ТППД с момента печати (в часах)";
            public static final String printerSendToOfdCountTrigger = "Количество документов для включения отправки данных в ОФД";
            public static final String printerSendToOfdPeriodTrigger = "Срок хранения документов в ФП для включения отправки данных в ОФД (в часах)";
            public static final String criticalNsiAttentionDialogTimeInterval = "Период времени для вывода предупреждений о закрытии смены из-за критической версии НСИ (в минутах)";
            public static final String ticketTapeAttentionLength = "Минимальная длина билетной ленты (в сантиметрах)";
        }
    }

    private static class CommonSettingsXmlHandler extends DefaultHandler {
        private CommonSettings commonSettings;

        private final StringBuilder currentElementValue = new StringBuilder();
        private List<ReportType> _ppReportOpenShift;
        private List<ReportType> _ppReportCloseShift;
        private List<ReportType> _ppReportCloseMonth;
        private List<Long> _allowedStationsCodes;

        private CommonSettingsXmlHandler() {
            commonSettings = new CommonSettings();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentElementValue.delete(0, currentElementValue.length());

            if (qName.equalsIgnoreCase(CommonSettings.Entities.REPORT_OPEN_SHIFT)) {
                _ppReportOpenShift = new ArrayList<>();
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.REPORT_CLOSE_SHIFT)) {
                _ppReportCloseShift = new ArrayList<>();
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.REPORT_CLOSE_MONTH)) {
                _ppReportCloseMonth = new ArrayList<>();
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.ALLOWED_STATIONS_CODES)) {
                _allowedStationsCodes = new ArrayList<>();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            // Function not always gives back the entire value in a single shot.
            // It may return the value in multiple chunks.
            // So need to be careful in assigning and using the values from characters() method.
            // So the better way to use characters() method is to keep appending all the values
            // to a buffer and use the value in the corresponding end tag section.
            // Also need to make sure that the buffer has to be flushed in the corresponding start element.
            currentElementValue.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String currentElementValue = this.currentElementValue.toString();
            if (qName.equalsIgnoreCase(CommonSettings.Entities.REPORT_OPEN_SHIFT)) {
                commonSettings.setReportOpenShift(_ppReportOpenShift.toArray(new ReportType[_ppReportOpenShift.size()]));
                _ppReportOpenShift = null;
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.REPORT_CLOSE_SHIFT)) {
                commonSettings.setReportCloseShift(_ppReportCloseShift.toArray(new ReportType[_ppReportCloseShift.size()]));
                _ppReportCloseShift = null;
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.REPORT_CLOSE_MONTH)) {
                commonSettings.setReportCloseMonth(_ppReportCloseMonth.toArray(new ReportType[_ppReportCloseMonth.size()]));
                _ppReportCloseMonth = null;
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.ALLOWED_STATIONS_CODES)) {
                commonSettings.setAllowedStationsCodes(CollectionUtils.toPrimitives(_allowedStationsCodes));
                _allowedStationsCodes = null;
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.TEST_PD_PRINT_REQ)) {
                commonSettings.setTestPdPrintReq(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_OPENING_SHIFT)) {
                commonSettings.setDiscountShiftSheetOpeningShift(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ)) {
                commonSettings.setDiscountShiftSheetClosingShiftReq(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.SHEET_SHIFT_CLOSE_SHIFT_REQ)) {
                commonSettings.setSheetShiftCloseShiftReq(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ)) {
                commonSettings.setSheetBlankingShiftClosingShiftReq(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ)) {
                commonSettings.setDiscountMonthShiftSheetClosingMonthReq(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.MONTH_SHEET_CLOSING_MONTH_REQ)) {
                commonSettings.setMonthSheetClosingMonthReq(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ)) {
                commonSettings.setSheetBlankingMonthClosingMonthReq(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.BT_MONTHLY_SHEET_CLOSING_MONTH_REQ)) {
                commonSettings.setBtMonthlySheetClosingMonthReq(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.TIME_CHANGES_PERIOD)) {
                commonSettings.setTimeChangesPeriod(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.ENABLE_ANNULATE_AFTER_TIME_OVER)) {
                commonSettings.setEnableAnnulateAfterTimeOver(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.TERM_STORAGE_PD)) {
                commonSettings.setTermStoragePd(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.DURATION_OF_PD_NEXT_DAY)) {
                commonSettings.setDurationOfPdNextDay(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.MAX_TIME_AGO_MARK)) {
                commonSettings.setMaxTimeAgoMark(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.TIME_ELECTRONIC_REGISTRATION)) {
                commonSettings.setTimeElectronicRegistration(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.BANK_CODE)) {
                commonSettings.setBankCode(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.CARRIER_NAME)) {
                commonSettings.setCarrierName(String.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.SCREEN_OFF_TIMEOUT)) {
                commonSettings.setScreenOffTimeout(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.POS_TERMINAL_CHECK_PERIOD)) {
                commonSettings.setPosTerminalCheckPeriod(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.AUTO_CLOSE_TIME)) {
                commonSettings.setAutoCloseTime(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.SELECT_DRAFT_NSI)) {
                commonSettings.setSelectDraftNsi(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.LOG_FULL_SQL)) {
                commonSettings.setLogFullSQL(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.AUTO_BLOCKING_TIMEOUT)) {
                commonSettings.setAutoBlockingTimeout(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.AUTO_BLOCKING_ENABLED)) {
                commonSettings.setAutoBlockingEnabled(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.TIME_ZONE_OFFSET)) {
                commonSettings.setTimeZoneOffset(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.IGNORE_CARD_VALIDITY_PERIOD)) {
                commonSettings.setIgnoreCardValidityPeriod(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.EXTRA_PAYMENT_WITH_CARD_ALLOWED)) {
                commonSettings.setExtraPaymentWithCardAllowed(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.DECREMENT_TRIP_ALLOWED)) {
                commonSettings.setDecrementTripAllowed(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED)) {
                commonSettings.setExtraSaleForPdWithExemptionAllowed(Boolean.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.COUPON_VALIDITY_TIME)) {
                commonSettings.setCouponValidityTime(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.PRINTER_SEND_TO_OFD_COUNT_TRIGGER)) {
                commonSettings.setPrinterSendToOfdCountTrigger(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.PRINTER_SEND_TO_OFD_PERIOD_TRIGGER)) {
                commonSettings.setPrinterSendToOfdPeriodTrigger(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettings.Entities.TICKET_TAPE_ATTENTION_LENGTH)) {
                commonSettings.setTicketTapeAttentionLength(Integer.valueOf(currentElementValue));
            } else if (qName.equalsIgnoreCase(CommonSettingsEntities.reportType)) {
                ReportType reportType = ReportType.getByVarName(currentElementValue);

                if (reportType == null)
                    throw new IllegalArgumentException("Can't find ReportType with name: " + currentElementValue);
                else if (_ppReportOpenShift != null) {
                    _ppReportOpenShift.add(reportType);
                } else if (_ppReportCloseShift != null) {
                    _ppReportCloseShift.add(reportType);
                } else if (_ppReportCloseMonth != null) {
                    _ppReportCloseMonth.add(reportType);
                }

            } else if (qName.equalsIgnoreCase(CommonSettingsEntities.code)) {
                Long code = Long.valueOf(currentElementValue);
                _allowedStationsCodes.add(code);
            }
        }

    }

}
