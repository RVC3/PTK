package ru.ppr.cppk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * @author Aleksandr Brazhkin
 */
public class RemoveExemptionParams implements Parcelable {

    private int expressCode;
    private String groupName;
    private int percentage;
    private String fio;
    private String documentNumber;
    private Date documentIssueDate;
    private String bscNumber;
    private String bscType;
    private boolean isRequireSnilsNumber;

    public RemoveExemptionParams() {

    }

    public int getExpressCode() {
        return expressCode;
    }

    public void setExpressCode(int expressCode) {
        this.expressCode = expressCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Date getDocumentIssueDate() {
        return documentIssueDate;
    }

    public void setDocumentIssueDate(Date documentIssueDate) {
        this.documentIssueDate = documentIssueDate;
    }

    public String getBscNumber() {
        return bscNumber;
    }

    public void setBscNumber(String bscNumber) {
        this.bscNumber = bscNumber;
    }

    public String getBscType() {
        return bscType;
    }

    public void setBscType(String bscType) {
        this.bscType = bscType;
    }

    public boolean isRequireSnilsNumber() {
        return isRequireSnilsNumber;
    }

    public void setRequireSnilsNumber(boolean requireSnilsNumber) {
        isRequireSnilsNumber = requireSnilsNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.expressCode);
        dest.writeString(this.groupName);
        dest.writeInt(this.percentage);
        dest.writeString(this.fio);
        dest.writeString(this.documentNumber);
        dest.writeLong(this.documentIssueDate != null ? this.documentIssueDate.getTime() : -1);
        dest.writeString(this.bscNumber);
        dest.writeString(this.bscType);
        dest.writeByte(this.isRequireSnilsNumber ? (byte) 1 : (byte) 0);
    }

    protected RemoveExemptionParams(Parcel in) {
        this.expressCode = in.readInt();
        this.groupName = in.readString();
        this.percentage = in.readInt();
        this.fio = in.readString();
        this.documentNumber = in.readString();
        long tmpDocumentIssueDate = in.readLong();
        this.documentIssueDate = tmpDocumentIssueDate == -1 ? null : new Date(tmpDocumentIssueDate);
        this.bscNumber = in.readString();
        this.bscType = in.readString();
        this.isRequireSnilsNumber = in.readByte() != 0;
    }

    public static final Creator<RemoveExemptionParams> CREATOR = new Creator<RemoveExemptionParams>() {
        @Override
        public RemoveExemptionParams createFromParcel(Parcel source) {
            return new RemoveExemptionParams(source);
        }

        @Override
        public RemoveExemptionParams[] newArray(int size) {
            return new RemoveExemptionParams[size];
        }
    };
}
