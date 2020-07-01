package ru.ppr.chit.domain.model.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.chit.domain.BuildConfig;
import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.domain.model.HashMapProperties;
import ru.ppr.core.domain.model.RfidType;
import ru.ppr.core.domain.model.TypedProperties;

/**
 * Параметры приложения (Key-Value).
 *
 * @author Aleksandr Brazhkin
 */
public class AppProperties extends TypedProperties {

    private static class Entity {
        private static final String EDS_TYPE = "EDS_TYPE";
        private static final String DEVICE_ID = "DEVICE_ID";
        private static final String BARCODE_TYPE = "BARCODE_TYPE";
        private static final String RFID_TYPE = "RFID_TYPE";
    }

    private static class Default {
        private static final EdsType EDS_TYPE = BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? EdsType.SFT : EdsType.STUB;
        private static final Long DEVICE_ID = null;
        private static final BarcodeType BARCODE_TYPE = BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? BarcodeType.MDI3100 : BarcodeType.FILE;
        private static final RfidType RFID_TYPE = BuildConfig.USE_REAL_DEVICES_BY_DEFAULT ? RfidType.REAL : RfidType.FILE;
    }

    public AppProperties() {
        super(new HashMapProperties());
    }

    @NonNull
    public EdsType getEdsType() {
        Integer rawValue = getInteger(Entity.EDS_TYPE, null);
        EdsType edsType = rawValue == null ? Default.EDS_TYPE : EdsType.valueOf(rawValue);
        return edsType == null ? Default.EDS_TYPE : edsType;
    }

    public void setEdsType(@Nullable EdsType edsType) {
        Integer rawValue = edsType == null ? null : edsType.getCode();
        setInteger(Entity.EDS_TYPE, rawValue);
    }

    @Nullable
    public Long getDeviceId() {
        return getLong(Entity.DEVICE_ID, Default.DEVICE_ID);
    }

    public void setDeviceId(@Nullable Long deviceId) {
        setLong(Entity.DEVICE_ID, deviceId);
    }

    @NonNull
    public BarcodeType getBarcodeType() {
        Integer rawValue = getInteger(Entity.BARCODE_TYPE, Default.BARCODE_TYPE.getCode());
        BarcodeType barcodeType = BarcodeType.valueOf(rawValue);
        return barcodeType == null ? Default.BARCODE_TYPE : barcodeType;
    }

    public void setBarcodeType(@Nullable BarcodeType barcodeType) {
        Integer rawValue = barcodeType == null ? null : barcodeType.getCode();
        setInteger(Entity.BARCODE_TYPE, rawValue);
    }

    @NonNull
    public RfidType getRfidType() {
        Integer rawValue = getInteger(Entity.RFID_TYPE, Default.RFID_TYPE.getCode());
        RfidType rfidType = RfidType.valueOf(rawValue);
        return rfidType == null ? Default.RFID_TYPE : rfidType;
    }

    public void setRfidType(@Nullable RfidType rfidType) {
        Integer rawValue = rfidType == null ? null : rfidType.getCode();
        setInteger(Entity.RFID_TYPE, rawValue);
    }

}
