package ru.ppr.core.domain.model;

/**
 * Информация об устройстве.
 *
 * @author Aleksandr Brazhkin
 */
public final class DeviceInfo {

    private final String androidDisplayedVersion;
    private final int androidSdkVersion;
    private final String buildDisplayedVersion;
    private final String deviceBrand;
    private final String deviceModel;

    public DeviceInfo(String androidDisplayedVersion, int androidSdkVersion, String buildDisplayedVersion, String deviceBrand, String deviceModel) {
        this.androidDisplayedVersion = androidDisplayedVersion;
        this.androidSdkVersion = androidSdkVersion;
        this.buildDisplayedVersion = buildDisplayedVersion;
        this.deviceBrand = deviceBrand;
        this.deviceModel = deviceModel;
    }

    public String getAndroidDisplayedVersion() {
        return androidDisplayedVersion;
    }

    public int getAndroidSDKVersion() {
        return androidSdkVersion;
    }

    public String getBuildDisplayedVersion() {
        return buildDisplayedVersion;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }
}
