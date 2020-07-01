package ru.ppr.nsi.query.stationswithtariffs;

import android.support.annotation.Nullable;

import java.util.Collection;

/**
 * Параметры для запроса тарифов.
 *
 * @author Aleksandr Brazhkin
 */
public class Params {
    /**
     * Ограничивающий список кодов станций отправления
     */
    @Nullable
    private final Collection<Long> allowedDepStationCodes;
    /**
     * Ограничивающий список кодов станций назначения
     */
    @Nullable
    private final Collection<Long> allowedDestStationCodes;
    /**
     * Список запрещеных кодов станций отправления
     */
    @Nullable
    private final Collection<Long> deniedDepStationCodes;
    /**
     * Список запрещеных кодов станций назначения
     */
    @Nullable
    private final Collection<Long> deniedDestStationCodes;
    /**
     * Ограничивающий список кодов тарифных планов
     */
    @Nullable
    private final Collection<Long> allowedTariffPlanCodes;
    /**
     * Ограничивающий список типов билетов
     */
    @Nullable
    private final Collection<Long> allowedTicketTypeCodes;
    /**
     * Версия НСИ
     */
    private final int versionId;

    public Params(@Nullable Collection<Long> allowedDepStationCodes,
                  @Nullable Collection<Long> allowedDestStationCodes,
                  @Nullable Collection<Long> deniedDepStationCodes,
                  @Nullable Collection<Long> deniedDestStationCodes,
                  @Nullable Collection<Long> allowedTariffPlanCodes,
                  @Nullable Collection<Long> allowedTicketTypeCodes,
                  int versionId) {
        this.allowedDepStationCodes = allowedDepStationCodes;
        this.allowedDestStationCodes = allowedDestStationCodes;
        this.deniedDepStationCodes = deniedDepStationCodes;
        this.deniedDestStationCodes = deniedDestStationCodes;
        this.allowedTariffPlanCodes = allowedTariffPlanCodes;
        this.allowedTicketTypeCodes = allowedTicketTypeCodes;
        this.versionId = versionId;
    }

    @Nullable
    public Collection<Long> getAllowedDepStationCodes() {
        return allowedDepStationCodes;
    }

    @Nullable
    public Collection<Long> getAllowedDestStationCodes() {
        return allowedDestStationCodes;
    }

    @Nullable
    public Collection<Long> getDeniedDepStationCodes() {
        return deniedDepStationCodes;
    }

    @Nullable
    public Collection<Long> getDeniedDestStationCodes() {
        return deniedDestStationCodes;
    }

    @Nullable
    public Collection<Long> getAllowedTariffPlanCodes() {
        return allowedTariffPlanCodes;
    }

    @Nullable
    public Collection<Long> getAllowedTicketTypeCodes() {
        return allowedTicketTypeCodes;
    }

    public int getVersionId() {
        return versionId;
    }
}
