package ru.ppr.cppk.ui.activity.decrementtrip.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.v23.PdV23;
import ru.ppr.core.dataCarrier.pd.v24.PdV24;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkVersion;
import ru.ppr.nsi.entity.TrainCategoryPrefix;

/**
 * Входные данные для экрана списания поездки.
 *
 * @author Aleksandr Brazhkin
 */
public class DecrementTripParams implements Parcelable {
    /**
     * Uid карты
     */
    private final byte[] cardUid;
    /**
     * Значение счетчика до списания поедки
     */
    private final int initialHwCounterValue;
    /**
     * Показания счетчика использования карты
     * Для {@link PassageMarkVersion#V4}
     */
    private final int passageMarkUsageCounterValue;
    /**
     * Номер ПД, ассоциированного со счетчиком
     */
    private final int pdIndex;
    /**
     * Дата продажи ПД
     */
    private final Date pdSaleDate;
    /**
     * Версия ПД, по которому производится списание поездки
     */
    private final int pdVersion;
    /**
     * Категория поезда, по которой производится списание поездки
     */
    private final TrainCategoryPrefix trainCategory;
    /**
     * Флаг наличия валидного прохода на поезд 6000.
     * Используется для списания поездок по {@link PdV23},{@link PdV24}
     */
    private final boolean passage6000Valid;

    public DecrementTripParams(@Nullable byte[] cardUid,
                               int initialHwCounterValue,
                               int passageMarkUsageCounterValue,
                               int pdIndex,
                               Date pdSaleDate,
                               int pdVersion,
                               TrainCategoryPrefix trainCategory,
                               boolean passage6000Valid) {
        this.cardUid = cardUid;
        this.initialHwCounterValue = initialHwCounterValue;
        this.passageMarkUsageCounterValue = passageMarkUsageCounterValue;
        this.pdIndex = pdIndex;
        this.pdSaleDate = pdSaleDate;
        this.pdVersion = pdVersion;
        this.trainCategory = trainCategory;
        this.passage6000Valid = passage6000Valid;
    }

    public byte[] getCardUid() {
        return cardUid;
    }

    public int getInitialHwCounterValue() {
        return initialHwCounterValue;
    }

    public TrainCategoryPrefix getTrainCategory() {
        return trainCategory;
    }

    public int getPdVersion() {
        return pdVersion;
    }

    public boolean isPassage6000Valid() {
        return passage6000Valid;
    }

    public int getPassageMarkUsageCounterValue() {
        return passageMarkUsageCounterValue;
    }

    public int getPdIndex() {
        return pdIndex;
    }

    public Date getPdSaleDate() {
        return pdSaleDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.cardUid);
        dest.writeInt(this.initialHwCounterValue);
        dest.writeInt(this.passageMarkUsageCounterValue);
        dest.writeInt(this.pdIndex);
        dest.writeLong(this.pdSaleDate != null ? this.pdSaleDate.getTime() : -1);
        dest.writeInt(this.pdVersion);
        dest.writeInt(this.trainCategory == null ? -1 : this.trainCategory.ordinal());
        dest.writeByte(this.passage6000Valid ? (byte) 1 : (byte) 0);
    }

    protected DecrementTripParams(Parcel in) {
        this.cardUid = in.createByteArray();
        this.initialHwCounterValue = in.readInt();
        this.passageMarkUsageCounterValue = in.readInt();
        this.pdIndex = in.readInt();
        long tmpPdSaleDate = in.readLong();
        this.pdSaleDate = tmpPdSaleDate == -1 ? null : new Date(tmpPdSaleDate);
        this.pdVersion = in.readInt();
        int tmpTrainCategory = in.readInt();
        this.trainCategory = tmpTrainCategory == -1 ? null : TrainCategoryPrefix.values()[tmpTrainCategory];
        this.passage6000Valid = in.readByte() != 0;
    }

    public static final Creator<DecrementTripParams> CREATOR = new Creator<DecrementTripParams>() {
        @Override
        public DecrementTripParams createFromParcel(Parcel source) {
            return new DecrementTripParams(source);
        }

        @Override
        public DecrementTripParams[] newArray(int size) {
            return new DecrementTripParams[size];
        }
    };
}
