package ru.ppr.core.manager.eds;

import android.support.annotation.NonNull;

import java.util.EnumSet;

import ru.ppr.core.domain.model.EdsType;
import ru.ppr.edssft.LicType;

/**
 * Конфигурация sft.
 *
 * @author Aleksandr Brazhkin
 */
public class EdsConfig {
    /**
     * Тип СФТ
     */
    private final EdsType edsType;
    /**
     * Идентификатор устройства
     */
    private final long deviceId;
    /**
     * Рабочие директории СФТ
     */
    private final EdsDirs edsDirs;
    /**
     * Типы лицензий, которые требуются для работы
     */
    private final EnumSet<LicType> licTypes;

    public EdsConfig(@NonNull EdsType edsType, long deviceId, @NonNull EdsDirs edsDirs, @NonNull EnumSet<LicType> licTypes) {
        this.edsType = edsType;
        this.deviceId = deviceId;
        this.edsDirs = edsDirs;
        this.licTypes = licTypes;
    }

    @NonNull
    public EdsType edsType() {
        return edsType;
    }

    public long deviceId() {
        return deviceId;
    }

    @NonNull
    public EdsDirs edsDirs() {
        return edsDirs;
    }

    @NonNull
    public EnumSet<LicType> getLicTypes() {
        return licTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EdsConfig edsConfig = (EdsConfig) o;

        if (deviceId != edsConfig.deviceId) return false;
        if (edsType != edsConfig.edsType) return false;
        if (!edsDirs.equals(edsConfig.edsDirs)) return false;
        return licTypes.equals(edsConfig.licTypes);
    }

    @Override
    public int hashCode() {
        int result = edsType.hashCode();
        result = 31 * result + (int) (deviceId ^ (deviceId >>> 32));
        result = 31 * result + edsDirs.hashCode();
        result = 31 * result + licTypes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EdsConfig{" +
                "edsType=" + edsType +
                ", deviceId=" + deviceId +
                ", edsDirs=" + edsDirs +
                ", licTypes=" + licTypes +
                '}';
    }
}
