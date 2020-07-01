package ru.ppr.cppk.entity.event.base34;

import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.localdb.model.TicketWayType;

/**
 * Created by Кашка Григорий on 13.12.2015.
 */
public class TicketEventBase {

    /**
     * Первичный ключ для таблицы
     */
    private long id = -1;
    /**
     * ID сменного события, в рамках которого случилось это событие
     * Первичный ключ для таблицы CashRegisterWorkingShift
     */
    private long shiftEventId;
    /**
     * Время продажи билета.
     * В базу это значение должно сохраняться в секундах
     */
    private Date saleDateTime;
    /**
     * Смещение даты начала действия ПД с момента продажи (в днях)
     */
    private int startDayOffset;
    /**
     * действует с
     */
    private Date validFromDateTime;
    /**
     * действует по
     */
    private Date validTillDateTime;
    /**
     * код тарифа
     */
    private long tariffCode = -1;
    /**
     * Направление действия билета.
     */
    private TicketWayType wayType;
    /**
     * Здесь хранится тип билета, по которому формируется событие. В выгрузке
     * данных в ЦОД данное поле не участвует!!!
     */
    private String ticketTypeShortName;
    /**
     * Тип АСО Экспресс билета (разовый, абонемент, количество поездок и т.д.)
     * Пока не пишем, т.к. данных полей нет в нашей БД. Берется из таблицы
     * ExpressTicketTypes имеет значение например: "G/Т"
     */
    @Nullable
    private String type;
    /**
     * Наш код типа билета, берeтся из таблицы Tariff.TicketTypeCode
     */
    private int typeCode;
    /**
     * ID смарткарты, с которой был прочитан билет (или на которую был записан)
     */
    private long smartCardId = -1;
    /**
     * Тип билета (Разовый, Багаж и т.д.) береться из НСИ
     * В выгрузке НЕ участвует.
     */
    private int ticketCategoryCode = -1;
    /**
     * Код станции отправления
     */
    private long departureStationCode;
    /**
     * Код станции назначения
     */
    private long destinationStationCode;

    /**
     * отметка, что запись будет удалена сборщиком мусора
     */
    private boolean deletedMark = false;

    public long getId() {
        return id;
    }

    public long getTariffCode() {
        return tariffCode;
    }

    public void setTariffCode(long tariffCode) {
        this.tariffCode = tariffCode;
    }

    public TicketWayType getWayType() {
        return wayType;
    }

    public void setWayType(TicketWayType wayType) {
        this.wayType = wayType;
    }

    /**
     * Тип АСО Экспресс билета (разовый, абонемент, количество поездок и т.д.)
     *
     * @return
     */
    @Nullable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return код типа билета, берeтся из таблицы Tariff.TicketTypeCode
     */
    public int getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public long getSmartCardId() {
        return smartCardId;
    }

    public void setSmartCardId(long smartCardId) {
        this.smartCardId = smartCardId;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return тип билета, по которому формируется событие.
     */
    public String getTicketTypeShortName() {
        return ticketTypeShortName;
    }

    public void setTicketTypeShortName(String ticketTypeShortName) {
        this.ticketTypeShortName = ticketTypeShortName;
    }

    /**
     * @return Тип билета (Разовый, Багаж и т.д.) береться из НСИ
     */
    public int getTicketCategoryCode() {
        return ticketCategoryCode;
    }

    public void setTicketCategoryCode(int ticketCategoryCode) {
        this.ticketCategoryCode = ticketCategoryCode;
    }

    public long getShiftEventId() {
        return shiftEventId;
    }

    public void setShiftEventId(long shiftEventId) {
        this.shiftEventId = shiftEventId;
    }

    public long getDepartureStationCode() {
        return departureStationCode;
    }

    public void setDepartureStationCode(long departureStationCode) {
        this.departureStationCode = departureStationCode;
    }

    public long getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(long destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }

    public void setSaletime(Date saledatetime) {
        this.saleDateTime = saledatetime;
    }

    public Date getSaledateTime() {
        return saleDateTime;
    }

    public int getStartDayOffset() {
        return startDayOffset;
    }

    public void setStartDayOffset(int startDayOffset) {
        this.startDayOffset = startDayOffset;
    }

    public void setValidFromDate(Date timestamp) {
        validFromDateTime = timestamp;
    }

    public Date getValidFromDate() {
        return validFromDateTime;
    }

    public void setValidTillDate(Date timestamp) {
        validTillDateTime = timestamp;
    }

    public Date getValidTillDate() {
        return validTillDateTime;
    }

    public void setDeletedMark(boolean value) {
        this.deletedMark = value;
    }

    public boolean getDeletedMark(){
        return deletedMark;
    }
}
