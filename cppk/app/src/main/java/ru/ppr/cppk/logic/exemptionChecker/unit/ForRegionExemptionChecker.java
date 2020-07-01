package ru.ppr.cppk.logic.exemptionChecker.unit;

import ru.ppr.nsi.repository.ExemptionRepository;

/**
 * Проверка "Возможность использования льготы в регионе"
 *
 * @author Aleksandr Brazhkin
 */
public class ForRegionExemptionChecker {

    private final ExemptionRepository exemptionRepository;

    public ForRegionExemptionChecker(ExemptionRepository exemptionRepository) {
        this.exemptionRepository = exemptionRepository;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param exemptionCode Код льготы
     * @param regionCode    Код региона
     * @param nsiVersion    Версия НСИ
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(int exemptionCode, int regionCode, int nsiVersion) {
        //http://agile.srvdev.ru/browse/CPPKPP-33942
        return exemptionRepository.isExemptionSupportedInRegion(exemptionCode, regionCode, nsiVersion);
    }
}
