package ru.ppr.cppk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Александр on 21.07.2016.
 */
public class BluetoothDevice implements Parcelable {

    private final String address;
    private final String name;

    public BluetoothDevice(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public static Creator<BluetoothDevice> getCREATOR() {
        return CREATOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BluetoothDevice that = (BluetoothDevice) o;

        return address.equals(that.address);

    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    public int describeContents() {
        return 0;
    }

    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(address);
        parcel.writeString(name);
    }

    public static final Parcelable.Creator<BluetoothDevice> CREATOR = new Parcelable.Creator<BluetoothDevice>() {
        // распаковываем объект из Parcel
        public BluetoothDevice createFromParcel(Parcel in) {
            return new BluetoothDevice(in);
        }

        public BluetoothDevice[] newArray(int size) {
            return new BluetoothDevice[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private BluetoothDevice(Parcel parcel) {
        address = parcel.readString();
        name = parcel.readString();
    }
}
