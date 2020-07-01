package ru.ppr.cppk.entity.settings;

import android.support.annotation.NonNull;

import ru.ppr.cppk.utils.ArrayUtils;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TrainCategoryPrefix;

/**
 * Частные настройки ПТК. Могут изменяться как с помощью интерфейса ПТК так и
 * через АРМ загрузки данных.
 *
 * @author G.Kashka
 */
public class PrivateSettings extends Settings {

    private static final String TAG = Logger.makeLogTag(PrivateSettings.class);

    public PrivateSettings() {
    }

    public PrivateSettings(PrivateSettings privateSettings) {
        this.setTerminalNumber(privateSettings.getTerminalNumber());
        this.setProductionSectionId(privateSettings.getProductionSectionId());
        this.setMobileCashRegister(privateSettings.isMobileCashRegister());
        this.setCurrentStationCode(privateSettings.getCurrentStationCode());
        this.setIsOutputMode(privateSettings.isOutputMode());
        this.setTimeForAnnulate(privateSettings.getTimeForAnnulate());
        this.setTrainCategoryPrefix(privateSettings.getTrainCategoryPrefix());
        this.setStopListValidTime(privateSettings.getStopListValidTime());
        this.setTimeToCloseShiftMessage(privateSettings.getTimeForShiftCloseMessage());
        this.setDayCode(privateSettings.getDayCode());
        this.setTimeSyncEnabled(privateSettings.isTimeSyncEnabled());
        this.setAutoTimeSyncEnabled(privateSettings.isAutoTimeSyncEnabled());
        this.setIsPosEnabled(privateSettings.isPosEnabled());
        this.setIsSaleEnabled(privateSettings.isSaleEnabled());
        this.setIsUseMobileData(privateSettings.isUseMobileDataEnabled());
        this.setSaleStationCode(privateSettings.getSaleStationCode());
        this.setAllowedFineCodes(privateSettings.getAllowedFineCodes());
        this.setTransferControlMode(privateSettings.isTransferControlMode());
        this.setTransferRouteStationsCodes(privateSettings.getTransferRouteStationsCodes());
        this.setIsOutsideProductionSectionSaleEnabled(privateSettings.isOutsideProductionSectionSaleEnabled());
        this.setTransferSaleEnabled(privateSettings.isTransferSaleEnabled());
        this.setPrinterDisconnectTimeout(privateSettings.getPrinterDisconnectTimeout());
    }

    /**
     * Метод для получения номера ПТК.
     */
    public long getTerminalNumber() {
        return getLong(Entities.TERMINAL_NUMBER, Default.TERMINAL_NUMBER);
    }

    /**
     * Метод для установления номера ПТК.
     * Номер ПТК должен устанавливаться один раз только при первичной настройке ПТК.
     * Используется для инициализации SFT в качестве userId.
     * Нормальный номер выглядит примерно так: 96
     *
     * @param terminalNumber номер ПТК.
     */
    public void setTerminalNumber(long terminalNumber) {
        getSettings().put(Entities.TERMINAL_NUMBER, String.valueOf(terminalNumber));
    }

    /**
     * Метод для получения участка обслуживания.
     */
    public int getProductionSectionId() {
        return getInt(Entities.PRODUCTION_SECTION_CODE, Default.PRODUCTION_SECTION_CODE);
    }

    /**
     * Метод для установления участка обслуживания.
     */
    public void setProductionSectionId(int productionSectionId) {
        getSettings().put(Entities.PRODUCTION_SECTION_CODE, String.valueOf(productionSectionId));
    }

    /**
     * Метод позволяющий определить работает ли ПТК в режиме «Мобильной кассы».
     *
     * @return true - ПТК работае в режиме «Мобильной кассы».
     */
    public boolean isMobileCashRegister() {
        return getBoolean(Entities.IS_MOBILE_CASH_REGISTER, Default.IS_MOBILE_CASH_REGISTER);
    }

    /**
     * Метод для установления работы ПТК в режиме «Мобильной кассы».
     *
     * @param isMobileCashRegister true - рабоат в режиме «Мобильной кассы».
     */
    public void setMobileCashRegister(boolean isMobileCashRegister) {
        getSettings().put(Entities.IS_MOBILE_CASH_REGISTER, String.valueOf(isMobileCashRegister));
    }

    /**
     * Метод для получения кода станции.
     *
     * @return код станции.
     */
    public int getCurrentStationCode() {
        return getInt(Entities.WORK_STATION_CODE, Default.WORK_STATION_CODE);
    }

    /**
     * Метод для уставновления кода станции.
     *
     * @param code код станции.
     */
    public void setCurrentStationCode(int code) {
        getSettings().put(Entities.WORK_STATION_CODE, String.valueOf(code));
    }

    /**
     * Метод для определения режима работы «Мобильной кассы» на выход.
     *
     * @return true - мобильная касса работает на выход.
     */
    public boolean isOutputMode() {
        return getBoolean(Entities.IS_OUTPUT_MODE, Default.IS_OUTPUT_MODE);
    }

    /**
     * Метод для установления режима работы «Мобильной кассы».
     *
     * @param isOutputMode true - мобильная касса работает на выход.
     */
    public void setIsOutputMode(boolean isOutputMode) {
        getSettings().put(Entities.IS_OUTPUT_MODE, String.valueOf(isOutputMode));
    }

    /**
     * Метод для получения времени на аннулирование ПД (в минутах).
     *
     * @return время на аннулирование ПД (в минутах).
     */
    public int getTimeForAnnulate() {
        return getInt(Entities.TIME_FOR_ANNULATE, Default.TIME_FOR_ANNULATE);
    }

    /**
     * Метод для установления времени на аннулирование ПД (в минутах).
     *
     * @param minutes время на аннулирование ПД (в минутах).
     */
    public void setTimeForAnnulate(final int minutes) {
        getSettings().put(Entities.TIME_FOR_ANNULATE, String.valueOf(minutes));
    }

    /**
     * Метод для получения категории поезда контроля.
     *
     * @return категория поезда контроля.
     */
    public TrainCategoryPrefix getTrainCategoryPrefix() {
        int code = getInt(Entities.TRAIN_CATEGORY_PREFIX, -1);
        return code == -1 ? Default.TRAIN_CATEGORY_PREFIX : TrainCategoryPrefix.valueOf(code);
    }

    /**
     * Метод для установления категории поезда контроля.
     *
     * @param trainCategoryPrefix категория поезда контроля.
     */
    public void setTrainCategoryPrefix(@NonNull TrainCategoryPrefix trainCategoryPrefix) {
        getSettings().put(Entities.TRAIN_CATEGORY_PREFIX, String.valueOf(trainCategoryPrefix.getCode()));
    }

    /**
     * Метод для получения срока действия версии стоп-листа в днях.
     *
     * @return срок действия версии стоп-листа в днях.
     */
    public int getStopListValidTime() {
        return getInt(Entities.STOP_LIST_VALID_TIME, Default.STOP_LIST_VALID_TIME);
    }

    /**
     * Метод для установления срока действия версии стоп-листа в днях.
     *
     * @param days срок действия версии стоп-листа в днях.
     */
    public void setStopListValidTime(int days) {
        getSettings().put(Entities.STOP_LIST_VALID_TIME, String.valueOf(days));
    }

    /**
     * Метод для получения кода дня.
     *
     * @return код дня.
     */
    public int getDayCode() {
        return getInt(Entities.DAY_CODE, Default.DAY_CODE);
    }

    /**
     * Метод для установления кода дня.
     *
     * @param dayCode код дня.
     */
    public void setDayCode(final int dayCode) {
        getSettings().put(Entities.DAY_CODE, String.valueOf(dayCode));
    }

    /**
     * Метод для проверки влючена ли синхронизации времени.
     *
     * @return результат проверки.
     */
    public boolean isTimeSyncEnabled() {
        return getBoolean(Entities.IS_TIME_SYNC_ENABLED, Default.IS_TIME_SYNC_ENABLED);
    }

    /**
     * Метод для установления синхронизации времени.
     *
     * @param enabled флаг синхронизации времени.
     */
    public void setTimeSyncEnabled(boolean enabled) {
        getSettings().put(Entities.IS_TIME_SYNC_ENABLED, String.valueOf(enabled));
    }

    /**
     * Метод для проверки включено ли автоматическое изменение времени превышающего допустимый период изменения текущего времени.
     *
     * @return результат проверки.
     */
    public boolean isAutoTimeSyncEnabled() {
        return getBoolean(Entities.IS_AUTO_TIME_SYNC_ENABLED, Default.IS_AUTO_TIME_SYNC_ENABLED);
    }

    /**
     * Метод для установления автоматического изменения времени превышающего допустимый период изменения текущего времени.
     *
     * @param enabled флаг автоматического изменения времени превышающего допустимый период изменения текущего времени.
     */
    public void setAutoTimeSyncEnabled(final boolean enabled) {
        getSettings().put(Entities.IS_AUTO_TIME_SYNC_ENABLED, String.valueOf(enabled));
    }

    /**
     * Возвращает период времени для вывода предупреждений о закрытии смены, в минутах
     *
     * @return период времени для вывода предупреждений о закрытии смены, в минутах
     */
    public int getTimeForShiftCloseMessage() {
        return getInt(Entities.TIME_TO_CLOSE_SHIFT_MESSAGE, Default.TIME_TO_CLOSE_SHIFT_MESSAGE);
    }

    /**
     * Задает период времени для вывода предупреждений о закрытии смены, в минутах
     *
     * @param minutes
     */
    public void setTimeToCloseShiftMessage(int minutes) {
        getSettings().put(Entities.TIME_TO_CLOSE_SHIFT_MESSAGE, String.valueOf(minutes));
    }

    /**
     * Метод для определения задействован ли POS терминал.
     *
     * @return true - POS терминал задействован.
     */
    public boolean isPosEnabled() {
        return getBoolean(Entities.IS_POS_ENABLED, Default.IS_POS_ENABLED);
    }

    /**
     * Задать настройку возможности использования POS-терминала
     *
     * @param isPosEnabled
     */
    public void setIsPosEnabled(boolean isPosEnabled) {
        getSettings().put(Entities.IS_POS_ENABLED, String.valueOf(isPosEnabled));
    }

    /**
     * Вернет значение флага переключающего мобильные данные и Wi-Fi
     */
    public boolean isUseMobileDataEnabled() {
        return getBoolean(Entities.IS_USE_MOBILE_DATA, Default.IS_USE_MOBILE_DATA);
    }

    /**
     * Включить возможность использования мобильных данных вместо WiFi
     *
     * @param enable
     * @return
     */
    public void setIsUseMobileData(boolean enable) {
        getSettings().put(Entities.IS_USE_MOBILE_DATA, String.valueOf(enable));
    }

    /**
     * Вернет флаг разрешающий продажу на ПТК
     *
     * @return
     */
    public boolean isSaleEnabled() {
        return getBoolean(Entities.IS_SALE_ENABLED, Default.IS_SALE_ENABLED);
    }

    /**
     * Задает флаг разрешающий продажу на ПТК
     *
     * @param enable
     * @return
     */
    public void setIsSaleEnabled(boolean enable) {
        getSettings().put(Entities.IS_SALE_ENABLED, String.valueOf(enable));
    }

    /**
     * Вернет текущую станцию привязки ПТК
     *
     * @return
     */
    public int getSaleStationCode() {
        return getInt(Entities.SALE_STATION_CODE, Default.SALE_STATION_CODE);
    }

    /**
     * Задает текущую станцию привязки ПТК
     *
     * @param code
     */
    public void setSaleStationCode(int code) {
        getSettings().put(Entities.SALE_STATION_CODE, String.valueOf(code));
    }

    /**
     * Список станций до которых можно продать билет
     */
    public long[] getAllowedFineCodes() {
        return getLongArray(Entities.ALLOWED_FINE_CODES, Default.ALLOWED_FINE_CODES);
    }

    public void setAllowedFineCodes(long[] allowedFineCodes) {
        getSettings().put(Entities.ALLOWED_FINE_CODES, ArrayUtils.concatToString(allowedFineCodes));
    }

    /**
     * режим контроля трансферов (в автобусе)
     */
    public boolean isTransferControlMode() {
        return getBoolean(Entities.IS_TRANSFER_CONTROL_MODE, Default.IS_TRANSFER_CONTROL_MODE);
    }

    public void setTransferControlMode(boolean isTransferControlMode) {
        getSettings().put(Entities.IS_TRANSFER_CONTROL_MODE, String.valueOf(isTransferControlMode));
    }

    /**
     * Пара станций начало и конец маршрута трансфера
     */
    @NonNull
    public long[] getTransferRouteStationsCodes() {
        return getLongArray(Entities.TRANSFER_ROUTE_STATIONS, Default.TRANSFER_ROUTE_STATIONS);
    }

    public void setTransferRouteStationsCodes(@NonNull long[] transferRouteStationsCodes) {
        getSettings().put(Entities.TRANSFER_ROUTE_STATIONS, ArrayUtils.concatToString(transferRouteStationsCodes));
    }

    /**
     * Разрешение на оформление ПД вне привязанного участка
     *
     * @return true, если разрешено
     */
    public boolean isOutsideProductionSectionSaleEnabled() {
        return getBoolean(Entities.IS_OUTSIDE_PRODUCTION_SECTION_SALE_ENABLED, Default.IS_OUTSIDE_PRODUCTION_SECTION_SALE_ENABLED);
    }

    /**
     * Установить разрешение на оформление ПД вне привязанного участка
     *
     * @param isOutsideProductionSectionSaleEnabled
     */
    public void setIsOutsideProductionSectionSaleEnabled(boolean isOutsideProductionSectionSaleEnabled) {
        getSettings().put(Entities.IS_OUTSIDE_PRODUCTION_SECTION_SALE_ENABLED, String.valueOf(isOutsideProductionSectionSaleEnabled));
    }

    /**
     * Разрешение на оформление трансфера
     *
     * @return true, если разрешено
     */
    public boolean isTransferSaleEnabled() {
        return getBoolean(Entities.IS_TRANSFER_SALE_ENABLED, Default.IS_TRANSFER_SALE_ENABLED);
    }

    /**
     * Установить разрешение на оформление трансфера
     *
     * @param isTransferSaleEnabled
     */
    public void setTransferSaleEnabled(boolean isTransferSaleEnabled) {
        getSettings().put(Entities.IS_TRANSFER_SALE_ENABLED, String.valueOf(isTransferSaleEnabled));
    }

    /**
     * Задержка на отключение принтера в секундах
     *
     * @return - количество секунд
     */
    public int getPrinterDisconnectTimeout() {
        return getInt(Entities.PRINTER_DISCONNECT_TIMEOUT, Default.PRINTER_DISCONNECT_TIMEOUT);
    }

    /**
     * Установить задержку на отключение принтера в секундах
     *
     * @param printerDisconnectTimeout
     */
    public void setPrinterDisconnectTimeout(int printerDisconnectTimeout) {
        getSettings().put(Entities.PRINTER_DISCONNECT_TIMEOUT, String.valueOf(printerDisconnectTimeout));
    }

    /**
     * Класс содержит значения по умолчанию для частных настроек.
     *
     * @see PrivateSettings
     */
    public static abstract class Default {
        public static final int TERMINAL_NUMBER = -1;
        public static final TrainCategoryPrefix TRAIN_CATEGORY_PREFIX = TrainCategoryPrefix.PASSENGER;
        public static final int PRODUCTION_SECTION_CODE = -1;
        public static final boolean IS_AUTO_TIME_SYNC_ENABLED = false;
        public static final int TIME_TO_CLOSE_SHIFT_MESSAGE = 5;
        public static final int STOP_LIST_VALID_TIME = 7;
        public static final int WORK_STATION_CODE = -1;
        public static final boolean IS_POS_ENABLED = false;
        public static final int TIME_FOR_ANNULATE = 15;
        public static final boolean IS_OUTPUT_MODE = false;
        public static final boolean IS_MOBILE_CASH_REGISTER = false;
        public static final boolean IS_SALE_ENABLED = false;
        public static final boolean IS_TIME_SYNC_ENABLED = false;
        public static final int DAY_CODE = 0;
        public static final boolean IS_USE_MOBILE_DATA = true;
        public static final int SALE_STATION_CODE = -1;
        public static final long[] ALLOWED_FINE_CODES = null;
        public static final boolean IS_TRANSFER_CONTROL_MODE = false;
        public static final long[] TRANSFER_ROUTE_STATIONS = new long[2];
        public static final boolean IS_OUTSIDE_PRODUCTION_SECTION_SALE_ENABLED = false;
        public static final boolean IS_TRANSFER_SALE_ENABLED = true;
        public static final int PRINTER_DISCONNECT_TIMEOUT = 60;
    }

    /**
     * Названия полей, синхронизированы с кассой
     */
    public final static class Entities {
        public static final String TERMINAL_NUMBER = "TerminalNumber";
        public static final String TRAIN_CATEGORY_PREFIX = "TrainCategoryPrefix";
        public static final String PRODUCTION_SECTION_CODE = "ProductionSectionCode";
        public static final String IS_AUTO_TIME_SYNC_ENABLED = "IsAutoTimeSyncEnabled";
        public static final String TIME_TO_CLOSE_SHIFT_MESSAGE = "TimeToCloseShiftMessage";
        public static final String STOP_LIST_VALID_TIME = "StopListValidTime";
        public static final String WORK_STATION_CODE = "WorkStationCode";
        public static final String IS_POS_ENABLED = "IsPosEnabled";
        public static final String TIME_FOR_ANNULATE = "TimeForAnnulate";
        public static final String IS_OUTPUT_MODE = "IsOutputMode";
        public static final String IS_MOBILE_CASH_REGISTER = "IsMobileCashRegister";
        public static final String IS_SALE_ENABLED = "IsSaleEnabled";
        public static final String IS_TIME_SYNC_ENABLED = "IsTimeSyncEnabled";
        public static final String DAY_CODE = "DayCode";
        public static final String IS_USE_MOBILE_DATA = "IsUseMobileData";
        public static final String SALE_STATION_CODE = "SaleStationCode";
        public static final String ALLOWED_FINE_CODES = "allowedFineCodes";
        public static final String IS_TRANSFER_CONTROL_MODE = "IsTransferControlMode";
        public static final String TRANSFER_ROUTE_STATIONS = "TransferRouteStations";
        public static final String IS_OUTSIDE_PRODUCTION_SECTION_SALE_ENABLED = "IsOutsideProductionSectionSaleEnabled";
        public static final String IS_TRANSFER_SALE_ENABLED = "IsTransferSaleEnabled";
        public static final String PRINTER_DISCONNECT_TIMEOUT = "PrinterDisconnectTimeout";
    }

}
