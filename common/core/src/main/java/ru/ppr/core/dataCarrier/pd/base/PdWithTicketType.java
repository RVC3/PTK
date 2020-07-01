package ru.ppr.core.dataCarrier.pd.base;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ПД с типом билета.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithTicketType extends Pd {

    /**
     * Тип билета
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TICKET_TYPE_FULL,
            TICKET_TYPE_WITH_EXEMPTION})
    @interface TicketType {
    }

    int TICKET_TYPE_FULL = 1;
    int TICKET_TYPE_WITH_EXEMPTION = 2;

    /**
     * Возращает тип билета.
     *
     * @return Тип билета
     */
    @PdWithTicketType.TicketType
    int getTicketType();
}
