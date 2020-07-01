package ru.ppr.core.dataCarrier.smartCard.serviceData.base;

/**
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataWithFlags extends ServiceData {

    /**
     * Возращает тип служебной карты.
     *
     * @return Тип служебной карты
     */
    CardType getCardType();

    /**
     * Возращает флаг "Персонифицированная".
     *
     * @return Флаг "Персонифицированная"
     */
    PersonalizedFlag getPersonalizedFlag();

    /**
     * Возращает флаг "Должность".
     *
     * @return Флаг "Должность"
     */
    PostExistingFlag getPostExistingFlag();

    /**
     * Возращает флаг "Обязательность проверки документов"
     *
     * @return Флаг "Обязательность проверки документов"
     */
    MandatoryOfDocVerification getMandatoryOfDocVerification();

    /**
     * Тип служебной карты.
     */
    enum CardType {
        /**
         * 0 - возможность открытия турникета
         */
        TURNSTILE(0),
        /**
         * 1 - возможность проезда
         */
        TRIP(1);

        private final int code;

        CardType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static CardType getByCode(int code) {
            for (CardType cardType : CardType.values()) {
                if (cardType.getCode() == code) {
                    return cardType;
                }
            }
            return null;
        }
    }

    /**
     * Флаг "Персонифицированная".
     */
    enum PersonalizedFlag {
        /**
         * 0 - не персонифицированная
         */
        NOT_PERSONALIZED(0),
        /**
         * 1 - персонифицированная
         * Само ФИО есть только в ЦОД, и может быть напечатано на карте.
         */
        PERSONALIZED(1);

        private final int code;

        PersonalizedFlag(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PersonalizedFlag getByCode(int code) {
            for (PersonalizedFlag personalizedFlag : PersonalizedFlag.values()) {
                if (personalizedFlag.getCode() == code) {
                    return personalizedFlag;
                }
            }
            return null;
        }
    }

    /**
     * Флаг "Должность".
     */
    enum PostExistingFlag {
        /**
         * 0 - без указания должности
         */
        NOT_EXISTS(0),
        /**
         * 1 - с указанием должности
         * Идентификатор должности может быть записан на карту.
         */
        EXISTS(1);

        private final int code;

        PostExistingFlag(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PostExistingFlag getByCode(int code) {
            for (PostExistingFlag postExistingFlag : PostExistingFlag.values()) {
                if (postExistingFlag.getCode() == code) {
                    return postExistingFlag;
                }
            }
            return null;
        }
    }

    /**
     * Флаг "Обязательность проверки документов".
     */
    enum MandatoryOfDocVerification {
        /**
         * 0 - не требуется проверка документов, если карта предъявлена на ПТК
         */
        NOT_REQUIRED(0),
        /**
         * 1 - обязательная проверка документов
         */
        REQUIRED(1);

        private final int code;

        MandatoryOfDocVerification(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static MandatoryOfDocVerification getByCode(int code) {
            for (MandatoryOfDocVerification mandatoryOfDocVerification : MandatoryOfDocVerification.values()) {
                if (mandatoryOfDocVerification.getCode() == code) {
                    return mandatoryOfDocVerification;
                }
            }
            return null;
        }
    }
}
