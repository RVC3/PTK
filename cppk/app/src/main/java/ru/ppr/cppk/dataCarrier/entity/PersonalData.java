package ru.ppr.cppk.dataCarrier.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Персональные данные, считанные со смарт-карты.
 */
public class PersonalData implements Parcelable {

    private String surname = "";
    private String name = "";
    private String lastName = "";
    private Gender gender = Gender.UNKNOWN;
    private String birdthDate = "";

    public PersonalData() {
    }

    ;

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getBirdthDate() {
        return birdthDate;
    }

    public void setBirdthDate(String birdthDate) {
        this.birdthDate = birdthDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Возвращает Персональные данные в виде: Иванов И И
     */
    public String getSmallString() {
        StringBuilder sb = new StringBuilder(surname.trim());
        sb.append(" ");
        if (name != null && name.trim().length() > 0) {
            sb.append(name.trim().substring(0, 1));
        }
        sb.append(" ");
        if (lastName != null && lastName.trim().length() > 0) {
            sb.append(lastName.trim().substring(0, 1));
        }
        return sb.toString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(surname);
        dest.writeString(name);
        dest.writeString(lastName);
        dest.writeString(birdthDate);
        dest.writeSerializable(gender);
    }

    public PersonalData(Parcel in) {
        surname = in.readString();
        name = in.readString();
        lastName = in.readString();
        birdthDate = in.readString();
        gender = (Gender) in.readSerializable();
    }

    public static final Parcelable.Creator<PersonalData> CREATOR = new Creator<PersonalData>() {

        @Override
        public PersonalData[] newArray(int size) {
            return new PersonalData[size];
        }

        @Override
        public PersonalData createFromParcel(Parcel source) {
            return new PersonalData(source);
        }
    };

    @Override
    public String toString() {
        return "PersonalData{" +
                "surname='" + surname + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", birdthDate='" + birdthDate + '\'' +
                '}';
    }
}
