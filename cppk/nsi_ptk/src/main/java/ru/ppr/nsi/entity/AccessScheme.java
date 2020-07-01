package ru.ppr.nsi.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ru.ppr.nsi.NsiDaoSession;

/**
 * Схема доступа.
 *
 * @author Aleksandr Brazhkin
 */
public class AccessScheme extends BaseNSIObject<Integer> {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SAM_SLOT_NUMBER_1,
            SAM_SLOT_NUMBER_2})
    public @interface SamSlotNumber {
    }

    public static final int SAM_SLOT_NUMBER_1 = 0;
    public static final int SAM_SLOT_NUMBER_2 = 1;

    /**
     * Название схемы
     */
    private String name = null;
    /**
     * Код типа устройства на полигоне, для которого схема
     */
    private int deviceTypeCode = 0;
    /**
     * Типа устройства на полигоне
     */
    private DeviceType deviceType = null;
    /**
     * Код типа носителя билета
     */
    private int ticketStorageTypeCode = 0;
    /**
     * Тип носителя билета
     */
    private TicketStorageType ticketStorageType;
    /**
     * Приоритет схемы
     */
    private int priority = 0;
    /**
     * Непонятно что это, похоже что атавизм
     */
    private int smartCardStorageType = 0;
    /**
     * Номер слота sam-модуля
     */
    @SamSlotNumber
    private int samSlotNumber = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeviceTypeCode() {
        return deviceTypeCode;
    }

    public void setDeviceTypeCode(int deviceTypeCode) {
        this.deviceTypeCode = deviceTypeCode;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public int getTicketStorageTypeCode() {
        return ticketStorageTypeCode;
    }

    public void setTicketStorageTypeCode(int ticketStorageTypeCode) {
        this.ticketStorageTypeCode = ticketStorageTypeCode;
    }

    public TicketStorageType getTicketStorageType(NsiDaoSession nsiDaoSession) {
        if (ticketStorageType == null)
            ticketStorageType = TicketStorageType.getTypeByDBCode(ticketStorageTypeCode);
        return ticketStorageType;
    }

    public void setTicketStorageType(TicketStorageType ticketStorageType) {
        this.ticketStorageType = ticketStorageType;
        ticketStorageTypeCode = ticketStorageType == null ? 0 : ticketStorageType.getDBCode();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getSmartCardStorageType() {
        return smartCardStorageType;
    }

    public void setSmartCardStorageType(int smartCardStorageType) {
        this.smartCardStorageType = smartCardStorageType;
    }

    @SamSlotNumber
    public int getSamSlotNumber() {
        return samSlotNumber;
    }

    public void setSamSlotNumber(@SamSlotNumber int samSlotNumber) {
        this.samSlotNumber = samSlotNumber;
    }
}
