package ru.ppr.ikkm;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.ikkm.model.OfdSettings;

/**
 * Created by Александр on 20.01.2016.
 */
public interface IPrinter {

    int closePage(int rotate) throws PrinterException;

    /**
     * Инициирует подключение к принтеру
     *
     * @throws PrinterException
     */
    ConnectResult connect() throws PrinterException;

    /**
     * Инициирует отключение от принтера
     *
     * @throws PrinterException
     */
    void disconnect() throws PrinterException;

    /**
     * Проверяет, подключен ли принтер (реализация SDK)
     *
     * @throws PrinterException
     */
    boolean checkConnectionWithDriver() throws PrinterException;

    /**
     * Подключение к принтеру (реализация SDK)
     *
     * @throws PrinterException
     */
    void connectWithDriver() throws PrinterException;

    /**
     * Отключение от принетра (реализация SDK)
     *
     * @throws PrinterException
     */
    void disconnectWithDriver() throws PrinterException;

    /**
     * Подготавливает окружение принтера (Например, включает Bluetooth)
     *
     * @throws PrinterException
     */
    void prepareResources() throws PrinterException;

    /**
     * Подготавливает окружение принтера (Например, выключает Bluetooth)
     *
     * @throws PrinterException
     */
    void freeResources() throws PrinterException;

    /**
     * Очищает все ресурсы используемые принтером
     * После вызова данного метода должны быть возможность
     * без прорблем создать новый инстанс принтера и работть с ним
     *
     * @throws PrinterException
     */
    void terminate() throws PrinterException;

    /**
     * Печатает произвольный текст в фискальном документе
     *
     * @param text текст для печати
     * @throws PrinterException
     */
    void printTextInFiscalMode(String text) throws PrinterException;

    /**
     * Печатает произвольный текст в фискальном документе
     *
     * @param text      текст для печати
     * @param textStyle стиль текста
     * @throws PrinterException
     */
    void printTextInFiscalMode(String text, TextStyle textStyle) throws PrinterException;

    /**
     * Печатает текст на ленте (size=Normal)
     *
     * @param text текст для печати
     * @throws PrinterException
     */
    void printTextInNormalMode(String text) throws PrinterException;

    /**
     * Печатает текст на ленте
     *
     * @param text      текст для печати
     * @param textStyle стиль текста
     * @throws PrinterException
     */
    void printTextInNormalMode(String text, TextStyle textStyle) throws PrinterException;

    /**
     * Ожидает, пока очередь команд, отправленных на принтер, будет им успешно обработана
     *
     * @throws PrinterException
     */
    void waitPendingOperations() throws PrinterException;

    /**
     * Открывает смену на принтере
     *
     * @param operatorCode код оператора
     * @param operatorName имя оператора
     * @throws PrinterException
     */
    void openShift(int operatorCode, String operatorName) throws PrinterException;

    /**
     * Устанавливает оператора
     *
     * @param operatorCode код оператора
     * @param operatorName имя оператора
     * @throws PrinterException
     */
    void setCashier(int operatorCode, String operatorName) throws PrinterException;

    /**
     * Проверяет, открыта ли смена
     *
     * @return
     * @throws PrinterException
     */
    boolean isShiftOpened() throws PrinterException;

    /**
     * Возвращает время с принтера
     *
     * @return
     * @throws PrinterException
     */
    Date getDate() throws PrinterException;

    /**
     * Возвращает последний СПНД
     *
     * @return
     * @throws PrinterException
     */
    int getLastSPND() throws PrinterException;

    /**
     * Возвращает время печати последнего чека
     *
     * @return
     * @throws PrinterException
     */
    Date getLastCheckTime() throws PrinterException;

    /**
     * Возвращает номер последней смены
     *
     * @return
     * @throws PrinterException
     */
    int getShiftNum() throws PrinterException;

    /**
     * Печатает Z отчет и закрывает смену
     *
     * @throws PrinterException
     */
    void printZReport() throws PrinterException;

    /**
     * Устанавливает заголовки
     *
     * @param headerLines массив заголовков
     * @throws PrinterException
     */
    void setHeaderLines(List<String> headerLines) throws PrinterException;

    /**
     * Добавляет налоговую ставку
     *
     * @param vatID    ид ставки от 1 до 15
     * @param vatValue значение ставки от 1 – это 0.1% до 999 – это 99.9%.
     * @throws PrinterException
     */
    void setVatValue(int vatID, int vatValue) throws PrinterException;

    /**
     * Возвращает налоговую ставку по индексу
     *
     * @param vatID ид ставки
     * @return
     * @throws PrinterException
     */
    int getVatValue(int vatID) throws PrinterException;

    /**
     * Печатает ШК
     *
     * @param data данные ШК
     * @throws PrinterException
     */
    void printBarcode(byte[] data) throws PrinterException;

    /**
     * Печатает настроечную таблицу
     *
     * @throws PrinterException
     */
    void printAdjustingTable() throws PrinterException;

    /**
     * Начинает формировать фискальный документ(транзакия)
     *
     * @param docType тип документа
     * @throws PrinterException
     */
    void startFiscalDocument(DocType docType) throws PrinterException;

    /**
     * Заканчивает формирование фискального документа(транзакции)
     *
     * @param docType тип документа
     * @throws PrinterException
     */
    void endFiscalDocument(DocType docType) throws PrinterException;

    /**
     * Добавление товарной позации в фискальный документ
     *
     * @param description наименование товара
     * @param amount      стоимость
     * @param vatRate     налоговая ставка
     * @throws PrinterException
     */
    void addItem(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws PrinterException;

    /**
     * Добавление товарной позации в чек возврата
     *
     * @param description наименование товара
     * @param amount      стоимость
     * @param vatRate     налоговая ставка
     * @throws PrinterException
     */
    void addItemRefund(String description, BigDecimal amount, @Nullable BigDecimal vatRate) throws PrinterException;

    /**
     * Добавляет скидку для последней добавленной товарной позиции
     *
     * @param discount  размер скидки, в рублях
     * @param newAmount новая стоимость товара с учетом скидки
     * @param vatRate   налоговая ставка
     * @throws PrinterException
     */
    void addDiscount(BigDecimal discount, BigDecimal newAmount, @Nullable BigDecimal vatRate) throws PrinterException;

    /**
     * Печатает итог в чеке
     *
     * @param total       полная стоимость в чеке
     * @param payment     полученная сумма от покупателя
     * @param paymentType тип оплаты
     * @throws PrinterException
     */
    void printTotal(BigDecimal total, BigDecimal payment, PaymentType paymentType) throws PrinterException;

    /**
     * Возврашает показания одометра
     * (Сколько ленты прошло в мм)
     *
     * @return
     * @throws PrinterException
     */
    long getOdometerValue() throws PrinterException;

    /**
     * Возвращает ИНН
     *
     * @return
     * @throws PrinterException
     */
    String getINN() throws PrinterException;

    /**
     * Возвращает регистрационный номер
     *
     * @return
     * @throws PrinterException
     */
    String getRegNumber() throws PrinterException;

    /**
     * Возвращает номер ЭКЛЗ
     *
     * @return
     * @throws PrinterException
     */
    String getEKLZNumber() throws PrinterException;

    /**
     * Возвращает номер Фискального накопителя
     *
     * @return
     * @throws PrinterException
     */
    String getFNSerial() throws PrinterException;

    /**
     * Возвращает модель принтера
     *
     * @return
     * @throws PrinterException
     */
    String getModel() throws PrinterException;

    /**
     * Возвращает сумму в фискальном регистраторе
     *
     * @return
     * @throws PrinterException
     */
    BigDecimal getCashInFR() throws PrinterException;

    /**
     * Количество оставшихся свободных записей в ФП для смен
     *
     * @return
     * @throws PrinterException
     */
    long getAvailableSpaceForShifts() throws PrinterException;

    /**
     * Получение информации о прошедших сменах
     *
     * @param startNum
     * @param endNum
     * @return
     * @throws PrinterException
     */
    List<ClosedShiftInfo> getShiftsInfo(int startNum, int endNum) throws PrinterException;

    /**
     * Возвращает ширину билетной ленты в символах для указанного стиля текста.
     * Предполагается, что данный метод не требует подключения к принтеру,
     * является очень легким, и его можно вызвать в любой момент из любого потока.
     *
     * @param textStyle Стиль текста
     * @return Ширина билетной ленты в символах
     */
    int getWidthForTextStyle(TextStyle textStyle);

    /**
     * Проверяет, поддерживает ли принтер 54 ФЗ.
     * Предполагается, что данный метод не требует подключения к принтеру,
     * является очень легким, и его можно вызвать в любой момент из любого потока.
     *
     * @return {@code true} если поддерживает, {@code false иначе}
     */
    boolean isFederalLaw54Supported();

    /**
     * Устанавливает номер телефона покупателя для отправки чека по смс.
     *
     * @param phoneNumber Номер телефона
     */
    void setCustomerPhoneNumber(String phoneNumber) throws PrinterException;

    /**
     * Устанавливает e-mail покупателя для отправки чека по электронной почте.
     *
     * @param email e-mail
     */
    void setCustomerEmail(String email) throws PrinterException;

    /**
     * Выводит на печать отчет о непереданных в ОФД документах
     */
    void printNotSentDocsReport() throws PrinterException;

    /**
     * Формирует чек коррекции
     *
     * @param docType Тип документа - приход/расход
     * @param total   Сумма коррекции
     */
    void printCorrectionReceipt(DocType docType, BigDecimal total) throws PrinterException;

    /**
     * Печатает дкбликат последнего чека
     */
    void printDuplicateReceipt() throws PrinterException;

    /**
     * Промотать бумагу
     *
     * @param linesCount - количество строчек
     * @throws PrinterException
     */
    void scrollPaperInNormalMode(int linesCount) throws PrinterException;

    /**
     * Вернет настройки для связи с ОФД
     *
     * @return
     * @throws PrinterException
     */
    OfdSettings getOfdSettings() throws PrinterException;

    /**
     * Установит настройки для связи с ОФД
     *
     * @param ofdSettings
     * @throws PrinterException
     */
    void setOfdSettings(OfdSettings ofdSettings) throws PrinterException;

    /**
     * Вернет состояние по неотправленным в ОФД документам.
     *
     * @return
     * @throws PrinterException
     */
    OfdDocsState getOfdDocsState() throws PrinterException;

    /**
     * Запускает отправку документов в ОФД.
     *
     * @return
     * @throws PrinterException
     */
    void startSendingDocsToOfd() throws PrinterException;


    enum DocType {
        SALE(1), RETURN(2);

        private final int code;

        DocType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static DocType create(int code) {
            DocType[] types = values();
            for (DocType type : types) {
                if (code == type.getCode()) {
                    return type;
                }
            }
            return null;
        }
    }

    enum PaymentType {
        CASH(1), CARD(2);

        private final int code;

        PaymentType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PaymentType create(int code) {
            PaymentType[] methods = values();
            for (PaymentType method : methods) {
                if (code == method.code) {
                    return method;
                }
            }
            return null;
        }
    }

    /**
     * Информация о закрытой смене в ЭКЛЗ
     */
    class ClosedShiftInfo {
        /**
         * Номер смены
         */
        private final int number;
        /**
         * Время закрытия смены
         */
        private final Date closeTime;
        /**
         * Сумма продаж по смене
         */
        private final BigDecimal totalSaleSum;
        /**
         * Сумма покупок по смене
         */
        private final BigDecimal totalBuySum;
        /**
         * Сумма возврата продаж по смене
         */
        private final BigDecimal totalReturnSaleSum;
        /**
         * Сумма возврата покупок по смене
         */
        private final BigDecimal totalReturnBuySum;

        public ClosedShiftInfo(int number, Date closeTime, BigDecimal totalSaleSum, BigDecimal totalBuySum, BigDecimal totalReturnSaleSum, BigDecimal totalReturnBuySum) {
            this.number = number;
            this.closeTime = closeTime;
            this.totalSaleSum = totalSaleSum;
            this.totalBuySum = totalBuySum;
            this.totalReturnSaleSum = totalReturnSaleSum;
            this.totalReturnBuySum = totalReturnBuySum;
        }

        public int getNumber() {
            return number;
        }

        public Date getCloseTime() {
            return closeTime;
        }

        public BigDecimal getTotalSaleSum() {
            return totalSaleSum;
        }

        public BigDecimal getTotalBuySum() {
            return totalBuySum;
        }

        public BigDecimal getTotalReturnSaleSum() {
            return totalReturnSaleSum;
        }

        public BigDecimal getTotalReturnBuySum() {
            return totalReturnBuySum;
        }
    }

    enum ConnectResult {
        ALREADY_CONNECTED,
        NOW_CONNECTED,
        NOT_CONNECTED
    }

}
