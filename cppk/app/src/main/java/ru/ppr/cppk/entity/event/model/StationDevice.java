package ru.ppr.cppk.entity.event.model;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.nsi.entity.DeviceType;

public class StationDevice {

    public static final String STUB_SERIAL_NUMBER = "StubSerialNumber";

    /**
     * локальный id - первичный ключ
     */
    private long id = 0;

    /**
     * Id устройства. для выгрузки в АРМ
     * Значение по умолчанию используется в случае если метод, возвращающий информацию по ключу эцп,
     * завершился с ошибкой
     * <p/>
     * <p/>
     * Номер ПТК, или любого другого устройства, уникален для всего полигона. Точно такой же как в сущности CashRegister
     */
    private long deviceId = 0;

    /**
     * Текстовое представление модели (например, "FPrint-55K")
     */
    private String model = null;

    /**
     * Серийный номер устройства
     */
    private String serialNumber = null;

    /**
     * Код типа терминального устройства
     */
    private DeviceType type = null;

    /**
     * Код участка
     */
    private int productionSectionCode = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public int getProductionSectionCode() {
        return productionSectionCode;
    }

    public void setProductionSectionCode(int productionSectionCode) {
        this.productionSectionCode = productionSectionCode;
    }

    /**
     * Вернет стандартный объект девайса для текущего состояния ПТК
     */
    public static StationDevice getThisDevice() {
        PrivateSettings privateSettings = Di.INSTANCE.getPrivateSettings().get();
        String serialNumber = SharedPreferencesUtils.getSerialNumber(Di.INSTANCE.getApp());
        String model = SharedPreferencesUtils.getModel(Di.INSTANCE.getApp());

        StationDevice stationDevice = new StationDevice();
        stationDevice.setModel(model); //http://agile.srvdev.ru/browse/CPPKPP-35482
        stationDevice.setSerialNumber(serialNumber); //https://aj.srvdev.ru/browse/CPPKPP-25800
        stationDevice.setType(DeviceType.Ptk);
        stationDevice.setDeviceId(privateSettings.getTerminalNumber());
        stationDevice.setProductionSectionCode(privateSettings.getProductionSectionId());
        return stationDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StationDevice that = (StationDevice) o;

        if (id != that.id) return false;
        if (deviceId != that.deviceId) return false;
        if (productionSectionCode != that.productionSectionCode) return false;
        if (model != null ? !model.equals(that.model) : that.model != null) return false;
        if (serialNumber != null ? !serialNumber.equals(that.serialNumber) : that.serialNumber != null)
            return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (deviceId ^ (deviceId >>> 32));
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + productionSectionCode;
        return result;
    }
}
