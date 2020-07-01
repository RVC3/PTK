package ru.ppr.nsi.entity;

/**
 * Запрет оформления вида ПД для категории льготника.
 *
 * @author Aleksandr Brazhkin
 */
public class ProhibitedTicketTypeForExemptionCategory extends BaseNSIObject<Long> {

    /**
     * Код типа носителя ПД
     */
    private long ticketStorageTypeCode;
    /**
     * Категория льготника
     */
    private String category;
    /**
     * Код типа ПД
     */
    private long ticketTypeCode;

    public long getTicketStorageTypeCode() {
        return ticketStorageTypeCode;
    }

    public void setTicketStorageTypeCode(long ticketStorageTypeCode) {
        this.ticketStorageTypeCode = ticketStorageTypeCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

}
