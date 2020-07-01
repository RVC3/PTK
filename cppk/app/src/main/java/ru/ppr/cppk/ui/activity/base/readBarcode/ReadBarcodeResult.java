package ru.ppr.cppk.ui.activity.base.readBarcode;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import ru.ppr.cppk.dataCarrier.entity.PD;

/**
 * @author Dmitry Nevolin
 */
public class ReadBarcodeResult implements Parcelable {

    private List<PD> pdList;
    private Long couponReadEventId;

    public ReadBarcodeResult() {
    }

    public List<PD> getPdList() {
        return pdList;
    }

    public void setPdList(List<PD> pdList) {
        this.pdList = pdList;
    }

    public Long getCouponReadEventId() {
        return couponReadEventId;
    }

    public void setCouponReadEventId(Long couponReadEventId) {
        this.couponReadEventId = couponReadEventId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.pdList);
        dest.writeValue(this.couponReadEventId);
    }

    protected ReadBarcodeResult(Parcel in) {
        this.pdList = in.createTypedArrayList(PD.CREATOR);
        this.couponReadEventId = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Creator<ReadBarcodeResult> CREATOR = new Creator<ReadBarcodeResult>() {
        @Override
        public ReadBarcodeResult createFromParcel(Parcel source) {
            return new ReadBarcodeResult(source);
        }

        @Override
        public ReadBarcodeResult[] newArray(int size) {
            return new ReadBarcodeResult[size];
        }
    };

}
