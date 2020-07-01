package ru.ppr.cppk.managers.db;

import com.google.common.primitives.Longs;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Fine;
import ru.ppr.nsi.repository.FineRepository;

/**
 * @author Aleksandr Brazhkin
 */
public class NsiDaoSessionUpdatesListener {

    public NsiDaoSessionUpdatesListener(
            NsiDbManager nsiDbManager,
            Holder<NsiDaoSession> nsiDaoSession,
            Holder<LocalDaoSession> localDaoSession,
            Holder<PrivateSettings> privateSettings,
            NsiVersionManager nsiVersionManager,
            PdSaleEnvFactory pdSaleEnvFactory,
            FineRepository fineRepository) {
        nsiDbManager
                .addDaoSessionResetListener(daoSession -> {
                    nsiDaoSession.set(daoSession);

                    pdSaleEnvFactory.reset();

                    // проверяем коды штрафов, см.
                    // http://agile.srvdev.ru/browse/CPPKPP-34711
                    long[] allowedFineCodes = privateSettings.get().getAllowedFineCodes();

                    if (allowedFineCodes != null && allowedFineCodes.length > 0) {
                        int nsiVersion = nsiVersionManager.getCurrentNsiVersionId();

                        List<Long> allowedFineCodesList = Longs.asList(allowedFineCodes);
                        List<Fine> actualFineList = fineRepository.loadAll(nsiVersion);
                        List<Long> actualFineCodesList = new ArrayList<>();
                        List<Long> actualAllowedFineCodesList = new ArrayList<>();

                        for (Fine fine : actualFineList) {
                            actualFineCodesList.add(fine.getCode());
                        }

                        for (Long allowedFineCode : allowedFineCodesList) {
                            if (actualFineCodesList.contains(allowedFineCode)) {
                                actualAllowedFineCodesList.add(allowedFineCode);
                            }
                        }

                        privateSettings.get().setAllowedFineCodes(Longs.toArray(actualAllowedFineCodesList));

                        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings.get());
                    }
                });
    }
}
