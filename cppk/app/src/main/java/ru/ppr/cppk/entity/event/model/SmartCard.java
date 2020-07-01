package ru.ppr.cppk.entity.event.model;

import android.os.Parcel;
import android.os.Parcelable;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * БСК
 */
public class SmartCard implements Parcelable {

    private long id;

    /**
     * Внешний номер карты без пробелов.
     * Для ЭТТ содержит 14 цифр/
     * Для ТРОЙКИ/СТРЕЛКИ 10 цифр
     * Для карт СКМ, СКМО, ИПК берется из эмисионных данных
     */
    private String outerNumber;

    private String crystalSerialNumber;

    private TicketStorageType type;

    private int usageCount;

    /**
     * Отсчитывающийся начиная с 0 номер дорожки на которую записан билет.
     * Может быть пустым, если карта была переполнена и билет был распечатан на чеке из-за этого
     */
    private Integer track;

    private String issuer;

    //билеты записанные на льготной карте
    private long presentTicket1Id;
    private long presentTicket2Id;

    private ParentTicketInfo PresentTicket1;
    private ParentTicketInfo PresentTicket2;

    public void setPresentTicket2(ParentTicketInfo presentTicket2) {
        PresentTicket2 = presentTicket2;
    }

    public void setPresentTicket1(ParentTicketInfo presentTicket1) {
        PresentTicket1 = presentTicket1;
    }

    public ParentTicketInfo getPresentTicket1() {
        ParentTicketInfo local = PresentTicket1;

        if (local == null && getPresentTicket1Id() > 0) {
            synchronized (this) {
                if (PresentTicket1 == null)
                    PresentTicket1 = Dagger.appComponent().localDaoSession().getParentTicketInfoDao().load(presentTicket1Id);
            }

            return PresentTicket1;
        }

        return local;
    }

    public ParentTicketInfo getPresentTicket2() {
        ParentTicketInfo local = PresentTicket2;

        if (local == null && getPresentTicket2Id() > 0) {
            synchronized (this) {
                if (PresentTicket2 == null)
                    PresentTicket2 = Dagger.appComponent().localDaoSession().getParentTicketInfoDao().load(presentTicket2Id);
            }

            return PresentTicket2;
        }

        return local;
    }

    public static Parcelable.Creator<SmartCard> CREATOR = new Parcelable.Creator<SmartCard>() {

        @Override
        public SmartCard createFromParcel(Parcel source) {
            return new SmartCard(source);
        }

        @Override
        public SmartCard[] newArray(int size) {
            return new SmartCard[size];
        }
    };

    public SmartCard() {
    }

    private SmartCard(Parcel parcel) {
        setId(parcel.readLong());
        setOuterNumber(parcel.readString());
        setType((TicketStorageType) parcel.readSerializable());
        setUsageCount(parcel.readInt());
        int track = parcel.readInt();
        setTrack(track == -1 ? null : track);
        setPresentTicket1Id(parcel.readLong());
        setPresentTicket2Id(parcel.readLong());
        setCrystalSerialNumber(parcel.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        dest.writeString(getOuterNumber());
        dest.writeSerializable(getType());
        dest.writeInt(getUsageCount());
        Integer track = getTrack();
        dest.writeInt(track == null ? -1 : track);
        dest.writeLong(getPresentTicket1Id());
        dest.writeLong(getPresentTicket2Id());
        dest.writeString(getCrystalSerialNumber());
    }

    /**
     * локальный id
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Внешний номер БСК. Идентификатор чипа карты. Задается при выпуске карты.
     * Необходимо прогонять номер считанный с карты через функцию унификации
     * номеров для разных типов карт.
     */
    public String getOuterNumber() {
        return outerNumber;
    }

    public void setOuterNumber(String outerNumber) {
        this.outerNumber = outerNumber;
    }

    /**
     * Номер кристалла (UID) БСК в виде числа. Уникальный идентификатор кристалла. Задается
     * при выпуске карты.
     */
    public String getCrystalSerialNumber() {
        return crystalSerialNumber;
    }

    public void setCrystalSerialNumber(String crystalSerialNumber) {
        this.crystalSerialNumber = crystalSerialNumber;
    }

    public TicketStorageType getType() {
        return type;
    }

    public void setType(TicketStorageType type) {
        this.type = type;
    }

    /**
     * Счетчик использования карты. Исходное значение 0, увеличивается каждый
     * раз при прикладывании к турникету. Для подсистемы анализа.
     */
    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    /**
     * Отсчитывающийся начиная с 0 номер дорожки на которую записан билет. Может
     * быть пустым, если карта была переполнена и билет был распечатан на чеке
     * из-за этого
     */
    public Integer getTrack() {
        return track;
    }

    public void setTrack(Integer track) {
        this.track = track;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getPresentTicket1Id() {
        return presentTicket1Id;
    }

    public void setPresentTicket1Id(long presentTicket1Id) {
        this.presentTicket1Id = presentTicket1Id;
    }

    public long getPresentTicket2Id() {
        return presentTicket2Id;
    }

    public void setPresentTicket2Id(long presentTicket2Id) {
        this.presentTicket2Id = presentTicket2Id;
    }

    @Override
    public String toString() {
        return "SmartCard{" +
                "id=" + id +
                ", outerNumber='" + outerNumber + '\'' +
                ", crystalSerialNumber='" + crystalSerialNumber + '\'' +
                ", type=" + type +
                ", usageCount=" + usageCount +
                ", track=" + track +
                ", issuer='" + issuer + '\'' +
                ", presentTicket1Id=" + presentTicket1Id +
                ", presentTicket2Id=" + presentTicket2Id +
                ", PresentTicket1=" + PresentTicket1 +
                ", PresentTicket2=" + PresentTicket2 +
                '}';
    }
}
