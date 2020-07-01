package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.EnumSet;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;

/**
 * Проверка на аннулированный ПД
 *
 * @author Grigoriy Kashka
 */
public class RevokedPdChecker {

    private final LocalDaoSession mLocalDaoSession;

    @Inject
    public RevokedPdChecker(LocalDaoSession mLocalDaoSession) {
        this.mLocalDaoSession = mLocalDaoSession;
    }

    public boolean check(int pdNumber, long ecpNumber, @NonNull Date saleDateTime) {
        CPPKTicketSales cppkTicketSales = mLocalDaoSession.getCppkTicketSaleDao().findSaleByParam(pdNumber, ecpNumber, saleDateTime);
        if (cppkTicketSales != null) {
            // http://agile.srvdev.ru/browse/CPPKPP-39874
            //
            // Рзянкина Наталья Владимировна​:
            // Аннулированным ПД считается только тогда, когда он прошел по фискальнику
            // Даже если уже отменена банковская, т.е. вернули деньги - это ничего не значит
            // Такая логика действует везде: и при аннулировании, и при контроле ПД
            EnumSet<ProgressStatus> statuses = EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed);
            CPPKTicketReturn cppkTicketReturn = mLocalDaoSession.getCppkTicketReturnDao().findLastPdRepealEventForPdSaleEvent(cppkTicketSales.getId(), statuses);
            if (cppkTicketReturn != null) {
                return false;
            }
        }
        return true;
    }
}
