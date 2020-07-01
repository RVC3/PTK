package ru.ppr.cppk.sync.kpp;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.CashRegisterEvent;

/***
 * @author Grigoriy Kashka
 */
public class TicketPaperRollEvent extends CashRegisterEvent {


    /**
     * Статус
     * - Установка
     * - Окончание
     */
    public int action;


    /**
     * Номер первого документа
     */
    public int ticketNumber;


    /**
     * Номер последнего документа
     * null для события установки ленты
     */
    public Integer lastTicketNumber;


    /**
     * Серия бобины
     */
    public String series;


    /**
     * Номер бобины
     */
    public int number;


    /**
     * Дата и время установки или окончания билетной ленты
     */
    public Date operationDateTime;


    /**
     * Количество чеков
     */
    public int totalTicketsCount;


    /**
     * Количество отчетов
     */
    public int totalReportsCount;


    /**
     * Длина ленты (мм)
     */
    public long paperConsumption;


    /**
     * Общая длина ленты (мм) с момента начала работы фискального регистратора
     * Никогда не обнуляется, счетчик с момента начала использования фискального регистратора.
     * [Obsolete("Не используется в ЦОД (на КО может не заполняться). Используется PaperConsumption.")]
     */
    public long deviceLength;

    //region Количество распечатанных чеков

    /**
     * Количество распечатанных пробных ПД
     */
    public int testTicketsCount;


    /**
     * Количество распечатанных ПД
     */
    public int ticketsCount;


    /**
     * Количество распечатанных чеков продажи услуги
     */
    public int serviceSaleReceiptsCount;


    /**
     * Количество распечатанных чеков аннулирования
     */
    public int cancellationReceiptsCount;

    //endregion

    //region Количество отчетов

    /**
     * Количество отчетов Пробная сменная ведомость
     */
    public int testShiftRegisterReportsCount;


    /**
     * Количество отчетов Сменная ведомость
     */
    public int shiftRegisterReportsCount;


    /**
     * Количество отчетов Журнал оформления по ЭТТ
     */
    public int ettRegisterReportsCount;


    /**
     * Количество отчетов Льготная сменная ведомость
     */
    public int exemptionShiftRegisterReportsCount;


    /**
     * Количество отчетов Пробная месячная ведомость
     */
    public int testMonthRegisterReportsCount;


    /**
     * Количество отчетов Месячная ведомость
     */
    public int monthRegisterReportsCount;


    /**
     * Количество отчетов Льготная месячная ведомость
     */
    public int exemptionMonthRegisterReportsCount;


    /**
     * Количество отчетов Контрольный журнал
     */
    public int controlRegisterReportsCount;

    //endregion


    /**
     * Был ли сброс счётчика билетной ленты
     */
    public boolean paperCounterHasRestarted;

}
