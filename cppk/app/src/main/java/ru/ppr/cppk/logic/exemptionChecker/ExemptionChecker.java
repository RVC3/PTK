package ru.ppr.cppk.logic.exemptionChecker;

import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.helpers.TicketCategoryChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.BannedDeviceExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.BeneficiaryCategoryExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.CppkRegistryBanExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.ExpressCodeExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.ExtraSaleForSeasonTicketOnEttChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.ForChildExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.ForRegionExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.ForTariffPlanExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.ManualInputExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.RepeatedSaleExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.SmartCardIdExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.SocialCardExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.TicketStorageTypeExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.TicketTypeTrainCategoryExemptionChecker;
import ru.ppr.cppk.logic.exemptionChecker.unit.ValidityPeriodExemptionChecker;
import ru.ppr.cppk.model.SmartCardId;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.ProhibitedTicketTypeForExemptionCategoryRepository;

/**
 * Класс, выполняющий проверку льготы при считывании с карты или ручном вводе.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionChecker {

    private final LocalDaoSession mLocalDaoSession;
    private final NsiDaoSession mNsiDaoSession;
    private final TicketCategoryChecker mTicketCategoryChecker;
    private final ProhibitedTicketTypeForExemptionCategoryRepository prohibitedTicketTypeForExemptionCategoryRepository;
    private final ExemptionRepository exemptionRepository;

    public ExemptionChecker(LocalDaoSession localDaoSession,
                            NsiDaoSession nsiDaoSession,
                            TicketCategoryChecker ticketCategoryChecker,
                            ProhibitedTicketTypeForExemptionCategoryRepository prohibitedTicketTypeForExemptionCategoryRepository,
                            ExemptionRepository exemptionRepository) {
        mLocalDaoSession = localDaoSession;
        mNsiDaoSession = nsiDaoSession;
        mTicketCategoryChecker = ticketCategoryChecker;
        this.prohibitedTicketTypeForExemptionCategoryRepository = prohibitedTicketTypeForExemptionCategoryRepository;
        this.exemptionRepository = exemptionRepository;
    }

    public CheckResult check(@NonNull SelectExemptionParams selectExemptionParams, @NonNull ExemptionForEvent exemptionForEvent, Exemption exemption, int regionCode, String ettPassengerCategory) {

        int nsiVersion = selectExemptionParams.getVersionNsi();
        int ticketTypeCode = selectExemptionParams.getTicketTypeCode();
        int exemptionCode = exemption.getCode();
        TrainCategory trainCategory = selectExemptionParams.getTrainCategory();

        SmartCard smartCard = exemptionForEvent.getSmartCardFromWhichWasReadAboutExemption();
        SmartCardId smartCardId;
        if (smartCard == null) {
            smartCardId = null;
        } else {
            smartCardId = new SmartCardId();
            smartCardId.setTicketStorageTypeCode(smartCard.getType().getDBCode());
            smartCardId.setCrystalSerialNumber(smartCard.getCrystalSerialNumber());
            smartCardId.setOuterNumber(smartCard.getOuterNumber());
        }

        TicketStorageType ticketStorageType = exemptionForEvent.isManualInput() ? TicketStorageType.Paper : smartCard.getType();

        if (!new ExpressCodeExemptionChecker().check(exemption.getExemptionExpressCode(), selectExemptionParams.getExceptedExpressCode())) {
            return CheckResult.INVALID_EXPRESS_CODE;
        }
        if (!new SmartCardIdExemptionChecker().check(smartCardId, selectExemptionParams.getExceptedSmartCardId())) {
            return CheckResult.INVALID_CARD_ID;
        }
        if (!new CppkRegistryBanExemptionChecker().check(exemption)) {
            return CheckResult.IN_CPPK_REGISTRY_BAN;
        }
        if (!new ValidityPeriodExemptionChecker().check(exemption, selectExemptionParams.getPdStartDateTime(), selectExemptionParams.getPdEndDateTime())) {
            return CheckResult.INVALID_PERIOD;
        }
        if (!new ForRegionExemptionChecker(exemptionRepository).check(exemptionCode, regionCode, nsiVersion)) {
            return CheckResult.DENIED_FOR_REGION;
        }
        if (!new BannedDeviceExemptionChecker(exemptionRepository).check(exemption, regionCode, trainCategory, nsiVersion)) {
            return CheckResult.BANNED_DEVICE;
        }
        if (!new ForTariffPlanExemptionChecker(mNsiDaoSession).check(exemption, selectExemptionParams.getTariffPlanCode(), nsiVersion)) {
            return CheckResult.DENIED_FOR_TARIFF_PLAN;
        }
        if (!new ManualInputExemptionChecker(mNsiDaoSession).check(exemptionForEvent, exemption, regionCode, nsiVersion)) {
            return CheckResult.DENIED_FOR_MANUAL_INPUT;
        }
        if (!new SocialCardExemptionChecker().check(exemptionForEvent, exemption)) {
            return CheckResult.SOCIAL_CARD_REQUIRED;
        }
        if (!new TicketStorageTypeExemptionChecker(mNsiDaoSession).check(ticketStorageType, exemption, nsiVersion)) {
            return CheckResult.DENIED_FOR_TICKET_STORAGE_TYPE;
        }
        if (!new TicketTypeTrainCategoryExemptionChecker(mNsiDaoSession).check(exemptionCode, ticketTypeCode, trainCategory.code, nsiVersion)) {
            return CheckResult.DENIED_FOR_TICKET_TYPE;
        }
        if (!new ForChildExemptionChecker().check(exemption, ticketTypeCode)) {
            return CheckResult.DENIED_FOR_CHILD_TICKET;
        }
        if (!new RepeatedSaleExemptionChecker(mLocalDaoSession, mNsiDaoSession).check(exemptionForEvent, exemptionCode, ticketTypeCode, trainCategory.code, nsiVersion)) {
            return CheckResult.DENIED_FOR_REPEATED_SALE;
        }
        if (!new BeneficiaryCategoryExemptionChecker(prohibitedTicketTypeForExemptionCategoryRepository).check(ticketStorageType, ticketTypeCode, ettPassengerCategory, nsiVersion)) {
            return CheckResult.DENIED_FOR_BENEFICIARY_CATEGORY;
        }
        if (!new ExtraSaleForSeasonTicketOnEttChecker(mTicketCategoryChecker).check(ticketStorageType, selectExemptionParams.getParentPdTicketCategoryCode())) {
            return CheckResult.DENIED_FOR_EXTRA_SALE_FOR_SEASON_TICKET_ON_ETT;
        }
        return CheckResult.SUCCESS;
    }

    /**
     * Результаты выполнения проверки
     */
    public enum CheckResult {
        SUCCESS,
        INVALID_EXPRESS_CODE,
        INVALID_CARD_ID,
        IN_CPPK_REGISTRY_BAN,
        INVALID_PERIOD,
        DENIED_FOR_REGION,
        BANNED_DEVICE,
        DENIED_FOR_TARIFF_PLAN,
        DENIED_FOR_MANUAL_INPUT,
        SOCIAL_CARD_REQUIRED,
        DENIED_FOR_TICKET_STORAGE_TYPE,
        DENIED_FOR_TICKET_TYPE,
        DENIED_FOR_CHILD_TICKET,
        DENIED_FOR_REPEATED_SALE,
        DENIED_FOR_BENEFICIARY_CATEGORY,
        DENIED_FOR_EXTRA_SALE_FOR_SEASON_TICKET_ON_ETT
    }
}