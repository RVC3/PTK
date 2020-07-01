package ru.ppr.cppk.pd.utils;

import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.ui.fragment.pd.countrips.CountTripsFragment;
import ru.ppr.cppk.ui.fragment.pd.countrips.CountTripsPdActivityLogic;
import ru.ppr.cppk.ui.fragment.pd.invalid.ErrorFragment;
import ru.ppr.cppk.ui.fragment.pd.invalid.ErrorFragment.Errors;
import ru.ppr.cppk.ui.fragment.pd.pdwithplace.PdWithPlaceActivityLogic;
import ru.ppr.cppk.ui.fragment.pd.pdwithplace.PdWithPlaceFragment;
import ru.ppr.cppk.ui.fragment.pd.servicefee.ServiceFeePdFragment;
import ru.ppr.cppk.ui.fragment.pd.simple.SimplePdActivityLogic;
import ru.ppr.cppk.ui.fragment.pd.simple.SimplePdFragment;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketType;

public class PdFragmentCreator {

    /**
     * Создает фрагмент для отображения билета
     *
     * @param pd       билет
     * @param variants варинаты валидности билетов, может быть null если hideButton == true
     * @param isNewPd  флаг нового пд, т.е. который записали на птк
     * @return
     */
    @NonNull
    public static Fragment createFragment(Activity activity,
                                          PD pd,
                                          ValidityPdVariants variants,
                                          boolean isNewPd,
                                          boolean fromControl,
                                          int pdIndex,
                                          boolean enableZoom,
                                          boolean transferSaleButtonCanBeShown,
                                          boolean transfer,
                                          boolean pdWithPlace,
                                          SimplePdActivityLogic.Callback simplePdFragmentCallback,
                                          CountTripsPdActivityLogic.Callback countTripsFragmentCallback,
                                          ServiceFeePdFragment.Callback serviceFeePdFragmentCallback) {
        Logger.info(PdFragmentCreator.class, "createFragment() Создаем фрагмент для отображения считанного ПД...");

        Fragment fragment;
        if (pdWithPlace) {
            PdWithPlaceFragment pdWithPlaceFragment = PdWithPlaceFragment.newInstance();
            new PdWithPlaceActivityLogic(activity, pdWithPlaceFragment);
            fragment = pdWithPlaceFragment;
        } else {
            if (pd == null) {
                return ErrorFragment.newInstance(variants, Errors.OTHER, fromControl);
            }

            // При считывании ПД после записи на БСК (isNewPd = true) нет проверки ЭЦП
            if (!isNewPd && !Dagger.appComponent().deviceIdChecker().isDeviceIdValid(pd.deviceId)) {
                return ErrorFragment.newInstance(variants, Errors.INCORRECT_DEVICE_ID, fromControl);
            }

            PdVersion pdVersion = PdVersion.getByCode(pd.versionPD);

            Logger.info(PdFragmentCreator.class, "createFragment() Версия считанного ПД: " + pd.versionPD);

            if (pdVersion == null) {
                return ErrorFragment.newInstance(variants, Errors.OTHER, fromControl);
            }

            // в ПД версии 21 в принципе нет тарифа, это ПД с услугами
            if (pd.getTariff() == null && pdVersion != PdVersion.V21) {
                return ErrorFragment.newInstance(variants, Errors.NO_TARIFF, fromControl);
            }

            // Защищаемся от краша, если в ПД вдруг окажется тариф на багаж
            Tariff tariff = pd.getTariff();
            if (tariff != null) {
                TicketType ticketType = Dagger.appComponent().ticketTypeRepository().load(tariff.getTicketTypeCode(), tariff.getVersionId());
                if (ticketType != null) {
                    if (Dagger.appComponent().ticketCategoryChecker().isTrainBaggageTicket(ticketType.getTicketCategoryCode())) {
                        // Если это багаж, отображаем ошибку
                        return ErrorFragment.newInstance(variants, Errors.OTHER, fromControl);
                    }
                }
            }

            if (Dagger.appComponent().pdVersionChecker().isCountTripsSeasonTicket(pdVersion)) {
                CountTripsFragment countTripsFragment = CountTripsFragment.newInstance();
                new CountTripsPdActivityLogic(activity, countTripsFragment, countTripsFragmentCallback, pd, variants);
                return countTripsFragment;
            }

            switch (pdVersion) {
                case V1:
                case V2:
                case V3:
                case V4:
                case V5:
                case V6:
                case V9:
                case V11:
                case V13:
                case V12:
                case V14:
                case V15:
                case V16:
                case V17:
                case V22:
                case V25:
                    SimplePdFragment simplePdFragment = SimplePdFragment.newInstance();
                    new SimplePdActivityLogic(activity, simplePdFragment, simplePdFragmentCallback, pd, variants, isNewPd, fromControl, enableZoom, transferSaleButtonCanBeShown, transfer);
                    fragment = simplePdFragment;
                    break;
                case V21:
                    ServiceFeePdFragment serviceFeePdFragment = ServiceFeePdFragment.newInstance(pd);
                    serviceFeePdFragment.setCallback(serviceFeePdFragmentCallback);
                    fragment = serviceFeePdFragment;
                    break;
                default:
                    fragment = ErrorFragment.newInstance(variants, Errors.OTHER, fromControl);
                    break;
            }
        }
        return fragment;
    }
}