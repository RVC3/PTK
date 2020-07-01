package ru.ppr.cppk.pd.check.write;

import java.util.Date;

import ru.ppr.cppk.dataCarrier.entity.PD;

/**
 * Created by Артем on 16.02.2016.
 */
public interface Checker {
    /**
     * Выполняет проверку действительности билета
     *
     * @param pd   билет, который необходимо проверить
     * @param date дата, на которую происходит проверка
     * @return true билет валиден, иначе false
     */
    boolean performCheck(PD pd, Date date);
}
