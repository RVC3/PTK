package ru.ppr.nsi.entity;

import java.util.Date;

import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.ExemptionGroupRepository;

/**
 * Льгота.
 */
public class Exemption {

    public Exemption() {

    }

    /**
     * Собственно код льготы
     */
    private Integer exemptionExpressCode = null;
    private Integer newExemptionExpressCode = null;
    private String regionOkatoCode = null;
    private String exemptionOrganizationCode = null;
    private Integer percentage = null;
    private boolean childTicketAvailable;
    private boolean massRegistryAvailable;
    private boolean cppkRegistryBan;
    /**
     * Признак возможности предварительной продажи льготного ПД на 7000 с местом.
     */
    private boolean presale7000WithPlace;
    /**
     * Признак возможности предварительной продажи льготного разового ПД на 6000.
     */
    private boolean presale6000Once;
    /**
     * Признак возможности предварительной продажи льготного абонементного ПД на 6000.
     */
    private boolean presale6000Abonement;
    /**
     * Признак взимания сбора за оформление льготного ПД на ПТК
     */
    private boolean leavy;
    private boolean requireSnilsNumber;
    private boolean requireSocialCard;
    private boolean notRequireFIO;
    private boolean notRequireDocumentNumber;
    private boolean isRegionOnly;
    private boolean isTakeProcessingFee = true;
    private Date activeFromDate = null;
    private Date activeTillDate = null;
    private Integer exemptionGroupCode = null;
    private String name = null;
    private Integer code = null; //id
    private byte[] dataChecksum = null;
    // public Integer deleteInVersionId = null;
    private Integer versionId = null;
    private ExemptionGroup exemptionGroup = null;
    private ExemptionOrganization exemptionOrganization = null;


    public ExemptionGroup getExemptionGroup(ExemptionGroupRepository exemptionGroupRepository, int nsiVersion) {
        if (exemptionGroup == null && exemptionGroupCode != null) {
            exemptionGroup = exemptionGroupRepository.load(exemptionGroupCode, nsiVersion);
        }
        return exemptionGroup;
    }

    public ExemptionOrganization getExemptionOrganization(NsiDaoSession nsiDaoSession, int nsiVersion) {
        if (exemptionOrganization == null && exemptionOrganizationCode != null) {
            exemptionOrganization = nsiDaoSession.getExemptionOrganizationDao().load(
                    exemptionOrganizationCode, nsiVersion);
        }
        return exemptionOrganization;
    }

    /**
     * Собственно код льготы
     */
    public Integer getExemptionExpressCode() {
        return exemptionExpressCode;
    }

    public void setExemptionExpressCode(Integer exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }

    public Integer getNewExemptionExpressCode() {
        return newExemptionExpressCode;
    }

    public void setNewExemptionExpressCode(Integer newExemptionExpressCode) {
        this.newExemptionExpressCode = newExemptionExpressCode;
    }

    public String getRegionOkatoCode() {
        return regionOkatoCode;
    }

    public void setRegionOkatoCode(String regionOkatoCode) {
        this.regionOkatoCode = regionOkatoCode;
    }

    public String getExemptionOrganizationCode() {
        return exemptionOrganizationCode;
    }

    public void setExemptionOrganizationCode(String exemptionOrganizationCode) {
        this.exemptionOrganizationCode = exemptionOrganizationCode;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public boolean isChildTicketAvailable() {
        return childTicketAvailable;
    }

    public void setChildTicketAvailable(boolean childTicketAvailable) {
        this.childTicketAvailable = childTicketAvailable;
    }

    public boolean isMassRegistryAvailable() {
        return massRegistryAvailable;
    }

    public void setMassRegistryAvailable(boolean massRegistryAvailable) {
        this.massRegistryAvailable = massRegistryAvailable;
    }

    public boolean isCppkRegistryBan() {
        return cppkRegistryBan;
    }

    public void setCppkRegistryBan(boolean cppkRegistryBan) {
        this.cppkRegistryBan = cppkRegistryBan;
    }

    public boolean isPresale7000WithPlace() {
        return presale7000WithPlace;
    }

    public void setPresale7000WithPlace(boolean presale7000WithPlace) {
        this.presale7000WithPlace = presale7000WithPlace;
    }

    public boolean isPresale6000Once() {
        return presale6000Once;
    }

    public void setPresale6000Once(boolean presale6000Once) {
        this.presale6000Once = presale6000Once;
    }

    public boolean isPresale6000Abonement() {
        return presale6000Abonement;
    }

    public void setPresale6000Abonement(boolean presale6000Abonement) {
        this.presale6000Abonement = presale6000Abonement;
    }

    /**
     * Признак взимания сбора за оформление льготного ПД на ПТК. true - взимать
     */
    public boolean isLeavy() {
        return leavy;
    }

    /**
     * Признак взимания сбора за оформление льготного ПД на ПТК. true - взимать
     */
    public void setLeavy(boolean leavy) {
        this.leavy = leavy;
    }

    public boolean isRequireSnilsNumber() {
        return requireSnilsNumber;
    }

    public void setRequireSnilsNumber(boolean requireSnilsNumber) {
        this.requireSnilsNumber = requireSnilsNumber;
    }

    public boolean isRequireSocialCard() {
        return requireSocialCard;
    }

    public void setRequireSocialCard(boolean requireSocialCard) {
        this.requireSocialCard = requireSocialCard;
    }

    public boolean isTakeProcessingFee() {
        return isTakeProcessingFee;
    }

    public void setIsTakeProcessingFee(boolean isTakeProcessingFee) {
        this.isTakeProcessingFee = isTakeProcessingFee;
    }

    public boolean isRegionOnly() {
        return isRegionOnly;
    }

    public void setRegionOnly(boolean isRegionOnly) {
        this.isRegionOnly = isRegionOnly;
    }

    public Date getActiveTillDate() {
        return activeTillDate;
    }

    public void setActiveTillDate(Date activeTillDate) {
        this.activeTillDate = activeTillDate;
    }

    public Date getActiveFromDate() {
        return activeFromDate;
    }

    public void setActiveFromDate(Date activeFromDate) {
        this.activeFromDate = activeFromDate;
    }

    public Integer getExemptionGroupCode() {
        return exemptionGroupCode;
    }

    public void setExemptionGroupCode(Integer exemptionGroupCode) {
        this.exemptionGroupCode = exemptionGroupCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public byte[] getDataChecksum() {
        return dataChecksum;
    }

    public void setDataChecksum(byte[] dataChecksum) {
        this.dataChecksum = dataChecksum;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public boolean isNotRequireFIO() {
        return notRequireFIO;
    }

    public void setNotRequireFIO(boolean notRequireFIO) {
        this.notRequireFIO = notRequireFIO;
    }

    public boolean isNotRequireDocumentNumber() {
        return notRequireDocumentNumber;
    }

    public void setNotRequireDocumentNumber(boolean notRequireDocumentNumber) {
        this.notRequireDocumentNumber = notRequireDocumentNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Exemption exemption = (Exemption) o;

        return exemptionExpressCode.equals(exemption.exemptionExpressCode);

    }

    @Override
    public int hashCode() {
        return exemptionExpressCode.hashCode();
    }
}
