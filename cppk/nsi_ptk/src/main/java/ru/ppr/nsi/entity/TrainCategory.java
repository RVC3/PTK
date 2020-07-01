package ru.ppr.nsi.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Класс для представления категории поезда.
 */
public class TrainCategory implements Parcelable {

    //такой дикий хардкод из-за того что у нас настраивается только префикс поезда а не сама категория
    //при продаже делаем так:
    //Наталья Рзянкина:
    //Тогда предлагаю делать так: выбирать тариф на скорый поезд (где категория поезда 7000),
    //отбрасывать тарифы на доплату, и из оставшихся тарифов (если их несколько) выбрать тот, где стоимость меньше

    public enum TrainCategoryCategory {

        Category_O("О", "Пассажирский"),
        Category_7("7", "Скорый"),
        Category_C("С", "Спутник");

        private String category = "О";
        private String description = "Пассажирский";

        public String getCategory() {
            return category;
        }

        static public TrainCategoryCategory getType(String category) {
            for (TrainCategoryCategory type : TrainCategoryCategory.values()) {
                if (type.getCategory().equals(category)) {
                    return type;
                }
            }
            return TrainCategoryCategory.Category_O;
        }

        TrainCategoryCategory(String category, String description) {
            this.category = category;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static final int CATEGORY_CODE_O = 1;
    public static final int CATEGORY_CODE_7 = 2;
    public static final int CATEGORY_CODE_C = 3;
    public static final int CATEGORY_CODE_M = 6;

    public TrainCategory() {
    }

    protected TrainCategory(Parcel in) {
        description = in.readString();
        category = in.readString();
        name = in.readString();
        code = in.readInt();
        dataChecksum = in.createByteArray();
        versionId = in.readInt();
        changedDateTime = in.readLong();
        prefix = in.readInt() == TrainCategoryPrefix.PASSENGER.getCode() ? TrainCategoryPrefix.PASSENGER : TrainCategoryPrefix.EXPRESS;
    }

    public String description = null;
    public String category = null;
    public String name;
    public int code;
    public byte[] dataChecksum = null;
    private int versionId;
    public long changedDateTime;
    public TrainCategoryPrefix prefix = TrainCategoryPrefix.PASSENGER;

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public static TrainCategory getDefault() {
        TrainCategory trainCategory = new TrainCategory();
        trainCategory.category = "О";
        trainCategory.name = "Пассажирский";
        trainCategory.prefix = TrainCategoryPrefix.PASSENGER;
        trainCategory.description = "Пригородные пассажирские поезда";
        trainCategory.code = 1;

        return trainCategory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(name);
        dest.writeInt(code);
        dest.writeByteArray(dataChecksum == null ? new byte[0] : dataChecksum);
        dest.writeInt(versionId);
        dest.writeLong(changedDateTime);
        dest.writeInt(prefix.getCode());
    }

    public static final Creator<TrainCategory> CREATOR = new Creator<TrainCategory>() {
        @Override
        public TrainCategory createFromParcel(Parcel in) {
            return new TrainCategory(in);
        }

        @Override
        public TrainCategory[] newArray(int size) {
            return new TrainCategory[size];
        }
    };

}
