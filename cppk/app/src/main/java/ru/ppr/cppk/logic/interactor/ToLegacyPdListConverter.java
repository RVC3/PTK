package ru.ppr.cppk.logic.interactor;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.cppk.dataCarrier.PdToLegacyMapper;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.entity.PassageMark;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.repository.TariffRepository;

/**
 * @author Aleksandr Brazhkin
 */
public class ToLegacyPdListConverter {

    private final TariffRepository tariffRepository;
    private final NsiVersionManager nsiVersionManager;

    public ToLegacyPdListConverter() {
        tariffRepository = Dagger.appComponent().tariffRepository();
        nsiVersionManager = Dagger.appComponent().nsiVersionManager();
    }

    @NonNull
    public List<PD> convert(@NonNull List<Pd> pdList, BscInformation bscInformation, PassageMark passageMark) {
        List<PD> legacyPdList = new ArrayList<>();

        for (int i = 0; i < pdList.size(); i++) {
            Pd pd = pdList.get(i);
            if (pd != null) {
                PD legacyPd = new PdToLegacyMapper().toLegacyPd(pd, bscInformation, passageMark);
                if (legacyPd != null) {
                    legacyPd.orderNumberPdOnCard = i;
                    legacyPd.setBscInformation(bscInformation);
                    legacyPd.setPassageMark(passageMark);
                    if (legacyPd.versionPD != PdVersion.V64.getCode()) {
                        int nsiVersion = nsiVersionManager.getNsiVersionIdForDate(legacyPd.getSaleDate());
                        legacyPd.setTariff(tariffRepository.getTariffToCodeIgnoreDeleteFlag(legacyPd.tariffCodePD, nsiVersion));
                    }
                    legacyPdList.add(legacyPd);
                }
            }
        }

        return legacyPdList;
    }
}
