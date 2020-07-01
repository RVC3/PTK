package ru.ppr.cppk.logic.pdSale;

import java.util.Arrays;
import java.util.Date;

import ru.ppr.cppk.localdb.model.TicketWayType;

/**
 * Входные данные для расчета ограничений на оформление ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleRestrictionsParams {
    /**
     * Версия НСИ для оформления ПД
     */
    private int nsiVersion;
    /**
     * Дата начала процесса оформления ПД
     */
    private Date timestamp;
    /**
     * Код производственного участка работы ПТК
     */
    private int productionSectionCode;
    /**
     * Режим мобильной кассы
     */
    private boolean mobileCashRegister;
    /**
     * Разрешение на оформление ПД вне привязанного участка
     */
    private boolean outsideProductionSectionSaleEnabled;
    /**
     * Список станций до которых можно продать билет
     */
    private long[] allowedStationsCodes;
    /**
     * Данные для доплаты
     */
    private ExtraPaymentData extraPaymentData;
    /**
     * Данные для оформления трансфера
     */
    private TransferSaleData transferSaleData;

    public PdSaleRestrictionsParams() {

    }

    public int getNsiVersion() {
        return nsiVersion;
    }

    public void setNsiVersion(int nsiVersion) {
        this.nsiVersion = nsiVersion;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getProductionSectionCode() {
        return productionSectionCode;
    }

    public void setProductionSectionCode(int productionSectionCode) {
        this.productionSectionCode = productionSectionCode;
    }

    public boolean isMobileCashRegister() {
        return mobileCashRegister;
    }

    public void setMobileCashRegister(boolean mobileCashRegister) {
        this.mobileCashRegister = mobileCashRegister;
    }

    public boolean isOutsideProductionSectionSaleEnabled() {
        return outsideProductionSectionSaleEnabled;
    }

    public void setOutsideProductionSectionSaleEnabled(boolean outsideProductionSectionSaleEnabled) {
        this.outsideProductionSectionSaleEnabled = outsideProductionSectionSaleEnabled;
    }

    public long[] getAllowedStationsCodes() {
        return allowedStationsCodes;
    }

    public void setAllowedStationsCodes(long[] allowedStationsCodes) {
        this.allowedStationsCodes = allowedStationsCodes;
    }

    public ExtraPaymentData getExtraPaymentData() {
        return extraPaymentData;
    }

    public void setExtraPaymentData(ExtraPaymentData extraPaymentData) {
        this.extraPaymentData = extraPaymentData;
    }

    public TransferSaleData getTransferSaleData() {
        return transferSaleData;
    }

    public void setTransferSaleData(TransferSaleData transferSaleData) {
        this.transferSaleData = transferSaleData;
    }

    boolean equalsForGeneralRestrictions(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PdSaleRestrictionsParams that = (PdSaleRestrictionsParams) o;

        if (nsiVersion != that.nsiVersion) return false;
        if (productionSectionCode != that.productionSectionCode) return false;
        if (mobileCashRegister != that.mobileCashRegister) return false;
        if (outsideProductionSectionSaleEnabled != that.outsideProductionSectionSaleEnabled)
            return false;
        if (!Arrays.equals(allowedStationsCodes, that.allowedStationsCodes)) return false;
        if (extraPaymentData != null ? !extraPaymentData.equals(that.extraPaymentData) : that.extraPaymentData != null)
            return false;
        return transferSaleData != null ? transferSaleData.equals(that.transferSaleData) : that.transferSaleData == null;
    }

    boolean equalsForTimeDependentRestrictions(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PdSaleRestrictionsParams that = (PdSaleRestrictionsParams) o;

        if (nsiVersion != that.nsiVersion) return false;
        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    /**
     * Данные для доплаты
     */
    public static class ExtraPaymentData {

        private Long parentDepartureStationCode;
        private Long parentDestinationStationCode;
        private Long parentDepartureTariffZoneCode;
        private Long parentDestinationTariffZoneCode;

        public Long getParentDepartureStationCode() {
            return parentDepartureStationCode;
        }

        public void setParentDepartureStationCode(Long parentDepartureStationCode) {
            this.parentDepartureStationCode = parentDepartureStationCode;
        }

        public Long getParentDestinationStationCode() {
            return parentDestinationStationCode;
        }

        public void setParentDestinationStationCode(Long parentDestinationStationCode) {
            this.parentDestinationStationCode = parentDestinationStationCode;
        }

        public Long getParentDepartureTariffZoneCode() {
            return parentDepartureTariffZoneCode;
        }

        public void setParentDepartureTariffZoneCode(Long parentDepartureTariffZoneCode) {
            this.parentDepartureTariffZoneCode = parentDepartureTariffZoneCode;
        }

        public Long getParentDestinationTariffZoneCode() {
            return parentDestinationTariffZoneCode;
        }

        public void setParentDestinationTariffZoneCode(Long parentDestinationTariffZoneCode) {
            this.parentDestinationTariffZoneCode = parentDestinationTariffZoneCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExtraPaymentData that = (ExtraPaymentData) o;

            if (parentDepartureStationCode != null ? !parentDepartureStationCode.equals(that.parentDepartureStationCode) : that.parentDepartureStationCode != null)
                return false;
            if (parentDestinationStationCode != null ? !parentDestinationStationCode.equals(that.parentDestinationStationCode) : that.parentDestinationStationCode != null)
                return false;
            if (parentDepartureTariffZoneCode != null ? !parentDepartureTariffZoneCode.equals(that.parentDepartureTariffZoneCode) : that.parentDepartureTariffZoneCode != null)
                return false;
            return parentDestinationTariffZoneCode != null ? parentDestinationTariffZoneCode.equals(that.parentDestinationTariffZoneCode) : that.parentDestinationTariffZoneCode == null;
        }

        @Override
        public int hashCode() {
            int result = parentDepartureStationCode != null ? parentDepartureStationCode.hashCode() : 0;
            result = 31 * result + (parentDestinationStationCode != null ? parentDestinationStationCode.hashCode() : 0);
            result = 31 * result + (parentDepartureTariffZoneCode != null ? parentDepartureTariffZoneCode.hashCode() : 0);
            result = 31 * result + (parentDestinationTariffZoneCode != null ? parentDestinationTariffZoneCode.hashCode() : 0);
            return result;
        }

    }

    /**
     * Данные для оформления трансфера
     */
    public static class TransferSaleData {
        /**
         * Флаг наличия родительского ПД
         */
        private boolean withParentPd;
        /**
         * Дата продажи родительского ПД
         */
        private Date parentPdSaleDateTime;
        /**
         * Дата начала действия родительского ПД
         */
        private Date parentPdStartDateTime;
        /**
         * Код тарифа родительского ПД
         */
        private long parentPdTariffCode;
        /**
         * Направление родительского ПД
         */
        private TicketWayType parentPdDirection;
        /**
         * Станция работы ПТК в режиме мобильной кассы на вход
         * {@code null} в ином режиме работы ПТК
         */
        private Long mobileCashRegisterStationCode;

        public boolean isWithParentPd() {
            return withParentPd;
        }

        public void setWithParentPd(boolean withParentPd) {
            this.withParentPd = withParentPd;
        }

        public Date getParentPdSaleDateTime() {
            return parentPdSaleDateTime;
        }

        public void setParentPdSaleDateTime(Date parentPdSaleDateTime) {
            this.parentPdSaleDateTime = parentPdSaleDateTime;
        }

        public Date getParentPdStartDateTime() {
            return parentPdStartDateTime;
        }

        public void setParentPdStartDateTime(Date parentPdStartDateTime) {
            this.parentPdStartDateTime = parentPdStartDateTime;
        }

        public long getParentPdTariffCode() {
            return parentPdTariffCode;
        }

        public void setParentPdTariffCode(long parentPdTariffCode) {
            this.parentPdTariffCode = parentPdTariffCode;
        }

        public TicketWayType getParentPdDirection() {
            return parentPdDirection;
        }

        public void setParentPdDirection(TicketWayType parentPdDirection) {
            this.parentPdDirection = parentPdDirection;
        }

        public Long getMobileCashRegisterStationCode() {
            return mobileCashRegisterStationCode;
        }

        public void setMobileCashRegisterStationCode(Long mobileCashRegisterStationCode) {
            this.mobileCashRegisterStationCode = mobileCashRegisterStationCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransferSaleData that = (TransferSaleData) o;

            if (withParentPd != that.withParentPd) return false;
            if (parentPdTariffCode != that.parentPdTariffCode) return false;
            if (parentPdSaleDateTime != null ? !parentPdSaleDateTime.equals(that.parentPdSaleDateTime) : that.parentPdSaleDateTime != null)
                return false;
            if (parentPdStartDateTime != null ? !parentPdStartDateTime.equals(that.parentPdStartDateTime) : that.parentPdStartDateTime != null)
                return false;
            if (parentPdDirection != that.parentPdDirection) return false;
            return mobileCashRegisterStationCode != null ? mobileCashRegisterStationCode.equals(that.mobileCashRegisterStationCode) : that.mobileCashRegisterStationCode == null;
        }

        @Override
        public int hashCode() {
            int result = (withParentPd ? 1 : 0);
            result = 31 * result + (parentPdSaleDateTime != null ? parentPdSaleDateTime.hashCode() : 0);
            result = 31 * result + (parentPdStartDateTime != null ? parentPdStartDateTime.hashCode() : 0);
            result = 31 * result + (int) (parentPdTariffCode ^ (parentPdTariffCode >>> 32));
            result = 31 * result + (parentPdDirection != null ? parentPdDirection.hashCode() : 0);
            result = 31 * result + (mobileCashRegisterStationCode != null ? mobileCashRegisterStationCode.hashCode() : 0);
            return result;
        }
    }

}
