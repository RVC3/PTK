package ru.ppr.cppk.dataCarrier.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.nsi.entity.TicketStorageType;
import ru.ppr.utils.CommonUtils;

/**
 * Контейнер данных о карте
 */
public class BscInformation implements Parcelable {

    private static final String TAG = BscInformation.class.getSimpleName();

    private Date validityTime = new Date(0);
    private Date initDate = new Date(0);
    private TicketStorageType typeBsc = TicketStorageType.Unknown;
    private String bscSeries = "";
    private String externalNumber = "";
    private byte[] cardUID = null;
    private int exemptionCode = 0;
    private EmissionData emissionData;
    private PersonalData personalData;
    private ETTData ettData;
    /**
     * Признак привязки БСК к пассажиру
     */
    private boolean boundToPassenger;

    public BscInformation() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bscSeries);
        dest.writeString(externalNumber);
        int cardUIDLength = cardUID == null ? 0 : cardUID.length;
        dest.writeInt(cardUIDLength);
        dest.writeByteArray(cardUID);
        dest.writeSerializable(typeBsc);
        dest.writeLong(validityTime.getTime());
        dest.writeLong(initDate == null ? 0 : initDate.getTime());
        dest.writeInt(exemptionCode);
        dest.writeParcelable(emissionData, PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(personalData, PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(ettData, PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeInt(boundToPassenger ? 1 : 0);
    }

    public BscInformation(Parcel in) {

        bscSeries = in.readString();
        externalNumber = in.readString();
        int cardUIDLength = in.readInt();
        cardUID = new byte[cardUIDLength];
        in.readByteArray(cardUID);
        typeBsc = (TicketStorageType) in.readSerializable();
        validityTime = new Date(in.readLong());
        long initDatelong = in.readLong();
        initDate = (initDatelong == 0) ? null : new Date(initDatelong);
        exemptionCode = in.readInt();
        emissionData = in.readParcelable(EmissionData.class.getClassLoader());
        personalData = in.readParcelable(PersonalData.class.getClassLoader());
        ettData = in.readParcelable(ETTData.class.getClassLoader());
        boundToPassenger = in.readInt() == 1;
    }

    public static final Parcelable.Creator<BscInformation> CREATOR = new Parcelable.Creator<BscInformation>() {

        public BscInformation createFromParcel(Parcel in) {
            return new BscInformation(in);
        }

        public BscInformation[] newArray(int size) {
            return new BscInformation[size];
        }
    };

    public void setValidityTime(Date validityTime) {
        this.validityTime = validityTime;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public void setExemptionCode(int exemptionCode) {
        this.exemptionCode = exemptionCode;
    }

    public void setBscSeries(String bscSeries) {
        this.bscSeries = bscSeries;
    }

    public void setExternalNumber(String externalNumber) {
        this.externalNumber = externalNumber;
    }

    /**
     * Возвращает срок действия карты
     *
     * @return
     */
    public Date getValidityTime() {
        return validityTime;
    }

    /**
     * Возвращает дату инициализации карты
     *
     * @return
     */
    public Date getInitDate() {
        return initDate;
    }

    /**
     * Возвращает тип БСК
     *
     * @return
     */
    public TicketStorageType getSmartCardTypeBsc() {
        return typeBsc;
    }

    /**
     * Устанавливает тип БСК
     *
     * @return
     */
    public void setTypeBsc(TicketStorageType ticketStorageType) {
        this.typeBsc = ticketStorageType;
    }

    /**
     * Возвращает серию БСК
     *
     * @return 4 цифры в виде строки
     */
    public String getBscSeries() {
        return bscSeries;
    }

    /**
     * Возвращает внешний номер карты
     *
     * @return цифры внешнего номера в виде строки
     */
    public String getExternalNumber() {
        return externalNumber;
    }

    public int getExemptionCode() {
        return exemptionCode;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }

    public void setPersonalData(PersonalData personalData) {
        this.personalData = personalData;
    }

    public ETTData getEttData() {
        return ettData;
    }

    public void setEttData(ETTData ettData) {
        this.ettData = ettData;
    }

    public boolean isBoundToPassenger() {
        return boundToPassenger;
    }

    public void setBoundToPassenger(boolean boundToPassenger) {
        this.boundToPassenger = boundToPassenger;
    }

    /**
     * Вернет номера кристалла БСК. Парсит из rfidAttr
     *
     * @return
     */
    public byte[] getCardUID() {
        return cardUID;
    }

    public void setCardUID(byte[] cardUID) {
        this.cardUID = cardUID;
    }

    public byte[] getCrystalSerialNumber() {
        return Arrays.copyOf(cardUID, 8);
    }

    public String getCrustalSerialNumberString() {
        return String.valueOf(CommonUtils.convertByteToLong(getCardUID(), ByteOrder.LITTLE_ENDIAN));
    }

    public void setEmissionData(EmissionData emissionData) {
        this.emissionData = emissionData;
        validityTime = emissionData.getValidityTime();
    }

    /**
     * Производит проверку даты действия карты и возвращает результат. Билеты
     * записанные на ультралайт(провожающего, абонемент на количество поездок)
     * всегда действительны true - карта действует false - время действия карты
     * истекло
     *
     * @return
     */
    public boolean cardTimeIsValid() {
        return typeBsc == TicketStorageType.SeeOfCard
                || typeBsc == TicketStorageType.CPPKCounter
                || validityTime.after(new Date())
                //https://aj.srvdev.ru/browse/CPPKPP-31895
                || Dagger.appComponent().commonSettingsStorage().get().isIgnoreCardValidityPeriod();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Возвращает внешний номер. Для сбора данных для проверки ЭЦП берем
     * серию и номер например "00150002819983" парсим ее в long переворачиваем в
     * littleEndian и получаем 8 байт - "8f63ddec22000000".
     * Для карт СКМ,СКМО,ИПК внешний номер берется из эмисионных данных
     * Для ЭТТ из внешнего номера убирается последняя(14) цифра
     * Для Стрелки к внешнему номеру спереди добавим 0, чтобы получилось целых 6 байт
     */
    public byte[] getOuterNumberBytes() {
        byte[] out;
        try {

            if (typeBsc.isMustHaveEmissionDate()) {

                if (emissionData == null)
                    throw new IllegalStateException("Emission date is null");

                String cardNumber = emissionData.getCardNumber();
                BigInteger bigInteger = new BigInteger(cardNumber);
                byte[] tmpArray = toLittleEndian(bigInteger.toByteArray());

                out = new byte[8];
                System.arraycopy(tmpArray, 0, out, 0, 8);
            } else if (typeBsc == TicketStorageType.TRK || typeBsc == TicketStorageType.STR) {
                String cardNumber = getExternalNumber();
                // https://aj.srvdev.ru/browse/CPPKPP-30178
                // для обеспечения обратной совместимости, при созданни данных для подписи, ископользуем 10 значный номер стрелки
                if (typeBsc == TicketStorageType.STR)
                    cardNumber = cardNumber.substring(1);
                long number = Long.valueOf(cardNumber);
                out = CommonUtils.generateByteArrayFromLong(number);
            } else {
                String cardNumber;
                //если карта ЭТТ, то отбрасываем последнюю цифру, т.к. у ЭТТ это контрольная цифра
                // и она не участвует в байтах для подписи
                if (typeBsc == TicketStorageType.ETT) {
                    cardNumber = getExternalNumber().substring(0, 13);
                } else {
                    cardNumber = getBscSeries() + getExternalNumber();
                }

                long number = Long.valueOf(cardNumber);
                out = CommonUtils.generateByteArrayFromLong(number);
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            out = new byte[8];
        }
        return out;
    }

    private byte[] toLittleEndian(byte[] src) {

        byte[] dst = new byte[src.length];

        for (int i = 0, j = src.length - 1; i < src.length; i++, j--) {
            dst[i] = src[j];
        }
        return dst;
    }

    /**
     * Возвращает OuterNumber в виде строки без пробела
     * Для карт СКМ, СКМО, ИПК берется из эмисионных данных.
     * Для ЭТТ берется без контрольной цифры
     * Для остальных карт берется из серии и номера.
     *
     * @return внешний номер
     */
    @NonNull
    public String getOuterNumberString() {

        String number;

        if (typeBsc.isMustHaveEmissionDate()) {

            if (emissionData == null)
                throw new IllegalStateException("Emission data is null");
            number = emissionData.getCardNumber();
        } else if (typeBsc == TicketStorageType.TRK || typeBsc == TicketStorageType.STR) {
            //для тройки и стрелки номер должен состоять из 10 цифр
            number = getExternalNumber();
        } else if (TicketStorageType.ETT.equals(typeBsc)) {
            //для тройки удаляем контрольную цифру
            number = getExternalNumber().substring(0, 13);
        } else {
            number = getBscSeries() + getExternalNumber();
        }
        return number;
    }

    /**
     * Возвращает отформатированный OuterNumber для отображения в интерфейсе
     * Для карт СКМ, СКМО, ИПК берется из эмисионных данных
     * Для остальных карт берется из серии и номера
     *
     * @return
     */
    @NonNull
    public String getFormattedOuterNumber() {
        String number;
        if (typeBsc.isMustHaveEmissionDate()) {

            if (emissionData == null)
                throw new IllegalStateException("Emission data is null");
            number = emissionData.getCardNumber();
        } else if (typeBsc == TicketStorageType.TRK || typeBsc == TicketStorageType.STR) {
            //для тройки номер должен состоять из 10 цифр, для стрелки 11
            number = getExternalNumber();
        } else {
            if (typeBsc == TicketStorageType.ETT) {
                // т.к. у ЭТТ нет серии, то возьмем только номер
                number = getExternalNumber();
            } else {
                number = getBscSeries() + " " + getExternalNumber();
            }
        }
        return number;
    }

    @Override
    public String toString() {
        return "BscInformation{" +
                "validityTime=" + validityTime +
                ", initDate=" + initDate +
                ", typeBsc=" + typeBsc +
                ", bscSeries='" + bscSeries + '\'' +
                ", externalNumber='" + externalNumber + '\'' +
                ", cardUID='" + CommonUtils.bytesToHexWithSpaces(cardUID) + '\'' +
                '}';
    }
}
