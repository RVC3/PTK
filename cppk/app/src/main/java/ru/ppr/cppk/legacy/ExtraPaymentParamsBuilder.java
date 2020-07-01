package ru.ppr.cppk.legacy;

import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.entity.utils.builders.events.AdditionalInfoForEttBuilder;
import ru.ppr.cppk.model.ExtraPaymentParams;
import ru.ppr.cppk.model.SmartCardId;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * @author Aleksandr Brazhkin
 */
public class ExtraPaymentParamsBuilder {

    public static ExtraPaymentParams from(PD pd) {
        ExtraPaymentParams extraPaymentParams = new ExtraPaymentParams();
        extraPaymentParams.setParentPdNumber(pd.numberPD);
        extraPaymentParams.setParentPdSaleDateTime(pd.saleDatetimePD);
        extraPaymentParams.setParentPdTariffCode(pd.tariffCodePD);
        extraPaymentParams.setParentPdDirectionCode(pd.wayType.getCode());
        extraPaymentParams.setParentPdDeviceId(pd.deviceId);
        extraPaymentParams.setParentPdExemptionExpressCode(pd.exemptionCode);

        if (pd.getBscInformation() != null) {
            SmartCardId smartCardId = new SmartCardId();
            smartCardId.setTicketStorageTypeCode(pd.getBscInformation().getSmartCardTypeBsc().getDBCode());
            smartCardId.setCrystalSerialNumber(pd.getBscInformation().getCrustalSerialNumberString());
            smartCardId.setOuterNumber(pd.getBscInformation().getFormattedOuterNumber().replace(" ", ""));
            extraPaymentParams.setSmartCardId(smartCardId);

            if (pd.getBscInformation().getSmartCardTypeBsc() == TicketStorageType.ETT) {
                AdditionalInfoForEtt additionalInfoForEtt = new AdditionalInfoForEttBuilder(Dagger.appComponent().fioFormatter())
                        .setCardIssueDataTime(pd.getBscInformation().getInitDate())
                        .setEttData(pd.getBscInformation().getEttData())
                        .build();
                extraPaymentParams.setAdditionalInfoForEtt(additionalInfoForEtt);
            } else {
                extraPaymentParams.setAdditionalInfoForEtt(null);
            }
        }
        return extraPaymentParams;
    }
}
