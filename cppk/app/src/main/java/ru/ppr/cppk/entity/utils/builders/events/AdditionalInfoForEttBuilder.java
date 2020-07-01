package ru.ppr.cppk.entity.utils.builders.events;

import com.google.common.base.Preconditions;

import java.util.Date;

import ru.ppr.cppk.dataCarrier.entity.ETTData;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.core.logic.FioFormatter;

/**
 * Created by Александр on 09.08.2016.
 */
public class AdditionalInfoForEttBuilder {

    private final FioFormatter fioFormatter;
    private Date cardIssueDataTime;
    private ETTData ettData;

    public AdditionalInfoForEttBuilder(FioFormatter fioFormatter) {
        this.fioFormatter = fioFormatter;
    }

    public AdditionalInfoForEttBuilder setCardIssueDataTime(Date cardIssueDataTime) {
        this.cardIssueDataTime = cardIssueDataTime;
        return this;
    }

    public AdditionalInfoForEttBuilder setEttData(ETTData ettData) {
        this.ettData = ettData;
        return this;
    }

    public AdditionalInfoForEtt build() {
        Preconditions.checkNotNull(ettData, "ettData is null");

        AdditionalInfoForEtt additionalInfoForEtt = new AdditionalInfoForEtt();
        additionalInfoForEtt.setIssueDateTime(cardIssueDataTime);
        additionalInfoForEtt.setGuardianFio(ettData.getWorkerInitials());
        additionalInfoForEtt.setIssueUnitCode(ettData.getDivisionCode());
        additionalInfoForEtt.setOwnerOrganizationCode(ettData.getOrganizationCode());
        additionalInfoForEtt.setPassengerCategory(ettData.getPassengerCategoryCipher());
        additionalInfoForEtt.setPassengerFio(fioFormatter.getFullNameAsSurnameWithInitials(ettData.getSurname(), ettData.getFirstName(), ettData.getSecondName()));
        additionalInfoForEtt.setSnils(ettData.getSNILSCode());

        return additionalInfoForEtt;
    }
}
