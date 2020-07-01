package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.core.logic.FioFormatter;
import ru.ppr.cppk.dataCarrier.entity.ETTData;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;

/**
 * Класс, выполняющий сборку {@link AdditionalInfoForEtt} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class AdditionalInfoForEttCreator {

    private final FioFormatter fioFormatter;
    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;

    private Date cardIssueDataTime;
    private ETTData ettData;

    @Inject
    AdditionalInfoForEttCreator(FioFormatter fioFormatter,
                                LocalDaoSession localDaoSession,
                                LocalDbTransaction localDbTransaction) {
        this.fioFormatter = fioFormatter;
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
    }

    public AdditionalInfoForEttCreator setCardIssueDataTime(Date cardIssueDataTime) {
        this.cardIssueDataTime = cardIssueDataTime;
        return this;
    }

    public AdditionalInfoForEttCreator setEttData(ETTData ettData) {
        this.ettData = ettData;
        return this;
    }

    /**
     * Выполнят сборку {@link AdditionalInfoForEtt} и запись его в БД.
     *
     * @return Сформированный {@link AdditionalInfoForEtt}
     */
    @NonNull
    public AdditionalInfoForEtt create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private AdditionalInfoForEtt createInternal() {
        Preconditions.checkNotNull(ettData);

        AdditionalInfoForEtt additionalInfoForEtt = new AdditionalInfoForEtt();
        additionalInfoForEtt.setIssueDateTime(cardIssueDataTime);
        additionalInfoForEtt.setGuardianFio(ettData.getWorkerInitials());
        additionalInfoForEtt.setIssueUnitCode(ettData.getDivisionCode());
        additionalInfoForEtt.setOwnerOrganizationCode(ettData.getOrganizationCode());
        additionalInfoForEtt.setPassengerCategory(ettData.getPassengerCategoryCipher());
        additionalInfoForEtt.setPassengerFio(fioFormatter.getFullNameAsSurnameWithInitials(ettData.getSurname(), ettData.getFirstName(), ettData.getSecondName()));
        additionalInfoForEtt.setSnils(ettData.getSNILSCode());

        // Пишем в БД AdditionalInfoForEtt
        localDaoSession.getAdditionalInfoForEttDao().insertOrThrow(additionalInfoForEtt);
        return additionalInfoForEtt;
    }
}
