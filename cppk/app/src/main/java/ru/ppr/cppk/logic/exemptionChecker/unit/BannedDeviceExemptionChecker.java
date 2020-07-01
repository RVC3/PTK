package ru.ppr.cppk.logic.exemptionChecker.unit;

import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.ExemptionRepository;

/**
 * Проверка "Возможность использования льготы на ПТК"
 *
 * @author Aleksandr Brazhkin
 */
public class BannedDeviceExemptionChecker {

    private final ExemptionRepository exemptionRepository;

    public BannedDeviceExemptionChecker(ExemptionRepository exemptionRepository) {
        this.exemptionRepository = exemptionRepository;
    }

    /**
     * Выполняет проверку льготы.
     *
     * @param exemption     Льгта
     * @param regionCode    Код региона
     * @param trainCategory Категория поезда
     * @param nsiVersion    Версия НСИ
     * @return {@code true} - проверка пройдена успешно, {@code false} - иначе
     */
    public boolean check(Exemption exemption, int regionCode, TrainCategory trainCategory, int nsiVersion) {
        return !exemptionRepository.isBannedDeviceForExemption(exemption, regionCode, trainCategory, nsiVersion);
    }
}
