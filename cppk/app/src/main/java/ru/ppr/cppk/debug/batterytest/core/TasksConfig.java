package ru.ppr.cppk.debug.batterytest.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nevolin on 12.07.2016.
 */
public class TasksConfig implements Parcelable {

    public static class Builder {

        private long smartCardTimeoutMillis = 2000;
        private long cpuLoadMillis = 5000;
        private long waitMillis = 10000;
        private long barcodeTimeoutMillis = 5000;
        private long awakenedLoopEndSleepMillis = 1000 * 60 * 5;

        private int awakenedCycleLoops = 5;
        private int totalCycleLoops = 10000;

        public Builder setSmartCardTimeoutMillis(long smartCardTimeoutMillis) {
            this.smartCardTimeoutMillis = smartCardTimeoutMillis;

            return this;
        }

        public Builder setCpuLoadMillis(long cpuLoadMillis) {
            this.cpuLoadMillis = cpuLoadMillis;

            return this;
        }

        public Builder setWaitMillis(long waitMillis) {
            this.waitMillis = waitMillis;

            return this;
        }

        public Builder setBarcodeTimeoutMillis(long barcodeTimeoutMillis) {
            this.barcodeTimeoutMillis = barcodeTimeoutMillis;

            return this;
        }

        public Builder setAwakenedLoopEndSleepMillis(long awakenedLoopEndSleepMillis) {
            this.awakenedLoopEndSleepMillis = awakenedLoopEndSleepMillis;

            return this;
        }

        public Builder setAwakenedCycleLoops(int awakenedCycleLoops) {
            this.awakenedCycleLoops = awakenedCycleLoops;

            return this;
        }

        public Builder setTotalCycleLoops(int totalCycleLoops) {
            this.totalCycleLoops = totalCycleLoops;

            return this;
        }

        public TasksConfig build() {
            return new TasksConfig(this);
        }
    }

    public static final Creator<TasksConfig> CREATOR = new Creator<TasksConfig>() {
        @Override
        public TasksConfig createFromParcel(Parcel in) {
            return new TasksConfig(in);
        }

        @Override
        public TasksConfig[] newArray(int size) {
            return new TasksConfig[size];
        }
    };

    private final long smartCardTimeoutMillis;
    private final long cpuLoadMillis;
    private final long waitMillis;
    private final long barcodeTimeoutMillis;
    private final long awakenedLoopEndSleepMillis;

    private final int awakenedCycleLoops;
    private final int totalCycleLoops;

    private TasksConfig(Builder builder) {
        smartCardTimeoutMillis = builder.smartCardTimeoutMillis;
        cpuLoadMillis = builder.cpuLoadMillis;
        waitMillis = builder.waitMillis;
        barcodeTimeoutMillis = builder.barcodeTimeoutMillis;
        awakenedLoopEndSleepMillis = builder.awakenedLoopEndSleepMillis;

        awakenedCycleLoops = builder.awakenedCycleLoops;
        totalCycleLoops = builder.totalCycleLoops;
    }

    private TasksConfig(Parcel in) {
        smartCardTimeoutMillis = in.readLong();
        cpuLoadMillis = in.readLong();
        waitMillis = in.readLong();
        barcodeTimeoutMillis = in.readLong();
        awakenedLoopEndSleepMillis = in.readLong();

        awakenedCycleLoops = in.readInt();
        totalCycleLoops = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(smartCardTimeoutMillis);
        dest.writeLong(cpuLoadMillis);
        dest.writeLong(waitMillis);
        dest.writeLong(barcodeTimeoutMillis);
        dest.writeLong(awakenedLoopEndSleepMillis);
        dest.writeInt(awakenedCycleLoops);
        dest.writeInt(totalCycleLoops);
    }

    public long getSmartCardTimeoutMillis() {
        return smartCardTimeoutMillis;
    }

    public long getCpuLoadMillis() {
        return cpuLoadMillis;
    }

    public long getWaitMillis() {
        return waitMillis;
    }

    public long getBarcodeTimeoutMillis() {
        return barcodeTimeoutMillis;
    }

    public long getAwakenedLoopEndSleepMillis() {
        return awakenedLoopEndSleepMillis;
    }

    public int getAwakenedCycleLoops() {
        return awakenedCycleLoops;
    }

    public int getTotalCycleLoops() {
        return totalCycleLoops;
    }

}
