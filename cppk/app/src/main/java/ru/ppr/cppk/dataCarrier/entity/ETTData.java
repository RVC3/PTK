package ru.ppr.cppk.dataCarrier.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Данные ЭТТ на смарт-кате.
 *
 * @author Dmitry Nevolin
 */
public class ETTData implements Parcelable {

    private String passengerCategoryCipher;
    private String divisionCode;
    private String organizationCode;
    private String ETTNumber;
    private String benefitExpressCipher;

    private String surname;
    private String firstName;
    private String secondName;
    private Date birthDate;
    private String documentIssuingCountry;
    private String birthPlace;
    private String gender;
    private String workerInitials;
    private String SNILSCode;

    public ETTData() {

    }

    private ETTData(Parcel source) {
        passengerCategoryCipher = source.readString();
        divisionCode = source.readString();
        organizationCode = source.readString();
        ETTNumber = source.readString();
        benefitExpressCipher = source.readString();

        surname = source.readString();
        firstName = source.readString();
        secondName = source.readString();

        long birthDate = source.readLong();

        this.birthDate = birthDate == -1 ? null : new Date(birthDate);
        documentIssuingCountry = source.readString();
        birthPlace = source.readString();
        gender = source.readString();
        workerInitials = source.readString();
        SNILSCode = source.readString();
    }

    /**
     * Возвращает Персональные данные в виде: Иванов И И
     */
    public String getSmallString() {
        StringBuilder sb = new StringBuilder();

        if (surname != null && surname.trim().length() > 0)
            sb.append(surname.trim());

        sb.append(" ");

        if (firstName != null && firstName.trim().length() > 0)
            sb.append(firstName.trim().substring(0, 1));

        sb.append(" ");

        if (secondName != null && secondName.trim().length() > 0)
            sb.append(secondName.trim().substring(0, 1));

        return sb.toString();
    }

    public String getPassengerCategoryCipher() {
        return passengerCategoryCipher;
    }

    public void setPassengerCategoryCipher(String passengerCategoryCipher) {
        this.passengerCategoryCipher = passengerCategoryCipher;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getETTNumber() {
        return ETTNumber;
    }

    public void setETTNumber(String ETTNumber) {
        this.ETTNumber = ETTNumber;
    }

    public String getBenefitExpressCipher() {
        return benefitExpressCipher;
    }

    public void setBenefitExpressCipher(String benefitExpressCipher) {
        this.benefitExpressCipher = benefitExpressCipher;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getDocumentIssuingCountry() {
        return documentIssuingCountry;
    }

    public void setDocumentIssuingCountry(String documentIssuingCountry) {
        this.documentIssuingCountry = documentIssuingCountry;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWorkerInitials() {
        return workerInitials;
    }

    public void setWorkerInitials(String workerInitials) {
        this.workerInitials = workerInitials;
    }

    public String getSNILSCode() {
        return SNILSCode;
    }

    public void setSNILSCode(String SNILSCode) {
        this.SNILSCode = SNILSCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(passengerCategoryCipher);
        dest.writeString(divisionCode);
        dest.writeString(organizationCode);
        dest.writeString(ETTNumber);
        dest.writeString(benefitExpressCipher);

        dest.writeString(surname);
        dest.writeString(firstName);
        dest.writeString(secondName);
        dest.writeLong(birthDate != null ? birthDate.getTime() : -1);
        dest.writeString(documentIssuingCountry);
        dest.writeString(birthPlace);
        dest.writeString(gender);
        dest.writeString(workerInitials);
        dest.writeString(SNILSCode);
    }

    public static final Parcelable.Creator<ETTData> CREATOR = new Creator<ETTData>() {
        @Override
        public ETTData[] newArray(int size) {
            return new ETTData[size];
        }

        @Override
        public ETTData createFromParcel(Parcel source) {
            return new ETTData(source);
        }
    };

}
