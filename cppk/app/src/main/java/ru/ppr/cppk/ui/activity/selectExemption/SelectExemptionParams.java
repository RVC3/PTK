package ru.ppr.cppk.ui.activity.selectExemption;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.model.SmartCardId;
import ru.ppr.nsi.entity.TrainCategory;

/**
 * @author Aleksandr Brazhkin
 */
public class SelectExemptionParams implements Parcelable {

    private Date pdStartDateTime;
    private Date pdEndDateTime;
    private int tariffPlanCode;
    private int ticketTypeCode;
    private TrainCategory trainCategory;
    private List<Integer> regionCodes;
    private int ticketCategoryCode;
    private boolean allowReadFromBsc;
    private int versionNsi;
    private Date timeStamp;
    private int exceptedExpressCode;
    private SmartCardId exceptedSmartCardId;
    private Integer parentPdTicketCategoryCode;

    public SelectExemptionParams() {

    }

    public Date getPdStartDateTime() {
        return pdStartDateTime;
    }

    public void setPdStartDateTime(Date pdStartDateTime) {
        this.pdStartDateTime = pdStartDateTime;
    }

    public Date getPdEndDateTime() {
        return pdEndDateTime;
    }

    public void setPdEndDateTime(Date pdEndDateTime) {
        this.pdEndDateTime = pdEndDateTime;
    }

    public int getTariffPlanCode() {
        return tariffPlanCode;
    }

    public void setTariffPlanCode(int tariffPlanCode) {
        this.tariffPlanCode = tariffPlanCode;
    }

    public int getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(int ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

    public TrainCategory getTrainCategory() {
        return trainCategory;
    }

    public void setTrainCategory(TrainCategory trainCategory) {
        this.trainCategory = trainCategory;
    }

    public List<Integer> getRegionCodes() {
        return regionCodes;
    }

    public void setRegionCodes(List<Integer> regionCodes) {
        this.regionCodes = regionCodes;
    }

    public int getTicketCategoryCode() {
        return ticketCategoryCode;
    }

    public void setTicketCategoryCode(int ticketCategoryCode) {
        this.ticketCategoryCode = ticketCategoryCode;
    }

    public boolean isAllowReadFromBsc() {
        return allowReadFromBsc;
    }

    public void setAllowReadFromBsc(boolean allowReadFromBsc) {
        this.allowReadFromBsc = allowReadFromBsc;
    }

    public int getVersionNsi() {
        return versionNsi;
    }

    public void setVersionNsi(int versionNsi) {
        this.versionNsi = versionNsi;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getExceptedExpressCode() {
        return exceptedExpressCode;
    }

    public void setExceptedExpressCode(int exceptedExpressCode) {
        this.exceptedExpressCode = exceptedExpressCode;
    }

    public SmartCardId getExceptedSmartCardId() {
        return exceptedSmartCardId;
    }

    public void setExceptedSmartCardId(SmartCardId exceptedSmartCardId) {
        this.exceptedSmartCardId = exceptedSmartCardId;
    }

    public Integer getParentPdTicketCategoryCode() {
        return parentPdTicketCategoryCode;
    }

    public void setParentPdTicketCategoryCode(Integer parentPdTicketCategoryCode) {
        this.parentPdTicketCategoryCode = parentPdTicketCategoryCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.pdStartDateTime != null ? this.pdStartDateTime.getTime() : -1);
        dest.writeLong(this.pdEndDateTime != null ? this.pdEndDateTime.getTime() : -1);
        dest.writeInt(this.tariffPlanCode);
        dest.writeInt(this.ticketTypeCode);
        dest.writeParcelable(this.trainCategory, flags);
        dest.writeList(this.regionCodes);
        dest.writeInt(this.ticketCategoryCode);
        dest.writeByte(this.allowReadFromBsc ? (byte) 1 : (byte) 0);
        dest.writeInt(this.versionNsi);
        dest.writeLong(this.timeStamp != null ? this.timeStamp.getTime() : -1);
        dest.writeInt(this.exceptedExpressCode);
        dest.writeParcelable(this.exceptedSmartCardId, flags);
        dest.writeValue(this.parentPdTicketCategoryCode);
    }

    protected SelectExemptionParams(Parcel in) {
        long tmpPdStartDateTime = in.readLong();
        this.pdStartDateTime = tmpPdStartDateTime == -1 ? null : new Date(tmpPdStartDateTime);
        long tmpPdEndDateTime = in.readLong();
        this.pdEndDateTime = tmpPdEndDateTime == -1 ? null : new Date(tmpPdEndDateTime);
        this.tariffPlanCode = in.readInt();
        this.ticketTypeCode = in.readInt();
        this.trainCategory = in.readParcelable(TrainCategory.class.getClassLoader());
        this.regionCodes = new ArrayList<>();
        in.readList(this.regionCodes, Integer.class.getClassLoader());
        this.ticketCategoryCode = in.readInt();
        this.allowReadFromBsc = in.readByte() != 0;
        this.versionNsi = in.readInt();
        long tmpTimeStamp = in.readLong();
        this.timeStamp = tmpTimeStamp == -1 ? null : new Date(tmpTimeStamp);
        this.exceptedExpressCode = in.readInt();
        this.exceptedSmartCardId = in.readParcelable(SmartCardId.class.getClassLoader());
        this.parentPdTicketCategoryCode = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Creator<SelectExemptionParams> CREATOR = new Creator<SelectExemptionParams>() {
        @Override
        public SelectExemptionParams createFromParcel(Parcel source) {
            return new SelectExemptionParams(source);
        }

        @Override
        public SelectExemptionParams[] newArray(int size) {
            return new SelectExemptionParams[size];
        }
    };
}
