package ru.ppr.cppk.logic.pd;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.cppk.Sounds.BeepPlayer;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.StrategyCheck;
import ru.ppr.cppk.logic.FareTariffChecker;
import ru.ppr.cppk.logic.PDSignChecker;
import ru.ppr.cppk.pd.check.control.StrategyCheckFactory;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.entity.TrainCategoryPrefix;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;

/**
 * Обработчик события контроля ПД на ШК или БСК.
 *
 * @author Dmitry Nevolin
 */
public class PdHandler {

    private final BeepPlayer beepPlayer;
    private final TariffPlanRepository tariffPlanRepository;
    private final TrainCategoryRepository trainCategoryRepository;
    private final FareTariffChecker fareTariffChecker;
    private final PDSignChecker pdSignChecker;
    private final StrategyCheckFactory strategyCheckFactory;
    private final PdVersionChecker pdVersionChecker;

    @Inject
    public PdHandler(@NonNull TariffPlanRepository tariffPlanRepository,
                     @NonNull BeepPlayer beepPlayer,
                     @NonNull TrainCategoryRepository trainCategoryRepository,
                     @NonNull FareTariffChecker fareTariffChecker,
                     @NonNull PDSignChecker pdSignChecker,
                     @NonNull StrategyCheckFactory strategyCheckFactory,
                     @NonNull PdVersionChecker pdVersionChecker) {
        this.tariffPlanRepository = tariffPlanRepository;
        this.beepPlayer = beepPlayer;
        this.trainCategoryRepository = trainCategoryRepository;
        this.fareTariffChecker = fareTariffChecker;
        this.pdSignChecker = pdSignChecker;
        this.strategyCheckFactory = strategyCheckFactory;
        this.pdVersionChecker = pdVersionChecker;
    }

    /**
     * Выполняет обработку считанной информации.
     *
     * @param legacyPdList Список считанных ПД
     */
    public void handle(@NonNull List<PD> legacyPdList) {
        if (legacyPdList.isEmpty()) {
            // Играем неудачный рингтон
            beepPlayer.playFailBeep();
            return;
        }

        // Проверяем ЭЦП
        pdSignChecker.check(legacyPdList);

        // Выполняем валидацию ПД
        for (PD legacyPd : legacyPdList) {
            StrategyCheck strategyCheck = strategyCheckFactory.createStrategyCheck(legacyPd);
            legacyPd.checkRelevance(strategyCheck);
        }

        // Проверяем наличие тарифа доплаты для ПД
        // В будущем: 7/27/2018 Этому коду здесь не место (вызывается при аннулировании ПД)
        for (PD legacyPd : legacyPdList) {
            legacyPd.hasFareTariff = hasFareTariff(legacyPd);
        }

        // Играем удачный рингтон
        beepPlayer.playSuccessBeep();
    }

    /**
     * Проверяет наличие тарифа доплаты для ПД
     *
     * @param legacyPd ПД
     * @return {@code true} если есть тарфи доплаты, {@code false} - иначе
     */
    private boolean hasFareTariff(@NonNull PD legacyPd) {
        // Ищем тариф доплаты в любом случае
        // https://aj.srvdev.ru/browse/CPPKPP-32203
        Tariff tariff = legacyPd.getTariff();
        if (tariff != null) {
            TariffPlan tariffPlan = tariffPlanRepository.load(tariff.getTariffPlanCode(), tariff.getVersionId());
            TrainCategory trainCategory = trainCategoryRepository.load(tariffPlan.getTrainCategoryCode(), tariff.getVersionId());
            PdVersion version = PdVersion.getByCode(legacyPd.versionPD);
            Preconditions.checkNotNull(version);
            if (TrainCategoryPrefix.PASSENGER == trainCategory.prefix || pdVersionChecker.isCombinedCountTripsSeasonTicket(version)) {
                // Но ищем тариф доплаты только для ПД на пассажирский поезд
                // https://aj.srvdev.ru/browse/CPPKPP-32462
                return fareTariffChecker.check(tariff, legacyPd.wayType);
            }
        }
        return false;
    }
}
