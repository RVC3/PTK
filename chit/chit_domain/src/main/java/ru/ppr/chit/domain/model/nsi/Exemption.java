package ru.ppr.chit.domain.model.nsi;

import java.util.Date;

import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCVD;

/**
 * Льгота.
 *
 * @author Aleksandr Brazhkin
 */
public class Exemption implements NsiModelWithCVD<Long> {

    /**
     * Собственно код льготы
     */
    private int exemptionExpressCode;
    private String regionOkatoCode;
    private String name;
    private Integer exemptionOrganizationCode;
    private Integer exemptionGroupCode;
    private int percentage;
    private Date activeFromDate;
    private Date activeTillDate;
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
    private boolean requireSnilsNumber;
    private boolean requireSocialCard;
    private boolean regionOnly;
    private Integer newExemptionExpressCode;
    private boolean takeProcessingFee;
    private boolean notRequireDocumentNumber;
    private boolean notRequireFIO;
    private Long code;
    private int versionId;
    private Integer deleteInVersionId;

    public int getExemptionExpressCode() {
        return exemptionExpressCode;
    }

    public void setExemptionExpressCode(int exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }

    public String getRegionOkatoCode() {
        return regionOkatoCode;
    }

    public void setRegionOkatoCode(String regionOkatoCode) {
        this.regionOkatoCode = regionOkatoCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getExemptionOrganizationCode() {
        return exemptionOrganizationCode;
    }

    public void setExemptionOrganizationCode(Integer exemptionOrganizationCode) {
        this.exemptionOrganizationCode = exemptionOrganizationCode;
    }

    public Integer getExemptionGroupCode() {
        return exemptionGroupCode;
    }

    public void setExemptionGroupCode(Integer exemptionGroupCode) {
        this.exemptionGroupCode = exemptionGroupCode;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public Date getActiveFromDate() {
        return activeFromDate;
    }

    public void setActiveFromDate(Date activeFromDate) {
        this.activeFromDate = activeFromDate;
    }

    public Date getActiveTillDate() {
        return activeTillDate;
    }

    public void setActiveTillDate(Date activeTillDate) {
        this.activeTillDate = activeTillDate;
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

    public boolean isRegionOnly() {
        return regionOnly;
    }

    public void setRegionOnly(boolean regionOnly) {
        this.regionOnly = regionOnly;
    }

    public Integer getNewExemptionExpressCode() {
        return newExemptionExpressCode;
    }

    public void setNewExemptionExpressCode(Integer newExemptionExpressCode) {
        this.newExemptionExpressCode = newExemptionExpressCode;
    }

    public boolean isTakeProcessingFee() {
        return takeProcessingFee;
    }

    public void setTakeProcessingFee(boolean takeProcessingFee) {
        this.takeProcessingFee = takeProcessingFee;
    }

    public boolean isNotRequireDocumentNumber() {
        return notRequireDocumentNumber;
    }

    public void setNotRequireDocumentNumber(boolean notRequireDocumentNumber) {
        this.notRequireDocumentNumber = notRequireDocumentNumber;
    }

    public boolean isNotRequireFIO() {
        return notRequireFIO;
    }

    public void setNotRequireFIO(boolean notRequireFIO) {
        this.notRequireFIO = notRequireFIO;
    }

    @Override
    public Long getCode() {
        return code;
    }

    @Override
    public void setCode(Long code) {
        this.code = code;
    }

    @Override
    public int getVersionId() {
        return versionId;
    }

    @Override
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    @Override
    public Integer getDeleteInVersionId() {
        return deleteInVersionId;
    }

    @Override
    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }
}
