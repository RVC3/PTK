package ru.ppr.cppk.ui.activity.controlreadbsc.model;

import android.os.Parcel;
import android.os.Parcelable;

import ru.ppr.cppk.ui.activity.controlreadbsc.ControlReadBscActivity;
import ru.ppr.cppk.ui.activity.readpdfortransfer.ReadPdForTransferActivity;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;

/**
 * Параметры для запуска {@link ControlReadBscActivity}.
 *
 * @author Aleksandr Brazhkin
 */
public class ControlReadBscParams implements Parcelable {
    /**
     * Флаг необходимости увеличения счетчика использования карты в метке прохода.
     */
    private boolean incrementPmHwUsageCounter;
    /**
     * Параметры для запуска {@link ReadPdForTransferActivity}
     */
    private ReadForTransferParams readForTransferParams;

    public boolean isIncrementPmHwUsageCounter() {
        return incrementPmHwUsageCounter;
    }

    public void setIncrementPmHwUsageCounter(boolean incrementPmHwUsageCounter) {
        this.incrementPmHwUsageCounter = incrementPmHwUsageCounter;
    }

    public ReadForTransferParams getReadForTransferParams() {
        return readForTransferParams;
    }

    public void setReadForTransferParams(ReadForTransferParams readForTransferParams) {
        this.readForTransferParams = readForTransferParams;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.incrementPmHwUsageCounter ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.readForTransferParams, flags);
    }

    public ControlReadBscParams() {
    }

    protected ControlReadBscParams(Parcel in) {
        this.incrementPmHwUsageCounter = in.readByte() != 0;
        this.readForTransferParams = in.readParcelable(ReadForTransferParams.class.getClassLoader());
    }

    public static final Creator<ControlReadBscParams> CREATOR = new Creator<ControlReadBscParams>() {
        @Override
        public ControlReadBscParams createFromParcel(Parcel source) {
            return new ControlReadBscParams(source);
        }

        @Override
        public ControlReadBscParams[] newArray(int size) {
            return new ControlReadBscParams[size];
        }
    };
}
