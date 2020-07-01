package ru.ppr.cppk.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Объект данных для Электронного билета
 *
 * @author Grigoriy Kashka
 */
public class ETicketDataParams implements Parcelable {

    private String email;
    private String phone;

    public ETicketDataParams() {

    }

    public String getData() {
        return TextUtils.isEmpty(email) ? phone : email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.email);
        dest.writeString(this.phone);
    }

    protected ETicketDataParams(Parcel in) {
        this.email = in.readString();
        this.phone = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ETicketDataParams> CREATOR = new Creator<ETicketDataParams>() {
        @Override
        public ETicketDataParams createFromParcel(Parcel in) {
            return new ETicketDataParams(in);
        }

        @Override
        public ETicketDataParams[] newArray(int size) {
            return new ETicketDataParams[size];
        }
    };
}
