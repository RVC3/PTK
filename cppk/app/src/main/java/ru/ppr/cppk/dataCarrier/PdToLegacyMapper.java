package ru.ppr.cppk.dataCarrier;

import android.support.annotation.Nullable;

import java.util.Arrays;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;
import ru.ppr.core.dataCarrier.pd.v1.PdV1;
import ru.ppr.core.dataCarrier.pd.v11.PdV11;
import ru.ppr.core.dataCarrier.pd.v12.PdV12;
import ru.ppr.core.dataCarrier.pd.v13.PdV13;
import ru.ppr.core.dataCarrier.pd.v14.PdV14;
import ru.ppr.core.dataCarrier.pd.v15.PdV15;
import ru.ppr.core.dataCarrier.pd.v16.PdV16;
import ru.ppr.core.dataCarrier.pd.v17.PdV17;
import ru.ppr.core.dataCarrier.pd.v18.PdV18;
import ru.ppr.core.dataCarrier.pd.v19.PdV19;
import ru.ppr.core.dataCarrier.pd.v2.PdV2;
import ru.ppr.core.dataCarrier.pd.v20.PdV20;
import ru.ppr.core.dataCarrier.pd.v21.PdV21;
import ru.ppr.core.dataCarrier.pd.v22.PdV22;
import ru.ppr.core.dataCarrier.pd.v23.PdV23;
import ru.ppr.core.dataCarrier.pd.v24.PdV24;
import ru.ppr.core.dataCarrier.pd.v25.PdV25;
import ru.ppr.core.dataCarrier.pd.v3.PdV3;
import ru.ppr.core.dataCarrier.pd.v4.PdV4;
import ru.ppr.core.dataCarrier.pd.v5.PdV5;
import ru.ppr.core.dataCarrier.pd.v6.PdV6;
import ru.ppr.core.dataCarrier.pd.v64.PdV64;
import ru.ppr.core.dataCarrier.pd.v7.PdV7;
import ru.ppr.core.dataCarrier.pd.v7.PdV7Encoder;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.entity.PassageMark;
import ru.ppr.cppk.dataCarrier.utils.CRC16Modbus;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.TicketWayType;

/**
 * Маппер новых сущностей ПД в старые.
 *
 * @author Aleksandr Brazhkin
 */
public class PdToLegacyMapper {

    public PdToLegacyMapper() {

    }

    public PD toLegacyPd(Pd pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        switch (pd.getVersion()) {
            case V1:
                return toLegacyPdV1((PdV1) pd, bscInformation, passageMark);
            case V2:
                return toLegacyPdV2((PdV2) pd, bscInformation, passageMark);
            case V3:
                return toLegacyPdV3((PdV3) pd, bscInformation, passageMark);
            case V4:
                return toLegacyPdV4((PdV4) pd, bscInformation, passageMark);
            case V5:
                return toLegacyPdV5((PdV5) pd, bscInformation, passageMark);
            case V6:
                return toLegacyPdV6((PdV6) pd, bscInformation, passageMark);
            case V7:
                return toLegacyPdV7((PdV7) pd, bscInformation, passageMark);
            case V11:
                return toLegacyPdV11((PdV11) pd, bscInformation, passageMark);
            case V12:
                return toLegacyPdV12((PdV12) pd, bscInformation, passageMark);
            case V13:
                return toLegacyPdV13((PdV13) pd, bscInformation, passageMark);
            case V14:
                return toLegacyPdV14((PdV14) pd, bscInformation, passageMark);
            case V15:
                return toLegacyPdV15((PdV15) pd, bscInformation, passageMark);
            case V16:
                return toLegacyPdV16((PdV16) pd, bscInformation, passageMark);
            case V17:
                return toLegacyPdV17((PdV17) pd, bscInformation, passageMark);
            case V18:
                return toLegacyPdV18((PdV18) pd, bscInformation, passageMark);
            case V19:
                return toLegacyPdV19((PdV19) pd, bscInformation, passageMark);
            case V20:
                return toLegacyPdV20((PdV20) pd, bscInformation, passageMark);
            case V21:
                return toLegacyPdV21((PdV21) pd, bscInformation, passageMark);
            case V22:
                return toLegacyPdV22((PdV22) pd, bscInformation, passageMark);
            case V23:
                return toLegacyPdV23((PdV23) pd, bscInformation, passageMark);
            case V24:
                return toLegacyPdV24((PdV24) pd, bscInformation, passageMark);
            case V25:
                return toLegacyPdV25((PdV25) pd, bscInformation, passageMark);
            case V64:
                return toLegacyPdV64((PdV64) pd, bscInformation, passageMark);
            default:
                return null;
        }
    }

    private PD toLegacyPdV1(PdV1 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.exemptionCode = pd.getExemptionCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.ecp = pd.getEds();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV2(PdV2 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.ecp = pd.getEds();
        ParentTicketInfo parentTicketInfo = new ParentTicketInfo();
        parentTicketInfo.setTicketNumber(pd.getSourceOrderNumber());
        parentTicketInfo.setSaleDateTime(pd.getSourceSaleDateTime());
        // направление исходного билета в этом случае не интересует, запишем любое значение чтобы не схватить NPE
        parentTicketInfo.setWayType(TicketWayType.TwoWay);
        parentTicketInfo.setCashRegisterNumber(pd.getSourceDeviceId());
        legacyPd.parentTicketInfo = parentTicketInfo;
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV3(PdV3 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.numberPD = pd.getOrderNumber();

        if (pd.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION) {
            legacyPd.exemptionCode = (bscInformation == null ? 0 : bscInformation.getExemptionCode());
        } else {
            legacyPd.exemptionCode = 0;
        }

        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(false);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV4(PdV4 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();

        if (pd.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION) {
            legacyPd.exemptionCode = (bscInformation == null ? 0 : bscInformation.getExemptionCode());
        } else {
            legacyPd.exemptionCode = 0;
        }

        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(false);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV5(PdV5 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(false);
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.exemptionCode = pd.getExemptionCode();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV6(PdV6 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.actionDays = 0xffffffffL & pd.getForDays();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.exemptionCode = pd.getExemptionCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV7(PdV7 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        byte[] pdDataForCrc = new PdV7Encoder().encode(pd, false);
        byte[] calculatedCrc = CRC16Modbus.getCrc(pdDataForCrc);

        if (!Arrays.equals(pd.getCrc(), calculatedCrc)) {
            return null;
        }

        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.startCountValue = pd.getStartCounterValue();
        legacyPd.endCountValue = pd.getEndCounterValue();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV11(PdV11 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.numberPD = pd.getOrderNumber();

        if (pd.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION) {
            legacyPd.exemptionCode = (bscInformation == null ? 0 : bscInformation.getExemptionCode());
        } else {
            legacyPd.exemptionCode = 0;
        }

        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(true);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV12(PdV12 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();

        if (pd.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION) {
            legacyPd.exemptionCode = (bscInformation == null ? 0 : bscInformation.getExemptionCode());
        } else {
            legacyPd.exemptionCode = 0;
        }

        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(true);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV13(PdV13 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(true);
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.exemptionCode = pd.getExemptionCode();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV14(PdV14 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();

        if (pd.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION) {
            legacyPd.exemptionCode = (bscInformation == null ? 0 : bscInformation.getExemptionCode());
        } else {
            legacyPd.exemptionCode = 0;
        }

        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(false);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(true);

        return legacyPd;
    }

    private PD toLegacyPdV15(PdV15 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();

        if (pd.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION) {
            legacyPd.exemptionCode = (bscInformation == null ? 0 : bscInformation.getExemptionCode());
        } else {
            legacyPd.exemptionCode = 0;
        }

        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(true);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(true);

        return legacyPd;
    }

    private PD toLegacyPdV16(PdV16 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(false);
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.exemptionCode = pd.getExemptionCode();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(true);

        return legacyPd;
    }

    private PD toLegacyPdV17(PdV17 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(true);
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.exemptionCode = pd.getExemptionCode();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(true);

        return legacyPd;
    }

    private PD toLegacyPdV18(PdV18 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        byte[] pdDataForCrc = new PdV7Encoder().encode(pd, false);
        byte[] calculatedCrc = CRC16Modbus.getCrc(pdDataForCrc);

        if (!Arrays.equals(pd.getCrc(), calculatedCrc)) {
            return null;
        }

        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.startCountValue = pd.getStartCounterValue();
        legacyPd.endCountValue = pd.getEndCounterValue();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.setRestoredTicket(true);

        return legacyPd;
    }

    private PD toLegacyPdV19(PdV19 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV20(PdV20 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(true);

        return legacyPd;
    }

    private PD toLegacyPdV21(PdV21 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();

        if (pd.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION) {
            legacyPd.exemptionCode = (bscInformation == null ? 0 : bscInformation.getExemptionCode());
        } else {
            legacyPd.exemptionCode = 0;
        }

        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(true);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.serviceFeeCode = pd.getServiceId();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV22(PdV22 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.wayType = pd.getDirection() == PdWithDirection.DIRECTION_THERE ? TicketWayType.OneWay : TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.setPassageToStationCheckRequired(pd.isPassageToStationCheckRequired());
        legacyPd.setActivationRequired(pd.isActivationRequired());
        legacyPd.setPhoneNumber(pd.getPhoneNumber());
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.exemptionCode = pd.getExemptionCode();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.ecp = pd.getEds();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV23(PdV23 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(false);

        return legacyPd;
    }

    private PD toLegacyPdV24(PdV24 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setRestoredTicket(true);

        return legacyPd;
    }

    private PD toLegacyPdV25(PdV25 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.numberPD = pd.getOrderNumber();
        legacyPd.actionDays = 0xffffffffL & pd.getForDays();
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.tariffCodePD = pd.getTariffCode();
        legacyPd.exemptionCode = pd.getExemptionCode();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();
        legacyPd.setIssBankPaymentType(pd.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD);
        legacyPd.term = pd.getStartDayOffset();
        legacyPd.wayType = TicketWayType.TwoWay;
        legacyPd.setRestoredTicket(true);

        return legacyPd;
    }

    private PD toLegacyPdV64(PdV64 pd, @Nullable BscInformation bscInformation, @Nullable PassageMark passageMark) {
        PD legacyPd = new PD(pd.getVersion().getCode(), pd.getSize());
        legacyPd.saleDatetimePD = pd.getSaleDateTime();
        legacyPd.ecpNumberPD = pd.getEdsKeyNumber();

        return legacyPd;
    }

}
