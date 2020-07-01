package ru.ppr.cppk.pd.check.write;

import java.util.Date;

import ru.ppr.cppk.dataCarrier.entity.PD;

/**
 * Заглушка, для вроверки неизвестных типов ПД.
 * Всегда говорит что билет валиден
 * Created by Артем on 16.02.2016.
 */
class StubChecker implements Checker {
    @Override
    public boolean performCheck(PD pd, Date date) {
        return true;
    }
}
