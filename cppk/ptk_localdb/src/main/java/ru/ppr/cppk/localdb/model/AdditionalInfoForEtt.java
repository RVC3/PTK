package ru.ppr.cppk.localdb.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Дополнительные параметры для ЭТТ.
 *
 * @author Artem Ushakov
 */
public class AdditionalInfoForEtt implements LocalModelWithId<Long>, Parcelable {
    /**
     * Id
     */
    private Long id;
    /**
     * Шифр категории пассажира
     */
    private String passengerCategory;
    /**
     * Код подразделения (билетного бюро), выдавшего ЭТТ
     */
    private String issueUnitCode;
    /**
     * Код организации в штате которой состоит работник
     */
    private String ownerOrganizationCode;
    /**
     * Фамилия, инициалы пассажира
     */
    private String passengerFio;
    /**
     * Фамилия, инициалы работника РЖД, на чьем иждивении находится пассажир
     */
    private String guardianFio;
    /**
     * Снилс
     */
    private String snils;
    /**
     * Дата выдачи ЭТТ
     */
    private Date issueDateTime;

    public AdditionalInfoForEtt() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getPassengerCategory() {
        return passengerCategory;
    }

    public void setPassengerCategory(String passengerCategory) {
        this.passengerCategory = passengerCategory;
    }

    public String getIssueUnitCode() {
        return issueUnitCode;
    }

    public void setIssueUnitCode(String issueUnitCode) {
        this.issueUnitCode = issueUnitCode;
    }

    public String getOwnerOrganizationCode() {
        return ownerOrganizationCode;
    }

    public void setOwnerOrganizationCode(String ownerOrganizationCode) {
        this.ownerOrganizationCode = ownerOrganizationCode;
    }

    public String getPassengerFio() {
        return passengerFio;
    }

    public void setPassengerFio(String passengerFio) {
        this.passengerFio = passengerFio;
    }

    public String getGuardianFio() {
        return guardianFio;
    }

    public void setGuardianFio(String guardianFio) {
        this.guardianFio = guardianFio;
    }

    public String getSnils() {
        return snils;
    }

    public void setSnils(String snils) {
        this.snils = snils;
    }

    public Date getIssueDateTime() {
        return issueDateTime;
    }

    public void setIssueDateTime(Date issueDateTime) {
        this.issueDateTime = issueDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdditionalInfoForEtt that = (AdditionalInfoForEtt) o;

        if (passengerCategory != null ? !passengerCategory.equals(that.passengerCategory) : that.passengerCategory != null)
            return false;

        if (issueUnitCode != null ? !issueUnitCode.equals(that.issueUnitCode) : that.issueUnitCode != null)
            return false;

        if (ownerOrganizationCode != null ? !ownerOrganizationCode.equals(that.ownerOrganizationCode) : that.ownerOrganizationCode != null)
            return false;

        if (passengerFio != null ? !passengerFio.equals(that.passengerFio) : that.passengerFio != null)
            return false;

        if (guardianFio != null ? !guardianFio.equals(that.guardianFio) : that.guardianFio != null)
            return false;

        if (snils != null ? !snils.equals(that.snils) : that.snils != null)
            return false;

        boolean dateCheck = issueDateTime == null && that.issueDateTime == null;
        if (!dateCheck) {
            if (issueDateTime != null && that.issueDateTime != null) {
                dateCheck = issueDateTime.compareTo(that.issueDateTime) == 0;
            } else {
                dateCheck = false;
            }
        }

        return dateCheck;
    }

    @Override
    public int hashCode() {
        long id = this.id == null ? -1 : this.id;
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (passengerCategory != null ? passengerCategory.hashCode() : 0);
        result = 31 * result + (issueUnitCode != null ? issueUnitCode.hashCode() : 0);
        result = 31 * result + (ownerOrganizationCode != null ? ownerOrganizationCode.hashCode() : 0);
        result = 31 * result + (passengerFio != null ? passengerFio.hashCode() : 0);
        result = 31 * result + (guardianFio != null ? guardianFio.hashCode() : 0);
        result = 31 * result + (snils != null ? snils.hashCode() : 0);
        result = 31 * result + (issueDateTime != null ? issueDateTime.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id != null ? this.id : -1);
        dest.writeString(this.passengerCategory);
        dest.writeString(this.issueUnitCode);
        dest.writeString(this.ownerOrganizationCode);
        dest.writeString(this.passengerFio);
        dest.writeString(this.guardianFio);
        dest.writeString(this.snils);
        dest.writeLong(this.issueDateTime != null ? this.issueDateTime.getTime() : -1);
    }

    protected AdditionalInfoForEtt(Parcel in) {
        long id = in.readLong();
        this.id = id == -1 ? null : id;
        this.passengerCategory = in.readString();
        this.issueUnitCode = in.readString();
        this.ownerOrganizationCode = in.readString();
        this.passengerFio = in.readString();
        this.guardianFio = in.readString();
        this.snils = in.readString();
        long time = in.readLong();
        this.issueDateTime = time == -1 ? null : new Date(time);
    }

    public static final Parcelable.Creator<AdditionalInfoForEtt> CREATOR = new Parcelable.Creator<AdditionalInfoForEtt>() {
        @Override
        public AdditionalInfoForEtt createFromParcel(Parcel source) {
            return new AdditionalInfoForEtt(source);
        }

        @Override
        public AdditionalInfoForEtt[] newArray(int size) {
            return new AdditionalInfoForEtt[size];
        }
    };
}
