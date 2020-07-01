package ru.ppr.security.entity;


/**
 * Настройки базы Security.
 */
public class SecuritySettings {

    /**
     * Количество неудачных попыток ввода пина подрят, в разах
     */
    private final int limitLoginAttempts;
    /**
     * Время, за которое информация о авторизационной карте должна появится в секюрити базе
     */
    private final int accessCardLoginLimitationPeriod;
    /**
     * Время блокировки доступа
     */
    private final int timeLockAccess;

    /**
     * максимальная длина ПИН кода на ПТК
     */
    private final int devicePincodeLength;

    private SecuritySettings(Builder builder) {
        limitLoginAttempts = builder.limitLoginAttempts;
        accessCardLoginLimitationPeriod = builder.accessCardLoginLimitationPeriod;
        timeLockAccess = builder.timeLockAccess;
        devicePincodeLength = builder.devicePincodeLength;
    }

    /**
     * Возвращает количество попыток неуспешной авторизаци подрят
     *
     * @return количество попыток неуспешной авторизаци
     */
    public int getLimitLoginAttempts() {
        return limitLoginAttempts;
    }

    /**
     * Возвращает время, за которое информация о авторизационной карте должна появится в секюрити базе
     *
     * @return время, за которое информация о авторизационной карте должна появится в секюрити базе,
     * в часах
     */
    public int getAccessCardLoginLimitationPeriod() {
        return accessCardLoginLimitationPeriod;
    }

    /**
     * Возвращает время блокировки доступа в минутах
     *
     * @return время блокировки доступа
     */
    public int getTimeLockAccess() {
        return timeLockAccess;
    }

    public Integer getDevicePincodeLength() {
        return this.devicePincodeLength;
    }

    public static class Builder {

        private Integer limitLoginAttempts;
        private Integer accessCodeValidityPeriod;
        private Integer accessCardLoginLimitationPeriod;
        private Integer timeLockAccess;
        private Integer devicePincodeLength;

        public void setLimitLoginAttempts(Integer limitLoginAttempts) {
            this.limitLoginAttempts = limitLoginAttempts;
        }

        public void setAccessCodeValidityPeriod(Integer accessCodeValidityPeriod) {
            this.accessCodeValidityPeriod = accessCodeValidityPeriod;
        }

        public void setTimeLockAccess(Integer timeLockAccess) {
            this.timeLockAccess = timeLockAccess;
        }

        public void setAccessCardLoginLimitationPeriod(Integer accessCardLoginLimitationPeriod) {
            this.accessCardLoginLimitationPeriod = accessCardLoginLimitationPeriod;
        }

        public void setDevicePincodeLength(Integer devicePincodeLength) {
            this.devicePincodeLength = devicePincodeLength;
        }

        public SecuritySettings create() {

            // при установке приложения на чистый птк эти поля не заданны,
            // поэтому пока заполним дефолтными значениями так
            if (limitLoginAttempts == null) {
                limitLoginAttempts = 3;
            }

            if (accessCardLoginLimitationPeriod == null) {
                accessCardLoginLimitationPeriod = 24;
            }

            if (accessCodeValidityPeriod == null) {
                accessCodeValidityPeriod = 15;
            }

            if (timeLockAccess == null) {
                timeLockAccess = 5;
            }

            if (devicePincodeLength == null) {
                devicePincodeLength = 10;
            }

            return new SecuritySettings(this);
        }


    }

    public static class Parameters {
        public static final String SecuritySettings_AccessCardLoginLimitationPeriod = "AccessCardLoginLimitationPeriod";
        /**
         * [17:48:02] ПП Антон Логинов, тестировщик: птк она не нужна
         * [17:48:18] ПП Антон Логинов, тестировщик: это для кассы код доступа, для входа без карты
         * [17:48:38] ПП Антон Логинов, тестировщик: срок действия кода, вернее
         */
        public static final String SecuritySettings_AccessCodeValidityPeriod = "AccessCodeValidityPeriod";
        public static final String SecuritySettings_LimitLoginAttempts = "LimitLoginAttempts";
        public static final String SecuritySettings_TimeLockAccess = "TimeLockAccess";
        public static final String SecuritySettings_DevicePincodeLength = "DevicePincodeLength";
    }

}
