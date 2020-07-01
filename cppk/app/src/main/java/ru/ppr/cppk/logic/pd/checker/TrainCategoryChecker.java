package ru.ppr.cppk.logic.pd.checker;

import javax.inject.Inject;

import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.entity.TrainCategoryPrefix;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;

/**
 * Контроль по категрии паровоза
 *
 * @author Grigoriy Kashka
 */
public class TrainCategoryChecker {

    private final TrainCategoryRepository trainCategoryRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final PrivateSettings privateSettings;
    private final NsiDaoSession nsiDaoSession;

    @Inject
    public TrainCategoryChecker(TrainCategoryRepository trainCategoryRepository,
                                TariffPlanRepository tariffPlanRepository,
                                PrivateSettings privateSettings,
                                NsiDaoSession nsiDaoSession) {
        this.trainCategoryRepository = trainCategoryRepository;
        this.tariffPlanRepository = tariffPlanRepository;
        this.privateSettings = privateSettings;
        this.nsiDaoSession = nsiDaoSession;
    }

    private boolean check(TrainCategory pdTrainCategory, int nsiVersion) {
        TrainCategoryPrefix currentTrainPrefix = privateSettings.getTrainCategoryPrefix();
        TrainCategory currentTrainCategory = trainCategoryRepository.getTrainCategoryToPrefix(currentTrainPrefix, nsiVersion);
        return currentTrainCategory == null
                || currentTrainCategory.prefix.getCode() <= pdTrainCategory.prefix.getCode();
    }

    public boolean check(Tariff tariff, int nsiVersion) {
        if (tariff.getTariffPlan(tariffPlanRepository) == null)
            return false;
        TrainCategory trainCategory = tariff.getTariffPlan(tariffPlanRepository).getTrainCategory(nsiDaoSession);
        return check(trainCategory, nsiVersion);
    }
}
