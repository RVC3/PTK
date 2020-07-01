package ru.ppr.nsi.model;

import ru.ppr.nsi.repository.TariffRepository;

/**
 * Информация о тарифе.
 * Синтетическая сущность, возвращаемая методом {@link TariffRepository#loadTariffsForSaleQuery}.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffDescription {

    private Long depStationCode;
    private Long destStationCode;
    private Long tariffPlanCode;
    private Long ticketTypeCode;
    private Long tariffCode;

    public Long getDepStationCode() {
        return depStationCode;
    }

    public void setDepStationCode(Long depStationCode) {
        this.depStationCode = depStationCode;
    }

    public Long getDestStationCode() {
        return destStationCode;
    }

    public void setDestStationCode(Long destStationCode) {
        this.destStationCode = destStationCode;
    }

    public Long getTariffPlanCode() {
        return tariffPlanCode;
    }

    public void setTariffPlanCode(Long tariffPlanCode) {
        this.tariffPlanCode = tariffPlanCode;
    }

    public Long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

    public Long getTariffCode() {
        return tariffCode;
    }

    public void setTariffCode(Long tariffCode) {
        this.tariffCode = tariffCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TariffDescription that = (TariffDescription) o;

        if (depStationCode != null ? !depStationCode.equals(that.depStationCode) : that.depStationCode != null)
            return false;
        if (destStationCode != null ? !destStationCode.equals(that.destStationCode) : that.destStationCode != null)
            return false;
        if (tariffPlanCode != null ? !tariffPlanCode.equals(that.tariffPlanCode) : that.tariffPlanCode != null)
            return false;
        if (ticketTypeCode != null ? !ticketTypeCode.equals(that.ticketTypeCode) : that.ticketTypeCode != null)
            return false;
        return tariffCode != null ? tariffCode.equals(that.tariffCode) : that.tariffCode == null;
    }

    @Override
    public int hashCode() {
        int result = depStationCode != null ? depStationCode.hashCode() : 0;
        result = 31 * result + (destStationCode != null ? destStationCode.hashCode() : 0);
        result = 31 * result + (tariffPlanCode != null ? tariffPlanCode.hashCode() : 0);
        result = 31 * result + (ticketTypeCode != null ? ticketTypeCode.hashCode() : 0);
        result = 31 * result + (tariffCode != null ? tariffCode.hashCode() : 0);
        return result;
    }

    public enum Field {
        DEP_STATION_CODE,
        DEST_STATION_CODE,
        TARIFF_PLAN_CODE,
        TICKET_TYPE_CODE,
        TARIFF_CODE
    }
}
