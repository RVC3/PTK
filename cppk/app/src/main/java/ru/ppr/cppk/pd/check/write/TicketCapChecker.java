package ru.ppr.cppk.pd.check.write;

import java.util.Date;

import ru.ppr.cppk.dataCarrier.entity.PD;

/**
 * Created by Артем on 01.03.2016.
 */
public class TicketCapChecker implements Checker {
    @Override
    public boolean performCheck(PD pd, Date date) {
        // заглушка всегда невалидна
        return false;
    }
}
