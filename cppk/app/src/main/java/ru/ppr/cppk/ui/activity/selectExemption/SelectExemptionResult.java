package ru.ppr.cppk.ui.activity.selectExemption;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;

/**
 * @author Aleksandr Brazhkin
 */
public class SelectExemptionResult implements Parcelable {

    private final List<ExemptionForEvent> exemptionsForEvent;
    private final AdditionalInfoForEtt additionalInfoForEtt;

    public SelectExemptionResult(@NonNull List<ExemptionForEvent> exemptionsForEvent, @Nullable AdditionalInfoForEtt additionalInfoForEtt) {
        this.exemptionsForEvent = exemptionsForEvent;
        this.additionalInfoForEtt = additionalInfoForEtt;
    }

    @NonNull
    public List<ExemptionForEvent> getExemptionsForEvent() {
        return exemptionsForEvent;
    }

    @Nullable
    public AdditionalInfoForEtt getAdditionalInfoForEtt() {
        return additionalInfoForEtt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.exemptionsForEvent);
        dest.writeParcelable(this.additionalInfoForEtt, flags);
    }

    protected SelectExemptionResult(Parcel in) {
        this.exemptionsForEvent = in.createTypedArrayList(ExemptionForEvent.CREATOR);
        this.additionalInfoForEtt = in.readParcelable(AdditionalInfoForEtt.class.getClassLoader());
    }

    public static final Creator<SelectExemptionResult> CREATOR = new Creator<SelectExemptionResult>() {
        @Override
        public SelectExemptionResult createFromParcel(Parcel source) {
            return new SelectExemptionResult(source);
        }

        @Override
        public SelectExemptionResult[] newArray(int size) {
            return new SelectExemptionResult[size];
        }
    };
}
