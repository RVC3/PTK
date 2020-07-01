package ru.ppr.cppk.localdb.model;

import com.google.gson.annotations.SerializedName;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Кассовый аппарат. Не должен выгружаться для чужих билетов. Только для ПД проданных на данном ПТК.
 *
 * @author Ivan Bachtin
 */
public class CashRegister implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private transient Long id;
    /**
     * Текстовое представление модели (например, "FPrint-55K")
     */
    @SerializedName("Model")
    private String model;
    /**
     * Серийный номер устройства
     */
    @SerializedName("SerialNumber")
    private String serialNumber;
    /**
     * ИНН (если устройство фискализированно). Для конторля не заполнять
     */
    @SerializedName("INN")
    private String INN = null;
    /**
     * Серийный номер ЭКЛЗ. Для контроля не заполнять.
     */
    @SerializedName("EKLZNumber")
    private String EKLZNumber = null;
    /**
     * Номер ФН
     */
    @SerializedName("FNSerial")
    private String FNSerial = null;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getINN() {
        return INN;
    }

    public void setINN(String INN) {
        this.INN = INN;
    }

    public String getEKLZNumber() {
        return EKLZNumber;
    }

    public void setEKLZNumber(String EKLZNumber) {
        this.EKLZNumber = EKLZNumber;
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

    public String getFNSerial() {
        return FNSerial;
    }

    public void setFNSerial(String FNSerial) {
        this.FNSerial = FNSerial;
    }

    @Override
    public String toString() {
        return "CashRegister{" +
                "id=" + id +
                ", model='" + model + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", INN='" + INN + '\'' +
                ", EKLZNumber='" + EKLZNumber + '\'' +
                ", FNSerial='" + FNSerial + '\'' +
                '}';
    }
}
