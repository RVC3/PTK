package ru.ppr.cppk.dataCarrier.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Метка прохода
 *
 * @author Артём Ушаков
 */
public class PassageMark implements Parcelable {

    /**
     * Номер турникета на станции, через который был совершен проход по ПД. На ПТК записывается 255. При продаже - 0.
     */
    public static final int GATE_NUMBER_FOR_PTK = 255;
    private Integer versionMark = -1;
    private Integer counterCard = Integer.MAX_VALUE;
    /**
     * В миилисекундах, время прохода по ПД1
     */
    private Long timeOne = -1L;
    /**
     * В миилисекундах, время прохода по ПД2
     */
    private Long timeTwo = -1L;
    /**
     * Количество секунд от момента оформления ПД №1 до момента последнего прохода через турникет по ПД №1. При стирании с БСК ПД №1 данное поле должно устанавливаться в 0. До 776 дней.
     */
    private Long secondsFromSalePd1 = -1L;
    /**
     * Количество секунд от момента оформления ПД №2 до момента последнего прохода через турникет по ПД №2. При стирании с БСК ПД №1 данное поле должно устанавливаться в 0. До 776 дней.
     */
    private Long secondsFromSalePd2 = -1L;
    private Integer station = -1;
    private Integer turniketForOnePd = -1;
    private Integer turniketForTwoPd = -1;
    private Integer directionForOne = -1;
    private Integer directionTwo = -1;
    /**
     * 0 (false) - по ПД №1 не было ни одного прохода. 1 (true) - по ПД №1 был хот я бы 1 проход.
     */
    private Boolean useOne = false;
    /**
     * 0 (false) - по ПД №2 не было ни одного прохода. 1 (true) - по ПД №2 был хот я бы 1 проход.
     */
    private Boolean useTwo = false;
    /**
     * Признак привязки БСК к пассажиру
     */
    private Boolean boundToPassenger = false;


    private byte[] parseData;

    public PassageMark(Parcel sourceParcelable) {
        versionMark = sourceParcelable.readInt();
        counterCard = sourceParcelable.readInt();
        timeOne = sourceParcelable.readLong();
        timeTwo = sourceParcelable.readLong();
        secondsFromSalePd1 = sourceParcelable.readLong();
        secondsFromSalePd2 = sourceParcelable.readLong();
        station = sourceParcelable.readInt();
        turniketForOnePd = sourceParcelable.readInt();
        turniketForTwoPd = sourceParcelable.readInt();
        directionForOne = sourceParcelable.readInt();
        directionTwo = sourceParcelable.readInt();
        byte useOneByte = sourceParcelable.readByte();
        byte useTwoByte = sourceParcelable.readByte();
        useOne = useOneByte == 1;
        useTwo = useTwoByte == 1;
        boundToPassenger = sourceParcelable.readInt() == 1;
    }

    public static final Parcelable.Creator<PassageMark> CREATOR = new Parcelable.Creator<PassageMark>() {

        @Override
        public PassageMark createFromParcel(Parcel source) {

            return new PassageMark(source);
        }

        @Override
        public PassageMark[] newArray(int size) {

            return new PassageMark[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(versionMark);
        dest.writeInt(counterCard);
        dest.writeLong(timeOne);
        dest.writeLong(timeTwo);
        dest.writeLong(secondsFromSalePd1);
        dest.writeLong(secondsFromSalePd2);
        dest.writeInt(station);
        dest.writeInt(turniketForOnePd);
        dest.writeInt(turniketForTwoPd);
        dest.writeInt(directionForOne);
        dest.writeInt(directionTwo);
        dest.writeByte(useOne ? (byte) 1 : 0);
        dest.writeByte(useTwo ? (byte) 1 : 0);
        dest.writeInt(boundToPassenger ? 1 : 0);
    }

    public PassageMark() {
    }

    public PassageMark(byte[] data, int version) {
        this.parseData = data;
        this.versionMark = version;
    }

    /**
     * Возвращает версию метки
     *
     * @return
     */
    public Integer getVersionMark() {
        return versionMark;
    }

    /**
     * Вовзаращет последнее показание счетчки при проходе
     *
     * @return
     */
    public Integer getCounterCard() {
        return counterCard;
    }

    /**
     * Возвращает количество секунд от момента оформления ПД №1 до момента последнего прохода через турникет по ПД №1
     * в секундах
     *
     * @return
     */
    public Long getSecondsFromSalePd1() {
        return secondsFromSalePd1;
    }

    /**
     * Возвращает количество секунд от момента оформления ПД №2 до момента последнего прохода через турникет по ПД №2
     * в секундах
     *
     * @return
     */
    public Long getSecondsFromSalePd2() {
        return secondsFromSalePd2;
    }

    /**
     * Возвращает timestamp для ПД № 1
     * в секундах
     *
     * @return
     */
    public Long getTimeOneInSecond() {
        return timeOne / 1000;
    }

    /**
     * Возвращает timestamp для ПД № 2
     * в секундах
     *
     * @return
     */
    public Long getTimeTwoInSecond() {
        return timeTwo / 1000;
    }

    /**
     * Вовзращает код станции прохода
     *
     * @return
     */
    public Integer getStation() {
        return station;
    }

    /**
     * Возвращает массив байтов метки
     *
     * @return
     */
    public byte[] getParseData() {
        return parseData;
    }

    public void setVersion(int version) {
        this.versionMark = version;
    }


    public void setSecondsFromSalePd1(Long secondsFromSalePd1) {
        this.secondsFromSalePd1 = secondsFromSalePd1;
    }

    public void setSecondsFromSalePd2(Long secondsFromSalePd2) {
        this.secondsFromSalePd2 = secondsFromSalePd2;
    }

    public Long getTimeOne() {
        return timeOne;
    }

    public void setTimeOne(Long timeOne) {
        this.timeOne = timeOne;
    }

    public Long getTimeTwo() {
        return timeTwo;
    }

    public void setTimeTwo(Long timeTwo) {
        this.timeTwo = timeTwo;
    }

    public void setCounterCard(Integer counterCard) {
        this.counterCard = counterCard;
    }

    public void setStation(Integer station) {
        this.station = station;
    }

    public void setParseData(byte[] parseData) {
        this.parseData = parseData;
    }

    public Integer getTurniketForOnePd() {
        return turniketForOnePd;
    }

    public void setTurniketForOnePd(Integer turniketForOnePd) {
        this.turniketForOnePd = turniketForOnePd;
    }

    public Integer getTurniketForTwoPd() {
        return turniketForTwoPd;
    }

    public void setTurniketForTwoPd(Integer turniketForTwoPd) {
        this.turniketForTwoPd = turniketForTwoPd;
    }

    public Integer getDirectionForOne() {
        return directionForOne;
    }

    public void setDirectionForOne(Integer directionForOne) {
        this.directionForOne = directionForOne;
    }

    public Integer getDirectionTwo() {
        return directionTwo;
    }

    public void setDirectionTwo(Integer directionTwo) {
        this.directionTwo = directionTwo;
    }

    public Boolean getUseOne() {
        return useOne;
    }

    public void setUseOne(Boolean useOne) {
        this.useOne = useOne;
    }

    public Boolean getUseTwo() {
        return useTwo;
    }

    public void setUseTwo(Boolean useTwo) {
        this.useTwo = useTwo;
    }

    public Boolean getBoundToPassenger() {
        return boundToPassenger;
    }

    public void setBoundToPassenger(Boolean boundToPassenger) {
        this.boundToPassenger = boundToPassenger;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * v < 4 - Время прохода в секундах
     * v = 5 - Количество секунд от момента оформления ПД до момента последнего прохода через турникет по ПД
     */
    public long getTimeForPd(int pdNumber) {
        if (pdNumber == 0)
            return getTimeOne();
        else
            return getTimeTwo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PassageMark that = (PassageMark) o;

        if (!versionMark.equals(that.versionMark)) return false;
        if (!counterCard.equals(that.counterCard)) return false;
        if (!timeOne.equals(that.timeOne)) return false;
        if (!timeTwo.equals(that.timeTwo)) return false;
        if (!secondsFromSalePd1.equals(that.secondsFromSalePd1)) return false;
        if (!secondsFromSalePd2.equals(that.secondsFromSalePd2)) return false;
        if (!station.equals(that.station)) return false;
        if (!turniketForOnePd.equals(that.turniketForOnePd)) return false;
        if (!turniketForTwoPd.equals(that.turniketForTwoPd)) return false;
        if (!directionForOne.equals(that.directionForOne)) return false;
        if (!directionTwo.equals(that.directionTwo)) return false;
        if (!useOne.equals(that.useOne)) return false;
        if (!useTwo.equals(that.useTwo)) return false;
        return boundToPassenger.equals(that.boundToPassenger);

    }

    @Override
    public int hashCode() {
        int result = versionMark.hashCode();
        result = 31 * result + counterCard.hashCode();
        result = 31 * result + timeOne.hashCode();
        result = 31 * result + timeTwo.hashCode();
        result = 31 * result + secondsFromSalePd1.hashCode();
        result = 31 * result + secondsFromSalePd2.hashCode();
        result = 31 * result + station.hashCode();
        result = 31 * result + turniketForOnePd.hashCode();
        result = 31 * result + turniketForTwoPd.hashCode();
        result = 31 * result + directionForOne.hashCode();
        result = 31 * result + directionTwo.hashCode();
        result = 31 * result + useOne.hashCode();
        result = 31 * result + useTwo.hashCode();
        result = 31 * result + boundToPassenger.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PassageMark{" +
                "versionMark=" + versionMark +
                ", counterCard=" + counterCard +
                ", timeOne=" + timeOne +
                ", timeTwo=" + timeTwo +
                ", secondsFromSalePd1=" + secondsFromSalePd1 +
                ", secondsFromSalePd2=" + secondsFromSalePd2 +
                ", station=" + station +
                ", turniketForOnePd=" + turniketForOnePd +
                ", turniketForTwoPd=" + turniketForTwoPd +
                ", directionForOne=" + directionForOne +
                ", directionTwo=" + directionTwo +
                ", useOne=" + useOne +
                ", useTwo=" + useTwo +
                ", boundToPassenger=" + boundToPassenger +
                '}';
    }
}
