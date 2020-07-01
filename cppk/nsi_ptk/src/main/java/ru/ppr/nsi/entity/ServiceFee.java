package ru.ppr.nsi.entity;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Сервисный сбор.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceFee extends BaseNSIObject<Long> {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SERVICE_FEE_CODE_EKLZ_CHANGE,
            BICYCLE_OPTION_EZH1_SO,
            BICYCLE_OPTION_VD1_SO,
            BICYCLE_OPTION_BM1_SO,
            UNKNOWN})
    public @interface ServiceFeeCode {
    }

    public static final int UNKNOWN = -1;
    public static final int SERVICE_FEE_CODE_EKLZ_CHANGE = 3;
    public static final int BICYCLE_OPTION_EZH1_SO = 12;
    public static final int BICYCLE_OPTION_VD1_SO = 13;
    public static final int BICYCLE_OPTION_BM1_SO = 14;

    /**
     * Наименование
     */
    private String name;
    /**
     * Количество дней, на протяжении которых действует услуга.
     * Если null значит срок действия неограничен (http://agile.srvdev.ru/browse/CPPKPP-36175)
     */
    private Integer validityPeriod;

    public ServiceFee() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public Integer getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    @ServiceFeeCode
    public int getServiceCode() {
        @ServiceFeeCode int serviceCode;

        switch (getCode().intValue()) {
            case SERVICE_FEE_CODE_EKLZ_CHANGE:
                serviceCode = SERVICE_FEE_CODE_EKLZ_CHANGE;

                break;
            case BICYCLE_OPTION_EZH1_SO:
                serviceCode = SERVICE_FEE_CODE_EKLZ_CHANGE;

                break;
            case BICYCLE_OPTION_VD1_SO:
                serviceCode = SERVICE_FEE_CODE_EKLZ_CHANGE;

                break;
            case BICYCLE_OPTION_BM1_SO:
                serviceCode = SERVICE_FEE_CODE_EKLZ_CHANGE;

                break;
            default:
                serviceCode = UNKNOWN;
        }

        return serviceCode;
    }

}
