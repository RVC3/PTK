package ru.ppr.cppk.logic.exemptionChecker.unit;

import java.util.Calendar;
import java.util.Date;

import ru.ppr.nsi.entity.Exemption;

/**
 * Проверка "Срок действия льготы"
 *
 * @author Aleksandr Brazhkin
 */
public class ValidityPeriodExemptionChecker {

    /**
     * Выполняет проверку льготы.
     *
     * @param exemption   Льгота
     * @param pdStartDate Дата начала действия ПД
     * @param pdEndDate   Дата окончания действия ПД
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(Exemption exemption, Date pdStartDate, Date pdEndDate) {

        //проверим корректность дат начала и окончания действия ПД
        if (pdStartDate.after(pdEndDate))
            return false;

        //проверим даты начала действия льготы и пд, дата начала действия ПД должа быть позже
        if (exemption.getActiveFromDate() != null && exemption.getActiveFromDate().after(pdStartDate))
            return false;

        //проверим даты окончания действия ПД и льготы, дата окончания действия ПД должна быть раньше
        //http://agile.srvdev.ru/browse/CPPKPP-33826
        boolean tillCheckResult = exemption.getActiveTillDate() == null;
        if (!tillCheckResult) {

            final Calendar exemptionTillTime = Calendar.getInstance();
            exemptionTillTime.setTime(exemption.getActiveTillDate());
            exemptionTillTime.set(Calendar.HOUR_OF_DAY, 0);
            exemptionTillTime.set(Calendar.MINUTE, 0);
            exemptionTillTime.set(Calendar.SECOND, 0);
            exemptionTillTime.set(Calendar.MILLISECOND, 0);

            final Calendar pdEndTime = Calendar.getInstance();
            pdEndTime.setTime(pdEndDate);
            pdEndTime.set(Calendar.HOUR_OF_DAY, 0);
            pdEndTime.set(Calendar.MINUTE, 0);
            pdEndTime.set(Calendar.SECOND, 0);
            pdEndTime.set(Calendar.MILLISECOND, 0);

            // >=0 если левая дата больше или равна правой
            tillCheckResult = exemptionTillTime.getTime().compareTo(pdEndTime.getTime()) >= 0;
        }

        return tillCheckResult;

    }
}
