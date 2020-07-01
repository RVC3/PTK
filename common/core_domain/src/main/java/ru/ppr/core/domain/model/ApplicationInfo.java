package ru.ppr.core.domain.model;

/**
 * Информация о приложении.
 *
 * @author Aleksandr Brazhkin
 */
public class ApplicationInfo {

    private final boolean debug;
    private final String applicationId;
    private final String buildType;
    private final String flavor;
    private final int versionCode;
    private final String versionName;

    public ApplicationInfo(boolean debug, String applicationId, String buildType, String flavor, int versionCode, String versionName) {
        this.debug = debug;
        this.applicationId = applicationId;
        this.buildType = buildType;
        this.flavor = flavor;
        this.versionCode = versionCode;
        this.versionName = versionName;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getBuildType() {
        return buildType;
    }

    public String getFlavor() {
        return flavor;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }
}
