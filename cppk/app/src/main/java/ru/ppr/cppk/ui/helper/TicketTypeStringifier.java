package ru.ppr.cppk.ui.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.cppk.logic.pd.checker.NoMoneyTicketChecker;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.TicketType;

/**
 * Преобразователь TicketType в строку
 *
 * @author Grigoriy Kashka
 */
public class TicketTypeStringifier {

    private final Globals app;

    @Inject
    public TicketTypeStringifier(Globals app) {
        this.app = app;
    }

    /**
     * Получить строку для типа билета
     *
     * @param exemption  - сущность льготы
     * @param ticketType - сущность типа билета
     * @return
     */
    @NonNull
    public String stringify(@Nullable Exemption exemption, TicketType ticketType) {
        String pdDescription;
        if (exemption != null) {
            pdDescription = getPdDescriptionForExemptionPd(new NoMoneyTicketChecker().check(exemption));
        } else {
            pdDescription = ticketType.getShortName(); // текущий тип оформляемого билета
        }
        return pdDescription;
    }

    /**
     * Получить строку для типа билета
     *
     * @param isExemption         - флаг наличия льготы
     * @param price               - сущность Price
     * @param ticketTypeShortName - краткое название типа билета
     * @return
     */
    @NonNull
    public String stringify(boolean isExemption, @NonNull Price price, @Nullable Fee fee, String ticketTypeShortName) {
        String pdDescription;
        if (isExemption) {
            pdDescription = getPdDescriptionForExemptionPd(new NoMoneyTicketChecker().check(price, fee, true));
        } else {
            pdDescription = ticketTypeShortName; // текущий тип оформляемого билета
        }
        return pdDescription;
    }

    private String getPdDescriptionForExemptionPd(boolean isFree) {
        if (isFree) {
            return app.getString(R.string.one_off_without_money); // Разовый безденежный
        }
        return app.getString(R.string.one_off_with_exemption); // Разовый льготный

    }
}
