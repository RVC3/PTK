package ru.ppr.cppk.ui.activity.decrementtrip.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Результат с экрана списания поездки.
 *
 * @author Aleksandr Brazhkin
 */
public class DecrementTripResult implements Parcelable {
    /**
     * Поездка была списана
     */
    private final boolean tripDecremented;

    public DecrementTripResult(boolean tripDecremented) {
        this.tripDecremented = tripDecremented;
    }

    public boolean isTripDecremented() {
        return tripDecremented;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.tripDecremented ? (byte) 1 : (byte) 0);
    }

    protected DecrementTripResult(Parcel in) {
        this.tripDecremented = in.readByte() != 0;
    }

    public static final Parcelable.Creator<DecrementTripResult> CREATOR = new Parcelable.Creator<DecrementTripResult>() {
        @Override
        public DecrementTripResult createFromParcel(Parcel source) {
            return new DecrementTripResult(source);
        }

        @Override
        public DecrementTripResult[] newArray(int size) {
            return new DecrementTripResult[size];
        }
    };
}
