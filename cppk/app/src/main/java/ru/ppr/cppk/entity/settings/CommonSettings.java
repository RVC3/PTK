package ru.ppr.cppk.entity.settings;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.Arrays;

import ru.ppr.cppk.utils.ArrayUtils;
import ru.ppr.logger.Logger;

/**
 * Общие настройки ПТК. Должны меняться только при обновлении ПО ПТК.
 * Синхронизируются при подключении к АРМ.
 *
 * @author Григорий Кашка
 */
public class CommonSettings extends Settings {

    private static final String TAG = Logger.makeLogTag(CommonSettings.class);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommonSettings that = (CommonSettings) o;

        if (isTestPdPrintReq() != that.isTestPdPrintReq()) return false;
        if (isDiscountShiftSheetOpeningShift() != that.isDiscountShiftSheetOpeningShift())
            return false;
        if (isDiscountShiftSheetClosingShiftReq() != that.isDiscountShiftSheetClosingShiftReq())
            return false;
        if (isSheetShiftCloseShiftReq() != that.isSheetShiftCloseShiftReq()) return false;
        if (isSheetBlankingShiftClosingShiftReq() != that.isSheetBlankingShiftClosingShiftReq())
            return false;
        if (isDiscountMonthShiftSheetClosingMonthReq() != that.isDiscountMonthShiftSheetClosingMonthReq())
            return false;
        if (isMonthSheetClosingMonthReq() != that.isMonthSheetClosingMonthReq()) return false;
        if (isSheetBlankingMonthClosingMonthReq() != that.isSheetBlankingMonthClosingMonthReq())
            return false;
        if (isBtMonthlySheetClosingMonthReq() != that.isBtMonthlySheetClosingMonthReq())
            return false;
        if (getTimeChangesPeriod() != that.getTimeChangesPeriod()) return false;
        if (isEnableAnnulateAfterTimeOver() != that.isEnableAnnulateAfterTimeOver()) return false;
        if (getTermStoragePd() != that.getTermStoragePd()) return false;
        if (getDurationOfPdNextDay() != that.getDurationOfPdNextDay()) return false;
        if (getMaxTimeAgoMark() != that.getMaxTimeAgoMark()) return false;
        if (getTimeElectronicRegistration() != that.getTimeElectronicRegistration()) return false;
        if (getBankCode() != that.getBankCode()) return false;
        if (getScreenOffTimeout() != that.getScreenOffTimeout()) return false;
        if (getPosTerminalCheckPeriod() != that.getPosTerminalCheckPeriod()) return false;
        if (getAutoCloseTime() != that.getAutoCloseTime()) return false;
        if (isSelectDraftNsi() != that.isSelectDraftNsi()) return false;
        if (isLogFullSQL() != that.isLogFullSQL()) return false;
        if (getAutoBlockingTimeout() != that.getAutoBlockingTimeout()) return false;
        if (isAutoBlockingEnabled() != that.isAutoBlockingEnabled()) return false;
        if (!Arrays.equals(getReportOpenShift(), that.getReportOpenShift())) return false;
        if (!Arrays.equals(getReportCloseShift(), that.getReportCloseShift())) return false;
        if (!Arrays.equals(getReportCloseMonth(), that.getReportCloseMonth())) return false;
        if (!getCarrierName().equals(that.getCarrierName())) return false;
        if (getTimeZoneOffset() != that.getTimeZoneOffset()) return false;
        if (isIgnoreCardValidityPeriod() != that.isIgnoreCardValidityPeriod()) return false;
        if (getPrinterSendToOfdCountTrigger() != that.getPrinterSendToOfdCountTrigger())
            return false;
        if (getPrinterSendToOfdPeriodTrigger() != that.getPrinterSendToOfdPeriodTrigger())
            return false;
        if (getTicketTapeAttentionLength() != that.getTicketTapeAttentionLength()) return false;
        return Arrays.equals(getAllowedStationsCodes(), that.getAllowedStationsCodes());

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(getReportOpenShift());
        result = 31 * result + Arrays.hashCode(getReportCloseShift());
        result = 31 * result + Arrays.hashCode(getReportCloseMonth());
        result = 31 * result + (isTestPdPrintReq() ? 1 : 0);
        result = 31 * result + (isDiscountShiftSheetOpeningShift() ? 1 : 0);
        result = 31 * result + (isDiscountShiftSheetClosingShiftReq() ? 1 : 0);
        result = 31 * result + (isSheetShiftCloseShiftReq() ? 1 : 0);
        result = 31 * result + (isSheetBlankingShiftClosingShiftReq() ? 1 : 0);
        result = 31 * result + (isDiscountMonthShiftSheetClosingMonthReq() ? 1 : 0);
        result = 31 * result + (isMonthSheetClosingMonthReq() ? 1 : 0);
        result = 31 * result + (isSheetBlankingMonthClosingMonthReq() ? 1 : 0);
        result = 31 * result + (isBtMonthlySheetClosingMonthReq() ? 1 : 0);
        result = 31 * result + getTimeChangesPeriod();
        result = 31 * result + (isEnableAnnulateAfterTimeOver() ? 1 : 0);
        result = 31 * result + getTermStoragePd();
        result = 31 * result + getDurationOfPdNextDay();
        result = 31 * result + getMaxTimeAgoMark();
        result = 31 * result + getTimeElectronicRegistration();
        result = 31 * result + getBankCode();
        result = 31 * result + (getCarrierName().hashCode());
        result = 31 * result + Arrays.hashCode(getAllowedStationsCodes());
        result = 31 * result + getScreenOffTimeout();
        result = 31 * result + getPosTerminalCheckPeriod();
        result = 31 * result + getAutoCloseTime();
        result = 31 * result + (isSelectDraftNsi() ? 1 : 0);
        result = 31 * result + (isLogFullSQL() ? 1 : 0);
        result = 31 * result + getAutoBlockingTimeout();
        result = 31 * result + (isAutoBlockingEnabled() ? 1 : 0);
        result = 31 * result + (getTimeZoneOffset());
        result = 31 * result + (isIgnoreCardValidityPeriod() ? 1 : 0);
        result = 31 * result + (getPrinterSendToOfdCountTrigger());
        result = 31 * result + (getPrinterSendToOfdPeriodTrigger());
        result = 31 * result + (getTicketTapeAttentionLength());
        return result;
    }

    /**
     * Последовательность печати отчетов при открытии смены 1.Пробный ПД
     * 2.Пробная сменная ведомость
     */
    @NonNull
    public ReportType[] getReportOpenShift() {
        return getReportTypes(Entities.REPORT_OPEN_SHIFT, Default.REPORT_OPEN_SHIFT);
    }

    // В будущем: Set default value to correct json report type array instead UNKNOWN.
    public void setReportOpenShift(@NonNull final ReportType[] reportOpenShift) {
        getSettings().put(Entities.REPORT_OPEN_SHIFT, getReportTypesAsJson(reportOpenShift, "UNKNOWN"));
    }

    /**
     * Последовательность печати отчетов при закрытии смены 1. Льготная сменная
     * ведомость 2. Ведомость по ЭТТ 3. Сменная ведомость 4. Ведомость гашения
     * смены п.4.2.12
     */
    @NonNull
    public ReportType[] getReportCloseShift() {
        return getReportTypes(Entities.REPORT_CLOSE_SHIFT, Default.REPORT_CLOSE_SHIFT);
    }

    public void setReportCloseShift(@NonNull final ReportType[] reportCloseShift) {
        getSettings().put(Entities.REPORT_CLOSE_SHIFT, getReportTypesAsJson(reportCloseShift, "UNKNOWN"));
    }

    /**
     * Последовательность печати отчетов при закрытии месяца 1. Льготная
     * месячная ведомость 2. Месячная ведомость 3. Ведомость гашения месяца
     * п.4.2.13
     */
    @NonNull
    public ReportType[] getReportCloseMonth() {
        return getReportTypes(Entities.REPORT_CLOSE_MONTH, Default.REPORT_CLOSE_MONTH);
    }

    public void setReportCloseMonth(@NonNull final ReportType[] reportCloseMonth) {
        getSettings().put(Entities.REPORT_CLOSE_MONTH, getReportTypesAsJson(reportCloseMonth, "UNKNOWN"));
    }

    /**
     * Обязательность печати пробного ПД при открытии смены Да п.4.2.1
     */
    public boolean isTestPdPrintReq() {
        return getBoolean(Entities.TEST_PD_PRINT_REQ, Default.TEST_PD_PRINT_REQ);
    }

    public void setTestPdPrintReq(boolean testPdPrintReq) {
        getSettings().put(Entities.TEST_PD_PRINT_REQ, String.valueOf(testPdPrintReq));
    }

    /**
     * Обязательность печати пробной сменной ведомости при открытии смены Нет
     * п.4.2.1
     */
    public boolean isDiscountShiftSheetOpeningShift() {
        return getBoolean(Entities.DISCOUNT_SHIFT_SHEET_OPENING_SHIFT, Default.DISCOUNT_SHIFT_SHEET_OPENING_SHIFT);
    }

    public void setDiscountShiftSheetOpeningShift(boolean discountShiftSheetOpeningShift) {
        getSettings().put(Entities.DISCOUNT_SHIFT_SHEET_OPENING_SHIFT, String.valueOf(discountShiftSheetOpeningShift));
    }

    /**
     * Обязательность печати Льготной сменной ведомости при закрытии смены Да
     * п.4.2.12
     */
    public boolean isDiscountShiftSheetClosingShiftReq() {
        return getBoolean(Entities.DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ, Default.DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ);
    }

    public void setDiscountShiftSheetClosingShiftReq(boolean discountShiftSheetClosingShiftReq) {
        getSettings().put(Entities.DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ, String.valueOf(discountShiftSheetClosingShiftReq));
    }

    /**
     * Обязательность печати Сменной ведомости при закрытии смены Да п.4.2.12
     */
    public boolean isSheetShiftCloseShiftReq() {
        return getBoolean(Entities.SHEET_SHIFT_CLOSE_SHIFT_REQ, Default.SHEET_SHIFT_CLOSE_SHIFT_REQ);
    }

    public void setSheetShiftCloseShiftReq(boolean sheetShiftCloseShiftReq) {
        getSettings().put(Entities.SHEET_SHIFT_CLOSE_SHIFT_REQ, String.valueOf(sheetShiftCloseShiftReq));
    }

    /**
     * Обязательность печати Ведомости гашения смены при закрытии смены Да
     * п.4.2.12
     */
    public boolean isSheetBlankingShiftClosingShiftReq() {
        return getBoolean(Entities.SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ, Default.SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ);
    }

    public void setSheetBlankingShiftClosingShiftReq(boolean sheetBlankingShiftClosingShiftReq) {
        getSettings().put(Entities.SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ, String.valueOf(sheetBlankingShiftClosingShiftReq));
    }

    /**
     * Обязательность печати Льготной месячной ведомости при закрытии месяца Да
     * п.4.2.13
     */
    public boolean isDiscountMonthShiftSheetClosingMonthReq() {
        return getBoolean(Entities.DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ, Default.DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ);
    }

    public void setDiscountMonthShiftSheetClosingMonthReq(boolean discountMonthShiftSheetClosingMonthReq) {
        getSettings().put(Entities.DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ, String.valueOf(discountMonthShiftSheetClosingMonthReq));
    }

    /**
     * Обязательность печати Месячной ведомости при закрытии месяца Да п.4.2.13
     */
    public boolean isMonthSheetClosingMonthReq() {
        return getBoolean(Entities.MONTH_SHEET_CLOSING_MONTH_REQ, Default.MONTH_SHEET_CLOSING_MONTH_REQ);
    }

    public void setMonthSheetClosingMonthReq(boolean monthSheetClosingMonthReq) {
        getSettings().put(Entities.MONTH_SHEET_CLOSING_MONTH_REQ, String.valueOf(monthSheetClosingMonthReq));
    }

    /**
     * Обязательность печати Ведомости гашения месяца при закрытии месяца Да
     * п.4.2.13
     */
    public boolean isSheetBlankingMonthClosingMonthReq() {
        return getBoolean(Entities.SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ, Default.SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ);
    }

    public void setSheetBlankingMonthClosingMonthReq(boolean sheetBlankingMonthClosingMonthReq) {
        getSettings().put(Entities.SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ, String.valueOf(sheetBlankingMonthClosingMonthReq));
    }

    /**
     * Обязательность печати отчёта месячного отчёта по операциям на БТ : Да
     * п.4.2.13
     */
    public boolean isBtMonthlySheetClosingMonthReq() {
        return getBoolean(Entities.BT_MONTHLY_SHEET_CLOSING_MONTH_REQ, Default.BT_MONTHLY_SHEET_CLOSING_MONTH_REQ);
    }

    public void setBtMonthlySheetClosingMonthReq(boolean btMonthlySheetClosingMonthReq) {
        getSettings().put(Entities.BT_MONTHLY_SHEET_CLOSING_MONTH_REQ, String.valueOf(btMonthlySheetClosingMonthReq));
    }

    /**
     * Допустимый период изменения текущего времени кассиром-контролером 5 минут
     * п.4.2.1 в минутах
     */
    public int getTimeChangesPeriod() {
        return getInt(Entities.TIME_CHANGES_PERIOD, Default.TIME_CHANGES_PERIOD);
    }

    public void setTimeChangesPeriod(int timeChangesPeriod) {
        getSettings().put(Entities.TIME_CHANGES_PERIOD, String.valueOf(timeChangesPeriod));
    }

    /**
     * Разрешать аннулирование ПД после истечения времени аннулирования Да
     * п.4.2.6
     */
    public boolean isEnableAnnulateAfterTimeOver() {
        return getBoolean(Entities.ENABLE_ANNULATE_AFTER_TIME_OVER, Default.ENABLE_ANNULATE_AFTER_TIME_OVER);
    }

    public void setEnableAnnulateAfterTimeOver(boolean enableAnnulateAfterTimeOver) {
        getSettings().put(Entities.ENABLE_ANNULATE_AFTER_TIME_OVER, String.valueOf(enableAnnulateAfterTimeOver));
    }

    /**
     * Срок хранения данных в БД оформленных ПД 13 мес п.4.2.23
     */
    public int getTermStoragePd() {
        return getInt(Entities.TERM_STORAGE_PD, Default.TERM_STORAGE_PD);
    }

    public void setTermStoragePd(int termStoragePd) {
        getSettings().put(Entities.TERM_STORAGE_PD, String.valueOf(termStoragePd));
    }

    /**
     * Время действия ПД на следующий день 2 часа п.4.2.10.3.1.2 (в часах)
     */
    public int getDurationOfPdNextDay() {
        return getInt(Entities.DURATION_OF_PD_NEXT_DAY, Default.DURATION_OF_PD_NEXT_DAY);
    }

    public void setDurationOfPdNextDay(int durationOfPdNextDay) {
        getSettings().put(Entities.DURATION_OF_PD_NEXT_DAY, String.valueOf(durationOfPdNextDay));
    }

    /**
     * Максимальное время давности метки 4 часа п.4.2.10.3.1.3 (в часах)
     */
    public int getMaxTimeAgoMark() {
        return getInt(Entities.MAX_TIME_AGO_MARK, Default.MAX_TIME_AGO_MARK);
    }

    public void setMaxTimeAgoMark(int maxTimeAgoMark) {
        getSettings().put(Entities.MAX_TIME_AGO_MARK, String.valueOf(maxTimeAgoMark));
    }

    /**
     * Окончание электронной регистрации до отправления поезда 1 час
     * п.4.2.10.3.6 (в часах)
     */
    public int getTimeElectronicRegistration() {
        return getInt(Entities.TIME_ELECTRONIC_REGISTRATION, Default.TIME_ELECTRONIC_REGISTRATION);
    }

    public void setTimeElectronicRegistration(int timeElectronicRegistration) {
        getSettings().put(Entities.TIME_ELECTRONIC_REGISTRATION, String.valueOf(timeElectronicRegistration));
    }

    /**
     * Код банка, с которым работает БТ, по умолчанию 1
     */
    public int getBankCode() {
        return getInt(Entities.BANK_CODE, Default.BANK_CODE);
    }

    public void setBankCode(int bankCode) {
        getSettings().put(Entities.BANK_CODE, String.valueOf(bankCode));
    }

    /**
     * Имя перевозчика для печати в заголовке чека
     */
    @NonNull
    public String getCarrierName() {
        return getString(Entities.CARRIER_NAME, Default.CARRIER_NAME);
    }

    public void setCarrierName(String carrierName) {
        getSettings().put(Entities.CARRIER_NAME, carrierName);
    }

    /**
     * Список станций до которых можно продать билет
     */
    public long[] getAllowedStationsCodes() {
        return getLongArray(Entities.ALLOWED_STATIONS_CODES, Default.ALLOWED_STATIONS_CODES);
    }

    public void setAllowedStationsCodes(long[] allowedStationsCodes) {
        getSettings().put(Entities.ALLOWED_STATIONS_CODES, ArrayUtils.concatToString(allowedStationsCodes));
    }

    /**
     * Таймаут на отключение экрана (в секундах)
     */
    public int getScreenOffTimeout() {
        return getInt(Entities.SCREEN_OFF_TIMEOUT, Default.SCREEN_OFF_TIMEOUT);
    }

    public void setScreenOffTimeout(int screenOffTimeout) {
        getSettings().put(Entities.SCREEN_OFF_TIMEOUT, String.valueOf(screenOffTimeout));
    }

    /**
     * Период опроса доступности Pos терминала после сбоев в секундах.
     */
    public int getPosTerminalCheckPeriod() {
        return getInt(Entities.POS_TERMINAL_CHECK_PERIOD, Default.POS_TERMINAL_CHECK_PERIOD);
    }

    public void setPosTerminalCheckPeriod(int posTerminalCheckPeriod) {
        getSettings().put(Entities.POS_TERMINAL_CHECK_PERIOD, String.valueOf(posTerminalCheckPeriod));
    }

    /**
     * Время автозакрытия окна после печати/записи ПД (в секундах).
     */
    public int getAutoCloseTime() {
        return getInt(Entities.AUTO_CLOSE_TIME, Default.AUTO_CLOSE_TIME);
    }

    public void setAutoCloseTime(int autoCloseTime) {
        getSettings().put(Entities.AUTO_CLOSE_TIME, String.valueOf(autoCloseTime));
    }

    /**
     * Использовать ли тестовые версии НСИ
     */
    public boolean isSelectDraftNsi() {
        return getBoolean(Entities.SELECT_DRAFT_NSI, Default.SELECT_DRAFT_NSI);
    }

    public void setSelectDraftNsi(boolean selectDraftNsi) {
        getSettings().put(Entities.SELECT_DRAFT_NSI, String.valueOf(selectDraftNsi));
    }

    /**
     * Логирование всех SQL запросов.
     * https://aj.srvdev.ru/browse/CPPKPP-28410
     */
    public boolean isLogFullSQL() {
        return getBoolean(Entities.LOG_FULL_SQL, Default.LOG_FULL_SQL);
    }

    public void setLogFullSQL(boolean logFullSQL) {
        getSettings().put(Entities.LOG_FULL_SQL, String.valueOf(logFullSQL));
    }

    /**
     * Время до автоблокировки в секундах, по умолчанию 30м
     */
    public int getAutoBlockingTimeout() {
        return getInt(Entities.AUTO_BLOCKING_TIMEOUT, Default.AUTO_BLOCKING_TIMEOUT);
    }

    public void setAutoBlockingTimeout(int autoBlockingTimeout) {
        getSettings().put(Entities.AUTO_BLOCKING_TIMEOUT, String.valueOf(autoBlockingTimeout));
    }

    /**
     * Возможность автоматической блокировки ПТК, по умолчанию true
     */
    public boolean isAutoBlockingEnabled() {
        return getBoolean(Entities.AUTO_BLOCKING_ENABLED, Default.AUTO_BLOCKING_ENABLED);
    }

    public void setAutoBlockingEnabled(boolean autoBlockingEnabled) {
        getSettings().put(Entities.AUTO_BLOCKING_ENABLED, String.valueOf(autoBlockingEnabled));
    }

    /**
     * Смещение timezone от UTC, в миллисекундах (по умолчанию москва +3ч = 10800000 mc)
     */
    public int getTimeZoneOffset() {
        return getInt(Entities.TIME_ZONE_OFFSET, Default.TIME_ZONE_OFFSET);
    }

    public void setTimeZoneOffset(int timeZoneOffset) {
        getSettings().put(Entities.TIME_ZONE_OFFSET, String.valueOf(timeZoneOffset));
    }

    /**
     * Игнорировать срок действия карты (нужно только для тестирования), по умолчанию false
     */
    public boolean isIgnoreCardValidityPeriod() {
        return getBoolean(Entities.IGNORE_CARD_VALIDITY_PERIOD, Default.IGNORE_CARD_VALIDITY_PERIOD);
    }

    /**
     * Задать возможность игнорирования срока действия карты
     *
     * @param ignoreCardValidityPeriod
     */
    public void setIgnoreCardValidityPeriod(boolean ignoreCardValidityPeriod) {
        getSettings().put(Entities.IGNORE_CARD_VALIDITY_PERIOD, String.valueOf(ignoreCardValidityPeriod));
    }

    /**
     * Возвращает возможность оформления доплаты по банковской карте, по умолчанию false
     */
    public boolean isExtraPaymentWithCardAllowed() {
        return getBoolean(Entities.EXTRA_PAYMENT_WITH_CARD_ALLOWED, Default.EXTRA_PAYMENT_WITH_CARD_ALLOWED);
    }

    /**
     * Устанавливает возможность оформления доплаты по банковской карте
     *
     * @param extraPaymentWithCardAllowed
     */
    public void setExtraPaymentWithCardAllowed(boolean extraPaymentWithCardAllowed) {
        getSettings().put(Entities.EXTRA_PAYMENT_WITH_CARD_ALLOWED, String.valueOf(extraPaymentWithCardAllowed));
    }

    /**
     * Возвращает возможность списания поездки, по умолчанию true
     */
    public boolean isDecrementTripAllowed() {
        return getBoolean(Entities.DECREMENT_TRIP_ALLOWED, Default.DECREMENT_TRIP_ALLOWED);
    }

    /**
     * Устанавливает возможность списания поездки
     *
     * @param decrementTripAllowed
     */
    public void setDecrementTripAllowed(boolean decrementTripAllowed) {
        getSettings().put(Entities.DECREMENT_TRIP_ALLOWED, String.valueOf(decrementTripAllowed));
    }

    /**
     * Возвращает возможность оформления доплаты к разовым льготным, безденежным ПД, по умолчанию true
     */
    public boolean isExtraSaleForPdWithExemptionAllowed() {
        return getBoolean(Entities.EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED, Default.EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED);
    }

    /**
     * Устанавливает возможность оформления доплаты к разовым льготным, безденежным ПД
     *
     * @param extraSaleForPdWithExemptionAllowed
     */
    public void setExtraSaleForPdWithExemptionAllowed(boolean extraSaleForPdWithExemptionAllowed) {
        getSettings().put(Entities.EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED, String.valueOf(extraSaleForPdWithExemptionAllowed));
    }

    /**
     * Возвращает срок действия талона ТППД с момента печати (в часах)
     */
    public int getCouponValidityTime() {
        return getInt(Entities.COUPON_VALIDITY_TIME, Default.COUPON_VALIDITY_TIME);
    }

    /**
     * Устанавливает срок действия талона ТППД с момента печати (в часах)
     *
     * @param couponValidityTime
     */
    public void setCouponValidityTime(int couponValidityTime) {
        getSettings().put(Entities.COUPON_VALIDITY_TIME, String.valueOf(couponValidityTime));
    }

    /**
     * Возвращает количество документов для включения отправки данных в ОФД.
     */
    public int getPrinterSendToOfdCountTrigger() {
        return getInt(Entities.PRINTER_SEND_TO_OFD_COUNT_TRIGGER, Default.PRINTER_SEND_TO_OFD_COUNT_TRIGGER);
    }

    /**
     * Устанавливает количество документов для включения отправки данных в ОФД.
     */
    public void setPrinterSendToOfdCountTrigger(int countTrigger) {
        getSettings().put(Entities.PRINTER_SEND_TO_OFD_COUNT_TRIGGER, String.valueOf(countTrigger));
    }

    /**
     * Возвращает срок хранения документов в ФП для включения отправки данных в ОФД (в часах).
     */
    public int getPrinterSendToOfdPeriodTrigger() {
        return getInt(Entities.PRINTER_SEND_TO_OFD_PERIOD_TRIGGER, Default.PRINTER_SEND_TO_OFD_PERIOD_TRIGGER);
    }

    /**
     * Устанавливает срок хранения документов в ФП для включения отправки данных в ОФД (в часах).
     */
    public void setPrinterSendToOfdPeriodTrigger(int periodTrigger) {
        getSettings().put(Entities.PRINTER_SEND_TO_OFD_PERIOD_TRIGGER, String.valueOf(periodTrigger));
    }

    /**
     * Возвращает минимальную длину билетной ленты, см
     */
    public int getTicketTapeAttentionLength() {
        return getInt(Entities.TICKET_TAPE_ATTENTION_LENGTH, Default.TICKET_TAPE_ATTENTION_LENGTH);
    }

    /**
     * Устанавливает минимальную длину билетной ленты, см
     */
    public void setTicketTapeAttentionLength(int ticketTapeAttentionLength) {
        getSettings().put(Entities.TICKET_TAPE_ATTENTION_LENGTH, String.valueOf(ticketTapeAttentionLength));
    }

    /**
     * Метод для получения {@link ReportType} из json строки.
     * В случае, когда данного ключа отсутствует значение или при возникновении ошибки
     * будет возвращено передаваемое значение по умолчанию.
     *
     * @param parameter          параметр настройки.
     * @param defaultReportTypes значение типов отчетов по умолчанию.
     * @return типы отчетов.
     */
    @NonNull
    private ReportType[] getReportTypes(@NonNull final String parameter, @NonNull final ReportType[] defaultReportTypes) {
        ReportType[] reportTypes = defaultReportTypes;
        final String value = getSettings().get(parameter);

        if (!TextUtils.isEmpty(value)) {
            try {
                reportTypes = new Gson().fromJson(value, ReportType[].class);
            } catch (Exception ex) {
                Logger.error(TAG, ex);
            }
        }

        return reportTypes;
    }

    /**
     * Метод для получения json строки из {@link ReportType}.
     * В случае, когда данного ключа отсутствует значение или при возникновении ошибки
     * будет возвращено передаваемое значение по умолчанию.
     *
     * @param reportTypes        типы отчетов.
     * @param defaultReportTypes значение типов отчетов по умолчанию.
     * @return json строка содержащая типы отчетов.
     */
    @NonNull
    private static String getReportTypesAsJson(@NonNull final ReportType[] reportTypes, @NonNull final String defaultReportTypes) {
        String json = defaultReportTypes;

        try {
            json = new Gson().toJson(reportTypes);
        } catch (Exception ex) {
            Logger.error(TAG, ex);
        }

        return json;
    }

    public final static class Entities {
        public static final String REPORT_OPEN_SHIFT = "reportOpenShift";
        public static final String REPORT_CLOSE_SHIFT = "reportCloseShift";
        public static final String REPORT_CLOSE_MONTH = "reportCloseMonth";
        public static final String TEST_PD_PRINT_REQ = "testPdPrintReq";
        public static final String DISCOUNT_SHIFT_SHEET_OPENING_SHIFT = "discountShiftSheetOpeningShift";
        public static final String DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ = "discountShiftSheetClosingShiftReq";
        public static final String SHEET_SHIFT_CLOSE_SHIFT_REQ = "sheetShiftCloseShiftReq";
        public static final String SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ = "sheetBlankingShiftClosingShiftReq";
        public static final String DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ = "discountMonthShiftSheetClosingMonthReq";
        public static final String MONTH_SHEET_CLOSING_MONTH_REQ = "monthSheetClosingMonthReq";
        public static final String SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ = "sheetBlankingMonthClosingMonthReq";
        public static final String BT_MONTHLY_SHEET_CLOSING_MONTH_REQ = "btMonthlySheetClosingMonthReq";
        public static final String TIME_CHANGES_PERIOD = "timeChangesPeriod";
        public static final String ENABLE_ANNULATE_AFTER_TIME_OVER = "enableAnnulateAfterTimeOver";
        public static final String TERM_STORAGE_PD = "termStoragePd";
        public static final String DURATION_OF_PD_NEXT_DAY = "durationOfPdNextDay";
        public static final String MAX_TIME_AGO_MARK = "maxTimeAgoMark";
        public static final String TIME_ELECTRONIC_REGISTRATION = "timeElectronicRegistration";
        public static final String BANK_CODE = "bankCode";
        public static final String CARRIER_NAME = "carrierName";
        public static final String ALLOWED_STATIONS_CODES = "allowedStationsCodes";
        public static final String SCREEN_OFF_TIMEOUT = "screenOffTimeout";
        public static final String POS_TERMINAL_CHECK_PERIOD = "posTerminalCheckPeriod";
        public static final String AUTO_CLOSE_TIME = "autoCloseTime";
        public static final String SELECT_DRAFT_NSI = "selectDraftNsi";
        public static final String LOG_FULL_SQL = "logFullSQL";
        public static final String AUTO_BLOCKING_TIMEOUT = "autoBlockingTimeout";
        public static final String AUTO_BLOCKING_ENABLED = "autoBlockingEnabled";
        public static final String TIME_ZONE_OFFSET = "timeZoneOffset";
        public static final String IGNORE_CARD_VALIDITY_PERIOD = "ignoreCardValidityPeriod";
        public static final String EXTRA_PAYMENT_WITH_CARD_ALLOWED = "extraPaymentWithCardAllowed";
        public static final String DECREMENT_TRIP_ALLOWED = "decrementTripAllowed";
        public static final String EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED = "extraSaleForPdWithExemptionAllowed";
        public static final String COUPON_VALIDITY_TIME = "couponValidityTime";
        public static final String PRINTER_SEND_TO_OFD_COUNT_TRIGGER = "printerSendToOfdCountTrigger";
        public static final String PRINTER_SEND_TO_OFD_PERIOD_TRIGGER = "printerSendToOfdPeriodTrigger";
        public static final String TICKET_TAPE_ATTENTION_LENGTH = "ticketTapeAttentionLength";
    }

    /**
     * Класс содержит значения по умолчанию для общих настроек.
     *
     * @see CommonSettings
     */
    private static abstract class Default {
        private static final ReportType[] REPORT_OPEN_SHIFT = {ReportType.TestPd, ReportType.TestShiftShit};

        // Наталья Рязанкина: "Отчет по ЭТТ на ПТК не печатается"
        private static final ReportType[] REPORT_CLOSE_SHIFT = {ReportType.DiscountedShiftShit, /* ReportType.EttShit,*/ ReportType.ShiftShit, ReportType.SheetShiftBlanking};

        private static final ReportType[] REPORT_CLOSE_MONTH = {ReportType.DiscountedMonthlySheet, ReportType.MonthlySheet, ReportType.SheetBlankingMonth, ReportType.BTMonthlySheet};

        private static final boolean TEST_PD_PRINT_REQ = true;

        private static final boolean DISCOUNT_SHIFT_SHEET_OPENING_SHIFT = false;

        private static final boolean DISCOUNT_SHIFT_SHEET_CLOSING_SHIFT_REQ = true;

        /**
         * Обязательность печати Ведомости по ЭТТ при закрытии смены Нет п.4.2.12
         */
        // пропускаем Наталья Рязанкина сказала не нужен

        private static final boolean SHEET_SHIFT_CLOSE_SHIFT_REQ = true;

        private static final boolean SHEET_BLANKING_SHIFT_CLOSING_SHIFT_REQ = true;

        private static final boolean DISCOUNT_MONTH_SHIFT_SHEET_CLOSING_MONTH_REQ = true;

        private static final boolean MONTH_SHEET_CLOSING_MONTH_REQ = true;

        private static final boolean SHEET_BLANKING_MONTH_CLOSING_MONTH_REQ = true;

        private static final boolean BT_MONTHLY_SHEET_CLOSING_MONTH_REQ = true;

        private static final int TIME_CHANGES_PERIOD = 5;

        private static final boolean ENABLE_ANNULATE_AFTER_TIME_OVER = true;

        // В будущем реализовать очистку локальной БД
        private static final int TERM_STORAGE_PD = 13;

        private static final int DURATION_OF_PD_NEXT_DAY = 2;

        private static final int MAX_TIME_AGO_MARK = 4;

        private static final int TIME_ELECTRONIC_REGISTRATION = 1;

        private static final int BANK_CODE = 1;

        private static final String CARRIER_NAME = "АО \"ЦЕНТРАЛЬНАЯ ППК\"";

        private static final long[] ALLOWED_STATIONS_CODES = null;

        private static final int SCREEN_OFF_TIMEOUT = 300;

        private static final int POS_TERMINAL_CHECK_PERIOD = 5;

        private static final int AUTO_CLOSE_TIME = 15;

        private static final boolean SELECT_DRAFT_NSI = false;

        private static final boolean LOG_FULL_SQL = true;

        private static final int AUTO_BLOCKING_TIMEOUT = 1800;

        private static final boolean AUTO_BLOCKING_ENABLED = true;

        private static final int TIME_ZONE_OFFSET = 10800000;

        private static final boolean IGNORE_CARD_VALIDITY_PERIOD = false;

        private static final boolean EXTRA_PAYMENT_WITH_CARD_ALLOWED = false;

        private static final boolean DECREMENT_TRIP_ALLOWED = true;

        private static final boolean EXTRA_SALE_FOR_PD_WITH_EXEMPTION_ALLOWED = true;

        private static final int COUPON_VALIDITY_TIME = 4;

        private static final int PRINTER_SEND_TO_OFD_COUNT_TRIGGER = 10;

        private static final int PRINTER_SEND_TO_OFD_PERIOD_TRIGGER = 24;

        /**
         * Остаток билетный ленты для вывода предупреждения, в см
         */
        private static final int TICKET_TAPE_ATTENTION_LENGTH = 25;
    }

}
