package ru.ppr.cppk.sync.kpp;

import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.sync.kpp.baseEntities.TicketEventBase;

/**
 * @author Grigoriy Kashka
 */
public class CPPKTicketControl extends TicketEventBase {

    /**
     * timestamp события контроля билета, в милисекундах. Это текущее время ПТК в момент контроля.
     * Никакого отновения к меткам прохода не имеет.
     */
    public Date ControlDateTime;

    /**
     * номер ключа ЭЦП
     */
    public long EdsKeyNumber;

    /**
     * признак отзыва ЭЦП
     */
    public boolean IsRevokedEds;

    /**
     * код отказа по стоп-листу : 0 — нет отказа; >0 — код стоп-листа
     */
    public int StopListId;

    /**
     * 4-значный код льготы
     */
    public int ExemptionCode;

    /**
     * Причина отказа
     * enum PassageResult
     */
    public Integer ValidationResult;

    /**
     * Кол-во поездок, которое списалось при проходе
     */
    public int TripsSpend;

    /**
     * Кол-во поездок на 7000-х поездах по абонементу на кол-во поездок, которое списалось при контроле
     */
    public Integer trips7000Spend;

    /**
     * Количество оставшихся поездок по абонементу на кол-во поездок
     */
    public Integer tripsCount;

    /**
     * Количество оставшихся поездок на 7000-х поездах по абонементу на кол-во поездок
     */
    public Integer trips7000Count;

    /**
     * Номер устройства, продавшего билет
     */
    public String SellTicketDeviceId;

    /**
     * Восстановленный билет
     */
    public boolean isRestoredTicket;

    /**
     * Пункт отправления автобуса (для трансфера).
     * Соответствует значению «Код Экспресс» в справочнике НСИ «Станции»
     */
    public Long DeparturePoint;

    /**
     * Пункт назначения автобуса (для трансфера).
     * Соответствует значению «Код Экспресс» в справочнике НСИ «Станции»
     */
    public Long DestinationPoint;

    /**
     * Дата и время отправления автобуса (для трансфера)
     * в UTC
     */
    @Nullable
    public Date TransferDepartureDateTime;

}
