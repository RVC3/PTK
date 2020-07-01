package ru.ppr.cppk.logic;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithoutPlace;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.TariffRepository;

/**
 * Класс, осуществляющий проверку является ли ПД автобусным билетам
 *
 * @author Dmitry Nevolin
 */
public class TransferPdChecker {

    private static final String TAG = Logger.makeLogTag(TransferPdChecker.class);

    private final NsiDaoSession nsiDaoSession;
    private final TicketCategoryChecker ticketCategoryChecker;
    private final TariffRepository tariffRepository;
    private final NsiVersionManager nsiVersionManager;

    @Inject
    public TransferPdChecker(NsiDaoSession nsiDaoSession,
                             TicketCategoryChecker ticketCategoryChecker,
                             TariffRepository tariffRepository,
                             NsiVersionManager nsiVersionManager) {
        this.nsiDaoSession = nsiDaoSession;
        this.ticketCategoryChecker = ticketCategoryChecker;
        this.tariffRepository = tariffRepository;
        this.nsiVersionManager = nsiVersionManager;
    }

    /**
     * Метод, осуществляющий проверку является ли ПД автобусным билетам
     *
     * @param pd ПД для проверки
     * @return true если является, false в противном случае
     */
    public boolean check(@NonNull Pd pd) {
        if (!(pd instanceof PdWithoutPlace)) {
            return false;
        }

        Tariff tariff = tariffRepository.getTariffToCodeIgnoreDeleteFlag(((PdWithoutPlace) pd).getTariffCode(), nsiVersionManager.getNsiVersionIdForDate(pd.getSaleDateTime()));

        if (tariff == null) {
            return false;
        }

        TicketType ticketType = tariff.getTicketType(nsiDaoSession);

        if (ticketType == null) {
            return false;
        }

        TicketCategory ticketCategory = ticketType.getTicketCategory(nsiDaoSession);

        if (ticketCategory != null) {
            Logger.info(TAG, "ticketCategory: " + ticketCategory.getCode());
        } else {
            Logger.info(TAG, "ticketCategory: null");
        }

        return ticketCategory != null && ticketCategoryChecker.isTransferTicket(ticketCategory.getCode());
    }

}
