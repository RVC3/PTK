package ru.ppr.cppk.sync.kpp;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;

/**
 * @author Grigoriy Kashka
 */
public class ServiceTicketControlEvent extends CashRegisterEvent {
    /**
     * Дата и время контроля
     */
    public Date controlDateTime;
    /**
     * Номер ключа ЭЦП
     */
    public long edsKeyNumber;
    /**
     * Код отказа по стоп-листу : 0 — нет отказа; >0 — код стоп-листа
     */
    public int stopListId;
    /**
     * Результат прохода по ПД / контроля ПД
     * Согласно справочнику НСИ «Описание результата прохода по ПД (контроля ПД)»
     */
    public Integer validationResult;
    /**
     * Внешний номер БСК
     */
    public String cardNumber;
    /**
     * Номер кристалла (UID) БСК
     */
    public String cardCristalId;
    /**
     * Тип БСК
     */
    public Integer cardType;
    /**
     * Дата начала действия
     */
    public Date validFromUtc;
    /**
     * Дата окончания действия
     */
    public Date validToUtc;
    /**
     * Вид зоны действия
     */
    public Integer zoneType;
    /**
     * Код зоны действия
     */
    public int zoneValue;
    /**
     * True - разрешён проход и проезд
     * False - разрешён только проход
     */
    public boolean canTravel;
    /**
     * Персонифицированная
     */
    public boolean requirePersonification;
    /**
     * Требовать проверки документов
     */
    public boolean requireCheckDocument;
    /**
     * Порядковый номер СТУ
     */
    public int ticketNumber;
    /**
     * Дата и время инициализации БСК
     */
    public Date ticketWriteDateTime;
    /**
     * Счетчик использования карты
     */
    public int smartCardUsageCount;
    /**
     *  Для ПТК - Результат контроля (валидная/невалидная служебная карта)?
     *  Для турникета - Признак успешного прохода
     */
    public boolean passageSign;
    /**
     * Идентификатор оборудования, которое записало СТУ
     */
    public String ticketDeviceId;

}
