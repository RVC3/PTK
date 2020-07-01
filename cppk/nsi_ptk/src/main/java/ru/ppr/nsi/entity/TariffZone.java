package ru.ppr.nsi.entity;

/**
 * Тарифный пояс (зона)
 */
public class TariffZone extends BaseNSIObject<Long> {

    /**
     * Наименование
     */
    private String name;
    /**
     * Расстояние от, км
     */
    private Integer distanceFrom;
    /**
     * Расстояние до, км
     */
    private Integer distanceTo;
    /**
     * Признак специального пояса
     */
    private boolean specialZone;
    /**
     * Признак оформления ПД от станции вне пояса
     */
    private boolean ticketIssuedOutsideZone;
    /**
     * Код тарифного плана
     */
    private Long tariffPlanCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDistanceFrom() {
        return distanceFrom;
    }

    public void setDistanceFrom(Integer distanceFrom) {
        this.distanceFrom = distanceFrom;
    }

    public Integer getDistanceTo() {
        return distanceTo;
    }

    public void setDistanceTo(Integer distanceTo) {
        this.distanceTo = distanceTo;
    }

    public boolean isSpecialZone() {
        return specialZone;
    }

    public void setSpecialZone(boolean specialZone) {
        this.specialZone = specialZone;
    }

    public boolean isTicketIssuedOutsideZone() {
        return ticketIssuedOutsideZone;
    }

    public void setTicketIssuedOutsideZone(boolean ticketIssuedOutsideZone) {
        this.ticketIssuedOutsideZone = ticketIssuedOutsideZone;
    }

    public Long getTariffPlanCode() {
        return tariffPlanCode;
    }

    public void setTariffPlanCode(Long tariffPlanCode) {
        this.tariffPlanCode = tariffPlanCode;
    }
}
