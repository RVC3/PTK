package ru.ppr.core.dataCarrier.pd.v21;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePd;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * @author Dmitry Nevolin
 */
public class PdV21Impl extends BasePd implements PdV21 {

    /**
     * Порядковый номер (не фискальный) чека за календарный месяц
     */
    private int orderNumber;
    /**
     * Код услуги по справочнику "Стоимости услуг"
     */
    private long serviceId;
    /**
     * Дата начала действия ПД: количество дней с дня продажи. От 0 (в день продажи) до 31.
     */
    private int startDayOffset;
    /**
     * Тип билета
     */
    @PdWithTicketType.TicketType
    private int ticketType;

    public PdV21Impl() {
        super(PdVersion.V21, PdV21Structure.PD_SIZE);
    }

    @Override
    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public int getStartDayOffset() {
        return startDayOffset;
    }

    public void setStartDayOffset(int startDayOffset) {
        this.startDayOffset = startDayOffset;
    }

    @PdWithTicketType.TicketType
    @Override
    public int getTicketType() {
        return ticketType;
    }

    public void setTicketType(@PdWithTicketType.TicketType int ticketType) {
        this.ticketType = ticketType;
    }

}
