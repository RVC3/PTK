package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.model.nsi.TicketStorageType;

/**
 * @author Dmitry Nevolin
 */
public class SmartCard implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Внешний номер БСК
     * Только для ПД на БСК
     */
    private String outerNumber;
    /**
     * Номер кристалла БСК
     * Только для ПД на БСК
     */
    private String crystalSerialNumber;
    /**
     * Тип БСК
     * Только для ПД на БСК
     */
    private TicketStorageType type;
    /**
     * Счетчик использования БСК из метки прохода
     * Только для ПД на БСК
     */
    private Integer usageCount;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getOuterNumber() {
        return outerNumber;
    }

    public void setOuterNumber(String outerNumber) {
        this.outerNumber = outerNumber;
    }

    public String getCrystalSerialNumber() {
        return crystalSerialNumber;
    }

    public void setCrystalSerialNumber(String crystalSerialNumber) {
        this.crystalSerialNumber = crystalSerialNumber;
    }

    public TicketStorageType getType() {
        return type;
    }

    public void setType(TicketStorageType type) {
        this.type = type;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

}
