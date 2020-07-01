package ru.ppr.cppk.logic.exemptionChecker.unit;

import java.util.List;

import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;

/**
 * Проверка "Запрет на формление льготного ПД по выбранному тарифному плану"
 *
 * @author Aleksandr Brazhkin
 */
public class ForTariffPlanExemptionChecker {

    private final NsiDaoSession nsiDaoSession;

    public ForTariffPlanExemptionChecker(NsiDaoSession nsiDaoSession) {
        this.nsiDaoSession = nsiDaoSession;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param exemption      Льгота
     * @param tariffPlanCode Код тарифного плана
     * @param nsiVersion     Версия НСИ
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(Exemption exemption, int tariffPlanCode, int nsiVersion) {
        List<Integer> bannedTariffs = nsiDaoSession.getExemptionBannedForTariffPlanDao().getBannedTariffPlanCodes(exemption, nsiVersion);

        for (Integer bannedTariff : bannedTariffs) {
            if (bannedTariff.equals(tariffPlanCode)) {
                return false;
            }
        }
        return true;
    }
}
