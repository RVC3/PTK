package ru.ppr.cppk.dataCarrier.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.dataCarrier.PdFromLegacyMapper;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.dataCarrier.pd.check.control.StrategyCheck;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.security.entity.TicketId;

/**
 * ПД, записанный на смарт-кату или ШК.
 */
public class PD implements Parcelable {

    public static final String TAG = PD.class.getSimpleName();

    /**
     * Версия ПД [1..127]
     */
    public Integer versionPD = null;

    /**
     * Номер ПД
     */
    public Integer numberPD = -1;

    /**
     * Направление. По умолчанию ТУДА
     */
    // public Integer direction = GlobalConstants.DIRECTION_ROUND;
    public TicketWayType wayType = TicketWayType.OneWay;

    /**
     * Код тарифа
     */
    public Long tariffCodePD = 0L;

    /**
     * Код Лльготы
     */
    public Integer exemptionCode = 0;

    /**
     * Дата продажи в секундах
     */
    public Date saleDatetimePD = null;

    /**
     * Срок действия (в днях)
     */
    public Integer term = -1;

    /**
     * Подпись 64 байта
     */
    public byte[] ecp = new byte[GlobalConstants.ECP_SIZE_BYTE]; // ЭЦП

    /**
     * Номер ключа ЭЦП
     */
    public long ecpNumberPD = 0;


    //region Последний проход
    /**
     * Дата последнего прохода по ПД
     */
    private Date lastPassageTime;
    /**
     * Флаг последнего прохода через турникет 7000 для комбинированного абонемента
     */
    private boolean lastPassageTurnstile7000;
    /**
     * Флаг валидности последнего прохода через турникет
     */
    private boolean lastPassageValid;
    /**
     * Флаг валидности последнего прохода через турникет 6000 для комбинированного абонемента
     */
    private boolean lastPassage6000Valid;
    //endregion

    public boolean issBankPaymentType() {
        return isBankPaymentType;
    }

    public void setIssBankPaymentType(boolean isBankPaymentType) {
        this.isBankPaymentType = isBankPaymentType;
    }

    /**
     * Признак безналичной оплаты
     */
    private boolean isBankPaymentType = false;

    /**
     * Признак - требуется проверка входа на другой станции при выходе на текущей станции
     */
    private boolean passageToStationCheckRequired;

    /**
     * Признак - требуется активация билета (с помощью считывания специального ШК на ИТ на станции отправления)
     */
    private boolean activationRequired = false;

    /**
     * Номер телефона
     */
    private long phoneNumber;

    /**
     * Данные ПД для проверки подписи, без информации и карте и номера ключа ЭЦП
     *
     * @return
     */
    public byte[] getEcpDataForCheck() {
        // Версия ПД 1 byte[1]
        // Порядковый номер, Сроки, условия. ПД 1 byte[3]
        // Дата и время ПД 1 byte[4]
        // Тариф ПД 1 byte[4]
        // Код льготы ПД 1 byte[2]

        if (ecpDataForCheck == null) {
            Pd newPd = new PdFromLegacyMapper().fromLegacyPd(this);
            this.ecpDataForCheck = Dagger.appComponent().pdEncoderFactory().create(newPd).encodeWithoutEdsKeyNumber(newPd);
        }

        return ecpDataForCheck;
    }

    /**
     * Подписываемые данные
     */
    private byte[] ecpDataForCheck = null;

    /**
     * ID устройства продавшего исходного ПД
     */
    public long deviceId = -1;
    /**
     * Вагон
     */
    public Integer car = -1;
    /**
     * дата и время отправления
     */
    public Long departureDatetime = -1L;
    /**
     * Номер поезда
     */
    public Integer carNumber = -1;
    /**
     * Место
     */
    public Integer place = -1;
    /**
     * Тип документа
     */
    public Integer documentType = -1;
    /**
     * номер документа
     */
    public String documentNumber = null;
    /**
     * Фамилия
     */
    public String lastname = null;
    /**
     * Инициал Имя
     */
    public String firstname = null;
    /**
     * Инициал Отчество
     */
    public String middlename = null;
    /**
     * Тариф
     */
    private Tariff tariff = null;
    /**
     * Информация о карте
     */
    private BscInformation bscInformation = null;
    /**
     * Метка прохода
     */
    private PassageMark passageMark = null;

    /**
     * порядковый номер билета на карте( 0 - 1й билет, 1 - 2й билет)
     */
    public int orderNumberPdOnCard = -1;

    public int startCountValue = -1;
    public int endCountValue = -1;

    /**
     * Дни действия абонемента для абонемента на даты.
     */
    public long actionDays = 0;
    /**
     * Ошибки, выявленные при проверке ПД.
     */
    public List<PassageResult> errors = new ArrayList<>();
    public Integer pdSizeByte = 0;
    public ParentTicketInfo parentTicketInfo = null;

    /**
     * Дата отзыва ключа ЭЦП, в секундах. Если не отозван, тогда -1
     */
    public Long revocationTime = -1L;

    public boolean hasFareTariff = false;

    private boolean isRestoredTicket;

    /**
     * Код услуги. Актуально для билета PdV21.
     */
    public Long serviceFeeCode = 0L;
    /**
     * Текущее значение ассоциированного хардварного счетчика
     */
    private Integer hwCounterValue;

    /**
     * @param versionPD  версия ПД
     * @param pdSizeByte размер ПД в байтах
     */
    public PD(int versionPD, int pdSizeByte) {
        this.versionPD = versionPD;
        this.pdSizeByte = pdSizeByte;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Вернет дату продажи ПД
     */
    public Date getSaleDate() {
        return saleDatetimePD;
    }

    public PassageMark getPassageMark() {
        return passageMark;
    }

    public void setPassageMark(PassageMark passageMark) {
        this.passageMark = passageMark;
    }

    /**
     * Вернет дату продажи в секундах
     *
     * @return
     */
    public long getSaleDateInSecond() {
        return getSaleDate().getTime() / 1000;
    }

    public static final Parcelable.Creator<PD> CREATOR = new Parcelable.Creator<PD>() {

        public PD createFromParcel(Parcel in) {

            return new PD(in);
        }

        public PD[] newArray(int size) {
            return new PD[size];
        }
    };

    @Nullable
    public Integer getHwCounterValue() {
        return hwCounterValue;
    }

    public void setHwCounterValue(@Nullable Integer hwCounterValue) {
        this.hwCounterValue = hwCounterValue;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeParcelable(getTariff(), PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(getBscInformation(), PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(passageMark, PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(parentTicketInfo, PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeInt(versionPD);
        dest.writeInt(numberPD);
        dest.writeSerializable(wayType);
        dest.writeLong(tariffCodePD);
        dest.writeInt(exemptionCode);
        dest.writeLong(saleDatetimePD.getTime());
        dest.writeInt(term);
        dest.writeLong(ecpNumberPD);
        dest.writeLong(deviceId);
        dest.writeInt(car);
        dest.writeLong(departureDatetime);
        dest.writeInt(carNumber);
        dest.writeInt(place);
        dest.writeInt(documentType);
        dest.writeString(documentNumber);
        dest.writeString(lastname);
        dest.writeString(firstname);
        dest.writeString(middlename);
        dest.writeInt(startCountValue);
        dest.writeInt(endCountValue);
        // dest.writeByteArray(ecp);
        // dest.writeByteArray(ecpDataForCheck);
        dest.writeInt(orderNumberPdOnCard);
        dest.writeInt(pdSizeByte);
        dest.writeLong(revocationTime);
        dest.writeInt(hasFareTariff ? 1 : 0);
        dest.writeLong(actionDays);
        int size = errors.size();
        int[] errorCodes = new int[size];
        for (int i = 0; i < size; i++) {
            errorCodes[i] = errors.get(i).getCode();
        }
        dest.writeInt(size);
        dest.writeIntArray(errorCodes);
        dest.writeInt(isRestoredTicket ? 1 : 0);
        dest.writeLong(serviceFeeCode);
        dest.writeValue(hwCounterValue);
        dest.writeLong(lastPassageTime == null ? 0 : lastPassageTime.getTime());
        dest.writeInt(lastPassageTurnstile7000 ? 1 : 0);
        dest.writeInt(lastPassageValid ? 1 : 0);
        dest.writeInt(lastPassage6000Valid ? 1 : 0);
    }

    private PD(Parcel in) {
        setTariff(in.readParcelable(Tariff.class.getClassLoader()));
        setBscInformation(in.readParcelable(BscInformation.class.getClassLoader()));
        passageMark = in.readParcelable(PassageMark.class.getClassLoader());
        parentTicketInfo = in.readParcelable(ParentTicketInfo.class.getClassLoader());
        versionPD = in.readInt();
        numberPD = in.readInt();
        wayType = (TicketWayType) in.readSerializable();
        tariffCodePD = in.readLong();
        exemptionCode = in.readInt();
        saleDatetimePD = new Date(in.readLong());
        term = in.readInt();
        ecpNumberPD = in.readLong();
        deviceId = in.readLong();
        car = in.readInt();
        departureDatetime = in.readLong();
        carNumber = in.readInt();
        place = in.readInt();
        documentType = in.readInt();
        documentNumber = in.readString();
        lastname = in.readString();
        firstname = in.readString();
        middlename = in.readString();
        startCountValue = in.readInt();
        endCountValue = in.readInt();
        // in.readByteArray(ecp);
        // in.readByteArray(ecpDataForCheck);
        orderNumberPdOnCard = in.readInt();
        pdSizeByte = in.readInt();
        revocationTime = in.readLong();
        hasFareTariff = in.readInt() == 1;
        actionDays = in.readLong();
        // Старый вариант крашился
        // Caused by: java.lang.ClassNotFoundException: ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult
        int size = in.readInt();
        int[] errorCodes = new int[size];
        in.readIntArray(errorCodes);
        for (int errorCode : errorCodes) {
            errors.add(PassageResult.getPassageResultByCode(errorCode));
        }
        isRestoredTicket = in.readInt() == 1;
        serviceFeeCode = in.readLong();
        hwCounterValue = (Integer) in.readValue(Integer.class.getClassLoader());
        long lastPassageTimeLong = in.readLong();
        lastPassageTime = lastPassageTimeLong == 0 ? null : new Date(lastPassageTimeLong);
        lastPassageTurnstile7000 = in.readInt() == 1;
        lastPassageValid = in.readInt() == 1;
        lastPassage6000Valid = in.readInt() == 1;
    }

    public void checkRelevance(@NonNull StrategyCheck strategyCheck) {
        errors.addAll(strategyCheck.execCheck(this));
    }

    public void setCheckError(PassageResult errorType) {
        errors.add(errorType);
    }

    public boolean isValid() {
        return errors.size() == 0;
    }

    /**
     * Проверка на отозванность сертификата ЭЦП
     */
    public boolean isRevokedEcp() {
        return errors.contains(PassageResult.SignKeyRevoked);
    }

    /**
     * Проверка на НЕвалидность ЭЦП
     */
    public boolean isInvalidEcp() {
        return errors.contains(PassageResult.InvalidSign);
    }

    /**
     * Вернет id билета
     */
    public TicketId getTicketId() {
        return new TicketId(numberPD, deviceId, getSaleDate());
    }

    /**
     * Валидирует все возможные поля, которые могут препятствовать записи события контроля
     */
    public boolean isReadyToAddControlEvent() {
        if (deviceId <= 0) {
            Logger.error(TAG, "Ошибка добавления события контроля в БД. deviceId=" + deviceId);
            return false;
        }

        Tariff tariff = getTariff();

        if (tariff == null) {
            Logger.error(TAG, "Ошибка добавления события контроля в БД. tariff=null, tariffCodePD=" + tariffCodePD);
            return false;
        }
        if (tariff.getStationDepartureCode() <= 0) {
            Logger.error(TAG, "Ошибка добавления события контроля в БД. stationDepartureCode=" + tariff.getStationDepartureCode());
            return false;
        }
        if (tariff.getStationDestinationCode() <= 0) {
            Logger.error(TAG, "Ошибка добавления события контроля в БД. stationDestinationCode=" + tariff.getStationDestinationCode());
            return false;
        }
        return true;
    }

    @Nullable
    public Tariff getTariff() {
        return tariff;
    }

    public void setTariff(Tariff tariff) {
        this.tariff = tariff;
    }

    @Nullable
    public BscInformation getBscInformation() {
        return bscInformation;
    }

    public void setBscInformation(BscInformation bscInformation) {
        this.bscInformation = bscInformation;
    }

    //билет начинает действовать с начала суток, а не с даты продажи
    public Date getStartPdDate() {
        final Calendar instance = Calendar.getInstance();
        instance.setTime(getSaleDate());
        instance.add(Calendar.DAY_OF_MONTH, term);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTime();
    }

    public boolean isRestoredTicket() {
        return isRestoredTicket;
    }

    public void setRestoredTicket(boolean restoredTicket) {
        isRestoredTicket = restoredTicket;
    }

    public boolean isPassageToStationCheckRequired() {
        return passageToStationCheckRequired;
    }

    public void setPassageToStationCheckRequired(boolean passageToStationCheckRequired) {
        this.passageToStationCheckRequired = passageToStationCheckRequired;
    }

    public boolean isActivationRequired() {
        return activationRequired;
    }

    public void setActivationRequired(boolean activationRequired) {
        this.activationRequired = activationRequired;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getLastPassageTime() {
        return lastPassageTime;
    }

    public void setLastPassageTime(Date lastPassageTime) {
        this.lastPassageTime = lastPassageTime;
    }

    public boolean isLastPassageTurnstile7000() {
        return lastPassageTurnstile7000;
    }

    public void setLastPassageTurnstile7000(boolean lastPassageTurnstile7000) {
        this.lastPassageTurnstile7000 = lastPassageTurnstile7000;
    }

    public boolean isLastPassageValid() {
        return lastPassageValid;
    }

    public void setLastPassageValid(boolean lastPassageValid) {
        this.lastPassageValid = lastPassageValid;
    }

    public boolean isLastPassage6000Valid() {
        return lastPassage6000Valid;
    }

    public void setLastPassage6000Valid(boolean lastPassage6000Valid) {
        this.lastPassage6000Valid = lastPassage6000Valid;
    }

    @Override
    public String toString() {
        return "PD{" +
                "versionPD=" + versionPD +
                ", numberPD=" + numberPD +
                ", wayType=" + wayType +
                ", tariffCodePD=" + tariffCodePD +
                ", exemptionCode=" + exemptionCode +
                ", saleDatetimePD=" + saleDatetimePD +
                ", term=" + term +
                ", ecp=" + Arrays.toString(ecp) +
                ", ecpNumberPD=" + ecpNumberPD +
                ", lastPassageTime=" + lastPassageTime +
                ", lastPassageTurnstile7000=" + lastPassageTurnstile7000 +
                ", lastPassageValid=" + lastPassageValid +
                ", lastPassage6000Valid=" + lastPassage6000Valid +
                ", isBankPaymentType=" + isBankPaymentType +
                ", passageToStationCheckRequired=" + passageToStationCheckRequired +
                ", activationRequired=" + activationRequired +
                ", phoneNumber=" + phoneNumber +
                ", ecpDataForCheck=" + Arrays.toString(ecpDataForCheck) +
                ", deviceId=" + deviceId +
                ", car=" + car +
                ", departureDatetime=" + departureDatetime +
                ", carNumber=" + carNumber +
                ", place=" + place +
                ", documentType=" + documentType +
                ", documentNumber='" + documentNumber + '\'' +
                ", lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", middlename='" + middlename + '\'' +
                ", tariff=" + tariff +
                ", bscInformation=" + bscInformation +
                ", passageMark=" + passageMark +
                ", orderNumberPdOnCard=" + orderNumberPdOnCard +
                ", startCountValue=" + startCountValue +
                ", endCountValue=" + endCountValue +
                ", actionDays=" + actionDays +
                ", errors=" + errors +
                ", pdSizeByte=" + pdSizeByte +
                ", parentTicketInfo=" + parentTicketInfo +
                ", revocationTime=" + revocationTime +
                ", hasFareTariff=" + hasFareTariff +
                ", isRestoredTicket=" + isRestoredTicket +
                ", serviceFeeCode=" + serviceFeeCode +
                ", hwCounterValue=" + hwCounterValue +
                '}';
    }
}
