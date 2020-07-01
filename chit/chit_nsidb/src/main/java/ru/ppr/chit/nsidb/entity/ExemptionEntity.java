package ru.ppr.chit.nsidb.entity;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCVD;
import ru.ppr.chit.nsidb.entity.converter.DateTimeConverter;
import ru.ppr.logger.Logger;

/**
 * Льгота.
 *
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = "Exemptions")
public class ExemptionEntity implements NsiEntityWithCVD<Long> {

    /**
     * Собственно код льготы
     */
    @Property(nameInDb = "ExemptionExpressCode")
    private int exemptionExpressCode;
    @Property(nameInDb = "RegionOkatoCode")
    private String regionOkatoCode;
    @Property(nameInDb = "Name")
    private String name;
    @Property(nameInDb = "ExemptionOrganizationCode")
    private Integer exemptionOrganizationCode;
    @Property(nameInDb = "ExemptionGroupCode")
    private Integer exemptionGroupCode;
    @Property(nameInDb = "Percentage")
    private int percentage;
    @Convert(converter = DateTimeConverter.class, columnType = String.class)
    @Property(nameInDb = "ActiveFromDate")
    private Date activeFromDate;
    @Convert(converter = DateTimeConverter.class, columnType = String.class)
    @Property(nameInDb = "ActiveTillDate")
    private Date activeTillDate;
    @Property(nameInDb = "ChildTicketAvailable")
    private boolean childTicketAvailable;
    @Property(nameInDb = "MassRegistryAvailable")
    private boolean massRegistryAvailable;
    @Property(nameInDb = "CppkRegistryBan")
    private boolean cppkRegistryBan;
    /**
     * Признак возможности предварительной продажи льготного ПД на 7000 с местом.
     */
    @Property(nameInDb = "Presale7000WithPlace")
    private boolean presale7000WithPlace;
    /**
     * Признак возможности предварительной продажи льготного разового ПД на 6000.
     */
    @Property(nameInDb = "Presale6000Once")
    private boolean presale6000Once;
    /**
     * Признак возможности предварительной продажи льготного абонементного ПД на 6000.
     */
    @Property(nameInDb = "Presale6000Abonement")
    private boolean presale6000Abonement;
    @Property(nameInDb = "RequireSnilsNumber")
    private boolean requireSnilsNumber;
    @Property(nameInDb = "RequireSocialCard")
    private boolean requireSocialCard;
    @Property(nameInDb = "IsRegionOnly")
    private boolean regionOnly;
    @Property(nameInDb = "NewExemptionExpressCode")
    private Integer newExemptionExpressCode;
    @Property(nameInDb = "IsTakeProcessingFee")
    private boolean takeProcessingFee;
    @Property(nameInDb = "NotRequireDocumentNumber")
    private boolean notRequireDocumentNumber;
    @Property(nameInDb = "NotRequireFIO")
    private boolean notRequireFIO;
    @Property(nameInDb = "Code")
    private Long code;
    @Property(nameInDb = "VersionId")
    private int versionId;
    @Property(nameInDb = "DeleteInVersionId")
    private Integer deleteInVersionId;

    @Generated(hash = 185563899)
    public ExemptionEntity(int exemptionExpressCode, String regionOkatoCode, String name, Integer exemptionOrganizationCode,
                           Integer exemptionGroupCode, int percentage, Date activeFromDate, Date activeTillDate,
                           boolean childTicketAvailable, boolean massRegistryAvailable, boolean cppkRegistryBan,
                           boolean presale7000WithPlace, boolean presale6000Once, boolean presale6000Abonement, boolean requireSnilsNumber,
                           boolean requireSocialCard, boolean regionOnly, Integer newExemptionExpressCode, boolean takeProcessingFee,
                           boolean notRequireDocumentNumber, boolean notRequireFIO, Long code, int versionId, Integer deleteInVersionId) {
        this.exemptionExpressCode = exemptionExpressCode;
        this.regionOkatoCode = regionOkatoCode;
        this.name = name;
        this.exemptionOrganizationCode = exemptionOrganizationCode;
        this.exemptionGroupCode = exemptionGroupCode;
        this.percentage = percentage;
        this.activeFromDate = activeFromDate;
        this.activeTillDate = activeTillDate;
        this.childTicketAvailable = childTicketAvailable;
        this.massRegistryAvailable = massRegistryAvailable;
        this.cppkRegistryBan = cppkRegistryBan;
        this.presale7000WithPlace = presale7000WithPlace;
        this.presale6000Once = presale6000Once;
        this.presale6000Abonement = presale6000Abonement;
        this.requireSnilsNumber = requireSnilsNumber;
        this.requireSocialCard = requireSocialCard;
        this.regionOnly = regionOnly;
        this.newExemptionExpressCode = newExemptionExpressCode;
        this.takeProcessingFee = takeProcessingFee;
        this.notRequireDocumentNumber = notRequireDocumentNumber;
        this.notRequireFIO = notRequireFIO;
        this.code = code;
        this.versionId = versionId;
        this.deleteInVersionId = deleteInVersionId;
    }

    @Generated(hash = 2115434424)
    public ExemptionEntity() {
    }

    public int getExemptionExpressCode() {
        return this.exemptionExpressCode;
    }

    public void setExemptionExpressCode(int exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }

    public String getRegionOkatoCode() {
        return this.regionOkatoCode;
    }

    public void setRegionOkatoCode(String regionOkatoCode) {
        this.regionOkatoCode = regionOkatoCode;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getExemptionOrganizationCode() {
        return this.exemptionOrganizationCode;
    }

    public void setExemptionOrganizationCode(Integer exemptionOrganizationCode) {
        this.exemptionOrganizationCode = exemptionOrganizationCode;
    }

    public Integer getExemptionGroupCode() {
        return this.exemptionGroupCode;
    }

    public void setExemptionGroupCode(Integer exemptionGroupCode) {
        this.exemptionGroupCode = exemptionGroupCode;
    }

    public int getPercentage() {
        return this.percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public Date getActiveFromDate() {
        return this.activeFromDate;
    }

    public void setActiveFromDate(Date activeFromDate) {
        this.activeFromDate = activeFromDate;
    }

    public Date getActiveTillDate() {
        return this.activeTillDate;
    }

    public void setActiveTillDate(Date activeTillDate) {
        this.activeTillDate = activeTillDate;
    }

    public boolean getChildTicketAvailable() {
        return this.childTicketAvailable;
    }

    public void setChildTicketAvailable(boolean childTicketAvailable) {
        this.childTicketAvailable = childTicketAvailable;
    }

    public boolean getMassRegistryAvailable() {
        return this.massRegistryAvailable;
    }

    public void setMassRegistryAvailable(boolean massRegistryAvailable) {
        this.massRegistryAvailable = massRegistryAvailable;
    }

    public boolean getCppkRegistryBan() {
        return this.cppkRegistryBan;
    }

    public void setCppkRegistryBan(boolean cppkRegistryBan) {
        this.cppkRegistryBan = cppkRegistryBan;
    }

    public boolean getPresale7000WithPlace() {
        return this.presale7000WithPlace;
    }

    public void setPresale7000WithPlace(boolean presale7000WithPlace) {
        this.presale7000WithPlace = presale7000WithPlace;
    }

    public boolean getPresale6000Once() {
        return this.presale6000Once;
    }

    public void setPresale6000Once(boolean presale6000Once) {
        this.presale6000Once = presale6000Once;
    }

    public boolean getPresale6000Abonement() {
        return this.presale6000Abonement;
    }

    public void setPresale6000Abonement(boolean presale6000Abonement) {
        this.presale6000Abonement = presale6000Abonement;
    }

    public boolean getRequireSnilsNumber() {
        return this.requireSnilsNumber;
    }

    public void setRequireSnilsNumber(boolean requireSnilsNumber) {
        this.requireSnilsNumber = requireSnilsNumber;
    }

    public boolean getRequireSocialCard() {
        return this.requireSocialCard;
    }

    public void setRequireSocialCard(boolean requireSocialCard) {
        this.requireSocialCard = requireSocialCard;
    }

    public boolean getRegionOnly() {
        return this.regionOnly;
    }

    public void setRegionOnly(boolean regionOnly) {
        this.regionOnly = regionOnly;
    }

    public Integer getNewExemptionExpressCode() {
        return this.newExemptionExpressCode;
    }

    public void setNewExemptionExpressCode(Integer newExemptionExpressCode) {
        this.newExemptionExpressCode = newExemptionExpressCode;
    }

    public boolean getTakeProcessingFee() {
        return this.takeProcessingFee;
    }

    public void setTakeProcessingFee(boolean takeProcessingFee) {
        this.takeProcessingFee = takeProcessingFee;
    }

    public boolean getNotRequireDocumentNumber() {
        return this.notRequireDocumentNumber;
    }

    public void setNotRequireDocumentNumber(boolean notRequireDocumentNumber) {
        this.notRequireDocumentNumber = notRequireDocumentNumber;
    }

    public boolean getNotRequireFIO() {
        return this.notRequireFIO;
    }

    public void setNotRequireFIO(boolean notRequireFIO) {
        this.notRequireFIO = notRequireFIO;
    }

    @Override
    public Long getCode() {
        return this.code;
    }

    @Override
    public void setCode(Long code) {
        this.code = code;
    }

    @Override
    public int getVersionId() {
        return this.versionId;
    }

    @Override
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    @Override
    public Integer getDeleteInVersionId() {
        return this.deleteInVersionId;
    }

    @Override
    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }

}
