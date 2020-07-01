package ru.ppr.cppk.debug.batterytest.core;

import com.google.gson.annotations.Expose;

/**
 * Created by nevolin on 12.07.2016.
 */
public class ExecutionResult {

    @Expose
    private long millisAtStart;
    @Expose
    private long millisAtEnd;
    @Expose
    private double batteryAtStart;
    @Expose
    private double batteryAtEnd;

    public ExecutionResult() {

    }

    public long getMillisAtStart() {
        return millisAtStart;
    }

    public void setMillisAtStart(long millisAtStart) {
        this.millisAtStart = millisAtStart;
    }

    public long getMillisAtEnd() {
        return millisAtEnd;
    }

    public void setMillisAtEnd(long millisAtEnd) {
        this.millisAtEnd = millisAtEnd;
    }

    public double getBatteryAtStart() {
        return batteryAtStart;
    }

    public void setBatteryAtStart(double batteryAtStart) {
        this.batteryAtStart = batteryAtStart;
    }

    public double getBatteryAtEnd() {
        return batteryAtEnd;
    }

    public void setBatteryAtEnd(double batteryAtEnd) {
        this.batteryAtEnd = batteryAtEnd;
    }

}
