package ru.ppr.cppk.entity.event.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.nsi.entity.Exemption;
import ru.ppr.nsi.entity.ExemptionGroup;

public class ExemptionForEvent implements Parcelable {

    private long id;
    private String fio = null;
    private BigDecimal lossSumm = BigDecimal.ZERO;
    private long smartCardId = -1;
    private boolean isSnilsUsed = false;
    private boolean isManualInput = false;
    private SmartCard smartCardFromWhichWasReadAboutExemption = null;
    private String typeOfDocumentWhichApproveExemption = null;
    private String numberOfDocumentWhichApproveExemption = null;
    /**
     * Дата выдачи ЭТТ при ручном вводе льготы
     * http://agile.srvdev.ru/browse/CPPKPP-37471
     */
    private Date issueDate;

    // Поля НСИ
    private int code;
    private Date activeFromDate = null;
    private long versionId;
    private int expressCode;
    private String organization = null;
    private String regionOkatoCode = null;
    private boolean requireSocialCard = false;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SmartCard getSmartCardFromWhichWasReadAboutExemption() {

        SmartCard local = smartCardFromWhichWasReadAboutExemption;

        if (local == null && smartCardId >= 0) {

            synchronized (this) {
                if (smartCardFromWhichWasReadAboutExemption == null) {
                    smartCardFromWhichWasReadAboutExemption = Dagger.appComponent().localDaoSession().getSmartCardDao().load(smartCardId);
                }
            }
            return smartCardFromWhichWasReadAboutExemption;
        }

        return local;
    }

    public String getRegionOkatoCode() {
        return regionOkatoCode;
    }

    public void setRegionOkatoCode(String regionOkatoCode) {
        this.regionOkatoCode = regionOkatoCode;
    }

    public boolean isRequireSocialCard() {
        return requireSocialCard;
    }

    public void setRequireSocialCard(boolean requireSocialCard) {
        this.requireSocialCard = requireSocialCard;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public ExemptionForEvent() { /* NOP */ }

    private ExemptionForEvent(Parcel parcel) {
        setFio(parcel.readString());
        code = parcel.readInt();
        long activeFromDateLocal = parcel.readLong();
        activeFromDate = activeFromDateLocal == 0 ? null : new Date(activeFromDateLocal);
        versionId = parcel.readLong();
        expressCode = parcel.readInt();
        setLossSumm(new BigDecimal(parcel.readString()));
        setSmartCardId(parcel.readLong());
        setTypeOfDocumentWhichApproveExemption(parcel.readString());
        setNumberOfDocumentWhichApproveExemption(parcel.readString());
        setOrganization(parcel.readString());
        setSmartCardFromWhichWasReadAboutExemption(parcel.readParcelable(SmartCard.class.getClassLoader()));
        setManualInput(parcel.readInt() == 1);
        setSnilsUsed(parcel.readInt() == 1);
        setRegionOkatoCode(parcel.readString());
        setRequireSocialCard(parcel.readInt() == 1);
        long issueDateLong = parcel.readLong();
        setIssueDate(issueDateLong == 0 ? null : new Date(issueDateLong));
    }

    public static Parcelable.Creator<ExemptionForEvent> CREATOR = new Parcelable.Creator<ExemptionForEvent>() {

        @Override
        public ExemptionForEvent createFromParcel(Parcel source) {
            return new ExemptionForEvent(source);
        }

        @Override
        public ExemptionForEvent[] newArray(int size) {
            return new ExemptionForEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getFio());
        dest.writeInt(code);
        dest.writeLong(activeFromDate == null ? 0 : activeFromDate.getTime());
        dest.writeLong(versionId);
        dest.writeInt(expressCode);
        dest.writeString(getLossSumm().toString());
        dest.writeLong(smartCardId);
        dest.writeString(getTypeOfDocumentWhichApproveExemption());
        dest.writeString(getNumberOfDocumentWhichApproveExemption());
        dest.writeString(getOrganization());
        dest.writeParcelable(getSmartCardFromWhichWasReadAboutExemption(), PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeInt(isManualInput() ? 1 : 0);
        dest.writeInt(isSnilsUsed() ? 1 : 0);
        dest.writeString(getRegionOkatoCode());
        dest.writeInt(isRequireSocialCard() ? 1 : 0);
        dest.writeLong(issueDate == null ? 0 : issueDate.getTime());
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }

    public Date getActiveFromDate() {
        return activeFromDate;
    }

    public void setActiveFromDate(Date activeFromDate) {
        this.activeFromDate = activeFromDate;
    }

    /**
     * 4-х значный код льготы
     */
    public int getExpressCode() {
        return expressCode;
    }

    public void setExpressCode(int expressCode) {
        this.expressCode = expressCode;
    }

    /**
     * Сумма потерь
     */
    public BigDecimal getLossSumm() {
        return lossSumm;
    }

    public void setLossSumm(BigDecimal lossSumm) {
        this.lossSumm = lossSumm;
    }

    public long getSmartCardId() {
        return smartCardId;
    }

    public void setSmartCardId(long smartCardId) {
        this.smartCardId = smartCardId;
    }

    public void setSmartCardFromWhichWasReadAboutExemption(SmartCard smartCardFromWhichWasReadAboutExemption) {
        this.smartCardFromWhichWasReadAboutExemption = smartCardFromWhichWasReadAboutExemption;
    }

    public String getTypeOfDocumentWhichApproveExemption() {
        return typeOfDocumentWhichApproveExemption;
    }

    public void setTypeOfDocumentWhichApproveExemption(String typeOfDocumentWhichApproveExemption) {
        this.typeOfDocumentWhichApproveExemption = typeOfDocumentWhichApproveExemption;
    }

    public String getNumberOfDocumentWhichApproveExemption() {
        return numberOfDocumentWhichApproveExemption;
    }

    public void setNumberOfDocumentWhichApproveExemption(String numberOfDocumentWhichApproveExemption) {
        this.numberOfDocumentWhichApproveExemption = numberOfDocumentWhichApproveExemption;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public boolean isSnilsUsed() {
        return isSnilsUsed;
    }

    public void setSnilsUsed(boolean snilsUsed) {
        isSnilsUsed = snilsUsed;
    }

    public boolean isManualInput() {
        return isManualInput;
    }

    public void setManualInput(boolean isManualInput) {
        this.isManualInput = isManualInput;
    }

    public void fillFromExemption(Exemption exemption) {
        setCode(exemption.getCode());
        setVersionId(exemption.getVersionId());
        setActiveFromDate(exemption.getActiveFromDate());
        setOrganization(exemption.getExemptionOrganizationCode());
        setExpressCode(exemption.getExemptionExpressCode());
        setRegionOkatoCode(exemption.getRegionOkatoCode());
        setRequireSocialCard(exemption.isRequireSocialCard());
        ExemptionGroup exemptionGroup = exemption.getExemptionGroup(Dagger.appComponent().exemptionGroupRepository(), Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId());
        setTypeOfDocumentWhichApproveExemption(exemptionGroup == null ? null : exemptionGroup.getGroupName());
    }

}