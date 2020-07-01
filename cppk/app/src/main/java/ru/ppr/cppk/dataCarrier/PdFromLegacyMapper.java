package ru.ppr.cppk.dataCarrier;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;
import ru.ppr.core.dataCarrier.pd.v1.PdV1;
import ru.ppr.core.dataCarrier.pd.v1.PdV1Impl;
import ru.ppr.core.dataCarrier.pd.v11.PdV11;
import ru.ppr.core.dataCarrier.pd.v11.PdV11Impl;
import ru.ppr.core.dataCarrier.pd.v12.PdV12;
import ru.ppr.core.dataCarrier.pd.v12.PdV12Impl;
import ru.ppr.core.dataCarrier.pd.v13.PdV13;
import ru.ppr.core.dataCarrier.pd.v13.PdV13Impl;
import ru.ppr.core.dataCarrier.pd.v14.PdV14;
import ru.ppr.core.dataCarrier.pd.v14.PdV14Impl;
import ru.ppr.core.dataCarrier.pd.v15.PdV15;
import ru.ppr.core.dataCarrier.pd.v15.PdV15Impl;
import ru.ppr.core.dataCarrier.pd.v16.PdV16;
import ru.ppr.core.dataCarrier.pd.v16.PdV16Impl;
import ru.ppr.core.dataCarrier.pd.v17.PdV17;
import ru.ppr.core.dataCarrier.pd.v17.PdV17Impl;
import ru.ppr.core.dataCarrier.pd.v18.PdV18;
import ru.ppr.core.dataCarrier.pd.v18.PdV18Impl;
import ru.ppr.core.dataCarrier.pd.v19.PdV19;
import ru.ppr.core.dataCarrier.pd.v19.PdV19Impl;
import ru.ppr.core.dataCarrier.pd.v2.PdV2;
import ru.ppr.core.dataCarrier.pd.v2.PdV2Impl;
import ru.ppr.core.dataCarrier.pd.v20.PdV20;
import ru.ppr.core.dataCarrier.pd.v20.PdV20Impl;
import ru.ppr.core.dataCarrier.pd.v21.PdV21;
import ru.ppr.core.dataCarrier.pd.v21.PdV21Impl;
import ru.ppr.core.dataCarrier.pd.v22.PdV22;
import ru.ppr.core.dataCarrier.pd.v22.PdV22Impl;
import ru.ppr.core.dataCarrier.pd.v23.PdV23;
import ru.ppr.core.dataCarrier.pd.v23.PdV23Impl;
import ru.ppr.core.dataCarrier.pd.v24.PdV24;
import ru.ppr.core.dataCarrier.pd.v24.PdV24Impl;
import ru.ppr.core.dataCarrier.pd.v25.PdV25;
import ru.ppr.core.dataCarrier.pd.v25.PdV25Impl;
import ru.ppr.core.dataCarrier.pd.v3.PdV3;
import ru.ppr.core.dataCarrier.pd.v3.PdV3Impl;
import ru.ppr.core.dataCarrier.pd.v4.PdV4;
import ru.ppr.core.dataCarrier.pd.v4.PdV4Impl;
import ru.ppr.core.dataCarrier.pd.v5.PdV5;
import ru.ppr.core.dataCarrier.pd.v5.PdV5Impl;
import ru.ppr.core.dataCarrier.pd.v6.PdV6;
import ru.ppr.core.dataCarrier.pd.v6.PdV6Impl;
import ru.ppr.core.dataCarrier.pd.v64.PdV64;
import ru.ppr.core.dataCarrier.pd.v64.PdV64Impl;
import ru.ppr.core.dataCarrier.pd.v7.PdV7;
import ru.ppr.core.dataCarrier.pd.v7.PdV7Impl;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.localdb.model.TicketWayType;

/**
 * Маппер старых сущностей ПД в новые.
 *
 * @author Aleksandr Brazhkin
 */
public class PdFromLegacyMapper {

    public PdFromLegacyMapper() {

    }

    public Pd fromLegacyPd(PD legacyPd) {
        PdVersion pdVersion = PdVersion.getByCode(legacyPd.versionPD);
        if (pdVersion == null) {
            return null;
        }
        switch (pdVersion) {
            case V1:
                return fromLegacyPdV1(legacyPd);
            case V2:
                return fromLegacyPdV2(legacyPd);
            case V3:
                return fromLegacyPdV3(legacyPd);
            case V4:
                return fromLegacyPdV4(legacyPd);
            case V5:
                return fromLegacyPdV5(legacyPd);
            case V6:
                return fromLegacyPdV6(legacyPd);
            case V7:
                return fromLegacyPdV7(legacyPd);
            case V11:
                return fromLegacyPdV11(legacyPd);
            case V12:
                return fromLegacyPdV12(legacyPd);
            case V13:
                return fromLegacyPdV13(legacyPd);
            case V14:
                return fromLegacyPdV14(legacyPd);
            case V15:
                return fromLegacyPdV15(legacyPd);
            case V16:
                return fromLegacyPdV16(legacyPd);
            case V17:
                return fromLegacyPdV17(legacyPd);
            case V18:
                return fromLegacyPdV18(legacyPd);
            case V19:
                return fromLegacyPdV19(legacyPd);
            case V20:
                return fromLegacyPdV20(legacyPd);
            case V21:
                return fromLegacyPdV21(legacyPd);
            case V22:
                return fromLegacyPdV22(legacyPd);
            case V23:
                return fromLegacyPdV23(legacyPd);
            case V24:
                return fromLegacyPdV24(legacyPd);
            case V25:
                return fromLegacyPdV25(legacyPd);
            case V64:
                return fromLegacyPdV64(legacyPd);
            default:
                return null;
        }
    }

    private PdV1 fromLegacyPdV1(PD legacyPd) {
        PdV1Impl pdV1 = new PdV1Impl();
        pdV1.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV1.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV1.setOrderNumber(legacyPd.numberPD);
        pdV1.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV1.setStartDayOffset(legacyPd.term);
        pdV1.setTariffCode(legacyPd.tariffCodePD);
        pdV1.setExemptionCode(legacyPd.exemptionCode);
        pdV1.setEdsKeyNumber(legacyPd.ecpNumberPD);
        pdV1.setEds(legacyPd.ecp);
        return pdV1;
    }

    private PdV2 fromLegacyPdV2(PD legacyPd) {
        PdV2Impl pdV2 = new PdV2Impl();
        pdV2.setOrderNumber(legacyPd.numberPD);
        pdV2.setStartDayOffset(legacyPd.term);
        pdV2.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV2.setTariffCode(legacyPd.tariffCodePD);
        pdV2.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV2.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV2.setEdsKeyNumber(legacyPd.ecpNumberPD);
        pdV2.setEds(legacyPd.ecp);
        pdV2.setSourceOrderNumber(legacyPd.parentTicketInfo.getTicketNumber());
        pdV2.setSourceSaleDateTime(legacyPd.parentTicketInfo.getSaleDateTime());
        pdV2.setSourceDeviceId(legacyPd.parentTicketInfo.getCashRegisterNumber());
        return pdV2;
    }

    private PdV3 fromLegacyPdV3(PD legacyPd) {
        PdV3Impl pdV3 = new PdV3Impl();
        pdV3.setStartDayOffset(legacyPd.term);
        pdV3.setOrderNumber(legacyPd.numberPD);
        pdV3.setTicketType(legacyPd.exemptionCode > 0 ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV3.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV3.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV3.setTariffCode(legacyPd.tariffCodePD);
        pdV3.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV3;
    }

    private PdV4 fromLegacyPdV4(PD legacyPd) {
        PdV4Impl pdV4 = new PdV4Impl();
        pdV4.setOrderNumber(legacyPd.numberPD);
        pdV4.setTicketType(legacyPd.exemptionCode > 0 ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV4.setStartDayOffset(legacyPd.term);
        pdV4.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV4.setTariffCode(legacyPd.tariffCodePD);
        pdV4.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV4;
    }

    private PdV5 fromLegacyPdV5(PD legacyPd) {
        PdV5Impl pdV5 = new PdV5Impl();
        pdV5.setOrderNumber(legacyPd.numberPD);
        pdV5.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV5.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV5.setStartDayOffset(legacyPd.term);
        pdV5.setExemptionCode(legacyPd.exemptionCode);
        pdV5.setTariffCode(legacyPd.tariffCodePD);
        pdV5.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV5;
    }

    private PdV6 fromLegacyPdV6(PD legacyPd) {
        PdV6Impl pdV6 = new PdV6Impl();
        pdV6.setOrderNumber(legacyPd.numberPD);
        pdV6.setForDays((int) legacyPd.actionDays);
        pdV6.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV6.setTariffCode(legacyPd.tariffCodePD);
        pdV6.setExemptionCode(legacyPd.exemptionCode);
        pdV6.setEdsKeyNumber(legacyPd.ecpNumberPD);
        pdV6.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV6.setStartDayOffset(legacyPd.term);
        return pdV6;
    }

    private PdV7 fromLegacyPdV7(PD legacyPd) {
        PdV7Impl pdV7 = new PdV7Impl();
        pdV7.setOrderNumber(legacyPd.numberPD);
        pdV7.setStartDayOffset(legacyPd.term);
        pdV7.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV7.setTariffCode(legacyPd.tariffCodePD);
        pdV7.setEdsKeyNumber(legacyPd.ecpNumberPD);
        pdV7.setStartCounterValue(legacyPd.startCountValue);
        pdV7.setEndCounterValue(legacyPd.endCountValue);
        pdV7.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        return pdV7;
    }

    private PdV11 fromLegacyPdV11(PD legacyPd) {
        PdV11Impl pdV11 = new PdV11Impl();
        pdV11.setStartDayOffset(legacyPd.term);
        pdV11.setOrderNumber(legacyPd.numberPD);
        pdV11.setTicketType(legacyPd.exemptionCode > 0 ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV11.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV11.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV11.setTariffCode(legacyPd.tariffCodePD);
        pdV11.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV11;
    }

    private PdV12 fromLegacyPdV12(PD legacyPd) {
        PdV12Impl pdV12 = new PdV12Impl();
        pdV12.setOrderNumber(legacyPd.numberPD);
        pdV12.setTicketType(legacyPd.exemptionCode > 0 ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV12.setStartDayOffset(legacyPd.term);
        pdV12.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV12.setTariffCode(legacyPd.tariffCodePD);
        pdV12.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV12;
    }

    private PdV13 fromLegacyPdV13(PD legacyPd) {
        PdV13Impl pdV13 = new PdV13Impl();
        pdV13.setOrderNumber(legacyPd.numberPD);
        pdV13.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV13.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV13.setStartDayOffset(legacyPd.term);
        pdV13.setExemptionCode(legacyPd.exemptionCode);
        pdV13.setTariffCode(legacyPd.tariffCodePD);
        pdV13.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV13;
    }

    private PdV14 fromLegacyPdV14(PD legacyPd) {
        PdV14Impl pdV14 = new PdV14Impl();
        pdV14.setOrderNumber(legacyPd.numberPD);
        pdV14.setTicketType(legacyPd.exemptionCode > 0 ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV14.setStartDayOffset(legacyPd.term);
        pdV14.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV14.setTariffCode(legacyPd.tariffCodePD);
        pdV14.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV14;
    }

    private PdV15 fromLegacyPdV15(PD legacyPd) {
        PdV15Impl pdV15 = new PdV15Impl();
        pdV15.setOrderNumber(legacyPd.numberPD);
        pdV15.setTicketType(legacyPd.exemptionCode > 0 ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV15.setStartDayOffset(legacyPd.term);
        pdV15.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV15.setTariffCode(legacyPd.tariffCodePD);
        pdV15.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV15;
    }

    private PdV16 fromLegacyPdV16(PD legacyPd) {
        PdV16Impl pdV16 = new PdV16Impl();
        pdV16.setOrderNumber(legacyPd.numberPD);
        pdV16.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV16.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV16.setStartDayOffset(legacyPd.term);
        pdV16.setExemptionCode(legacyPd.exemptionCode);
        pdV16.setTariffCode(legacyPd.tariffCodePD);
        pdV16.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV16;
    }

    private PdV17 fromLegacyPdV17(PD legacyPd) {
        PdV17Impl pdV17 = new PdV17Impl();
        pdV17.setOrderNumber(legacyPd.numberPD);
        pdV17.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV17.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV17.setStartDayOffset(legacyPd.term);
        pdV17.setExemptionCode(legacyPd.exemptionCode);
        pdV17.setTariffCode(legacyPd.tariffCodePD);
        pdV17.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV17;
    }

    private PdV18 fromLegacyPdV18(PD legacyPd) {
        PdV18Impl pdV18 = new PdV18Impl();
        pdV18.setOrderNumber(legacyPd.numberPD);
        pdV18.setStartDayOffset(legacyPd.term);
        pdV18.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV18.setTariffCode(legacyPd.tariffCodePD);
        pdV18.setEdsKeyNumber(legacyPd.ecpNumberPD);
        pdV18.setStartCounterValue(legacyPd.startCountValue);
        pdV18.setEndCounterValue(legacyPd.endCountValue);
        pdV18.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        return pdV18;
    }

    private PdV19 fromLegacyPdV19(PD legacyPd) {
        PdV19Impl pdV19 = new PdV19Impl();
        pdV19.setOrderNumber(legacyPd.numberPD);
        pdV19.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV19.setStartDayOffset(legacyPd.term);
        pdV19.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV19.setTariffCode(legacyPd.tariffCodePD);
        pdV19.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV19;
    }

    private PdV20 fromLegacyPdV20(PD legacyPd) {
        PdV20Impl pdV20 = new PdV20Impl();
        pdV20.setOrderNumber(legacyPd.numberPD);
        pdV20.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV20.setStartDayOffset(legacyPd.term);
        pdV20.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV20.setTariffCode(legacyPd.tariffCodePD);
        pdV20.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV20;
    }

    private PdV21 fromLegacyPdV21(PD legacyPd) {
        PdV21Impl pdV21 = new PdV21Impl();
        pdV21.setOrderNumber(legacyPd.numberPD);
        pdV21.setTicketType(legacyPd.exemptionCode > 0 ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV21.setStartDayOffset(legacyPd.term);
        pdV21.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV21.setServiceId(legacyPd.serviceFeeCode);
        pdV21.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV21;
    }

    private PdV22 fromLegacyPdV22(PD legacyPd) {
        PdV22Impl pdV22 = new PdV22Impl();
        pdV22.setOrderNumber(legacyPd.numberPD);
        pdV22.setDirection(legacyPd.wayType == TicketWayType.OneWay ? PdWithDirection.DIRECTION_THERE : PdWithDirection.DIRECTION_BACK);
        pdV22.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV22.setPassageToStationCheckRequired(legacyPd.isPassageToStationCheckRequired());
        pdV22.setActivationRequired(legacyPd.isActivationRequired());
        pdV22.setPhoneNumber(legacyPd.getPhoneNumber());
        pdV22.setStartDayOffset(legacyPd.term);
        pdV22.setExemptionCode(legacyPd.exemptionCode);
        pdV22.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV22.setTariffCode(legacyPd.tariffCodePD);
        pdV22.setEdsKeyNumber(legacyPd.ecpNumberPD);
        pdV22.setEds(legacyPd.ecp);
        return pdV22;
    }

    private PdV23 fromLegacyPdV23(PD legacyPd) {
        PdV23Impl pdV23 = new PdV23Impl();
        pdV23.setOrderNumber(legacyPd.numberPD);
        pdV23.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV23.setStartDayOffset(legacyPd.term);
        pdV23.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV23.setTariffCode(legacyPd.tariffCodePD);
        pdV23.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV23;
    }

    private PdV24 fromLegacyPdV24(PD legacyPd) {
        PdV24Impl pdV24 = new PdV24Impl();
        pdV24.setOrderNumber(legacyPd.numberPD);
        pdV24.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV24.setStartDayOffset(legacyPd.term);
        pdV24.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV24.setTariffCode(legacyPd.tariffCodePD);
        pdV24.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV24;
    }

    private PdV25 fromLegacyPdV25(PD legacyPd) {
        PdV25Impl pdV25 = new PdV25Impl();
        pdV25.setOrderNumber(legacyPd.numberPD);
        pdV25.setForDays((int) legacyPd.actionDays);
        pdV25.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV25.setTariffCode(legacyPd.tariffCodePD);
        pdV25.setExemptionCode(legacyPd.exemptionCode);
        pdV25.setEdsKeyNumber(legacyPd.ecpNumberPD);
        pdV25.setPaymentType(legacyPd.issBankPaymentType() ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV25.setStartDayOffset(legacyPd.term);
        return pdV25;
    }

    private PdV64 fromLegacyPdV64(PD legacyPd) {
        PdV64Impl pdV64 = new PdV64Impl();
        pdV64.setSaleDateTime(legacyPd.saleDatetimePD);
        pdV64.setEdsKeyNumber(legacyPd.ecpNumberPD);
        return pdV64;
    }
}
