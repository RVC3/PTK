package ru.ppr.core.dataCarrier.smartCard.serviceData.base;

import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataVersion;

/**
 * Базоый класс для служебных данных v.120, v.121.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseServiceDataV120V121 extends BaseServiceData implements
        ServiceDataWithOrderNumber,
        ServiceDataWithValidityTime,
        ServiceDataWithFlags {

    /**
     * Порядковый номер
     */
    private int orderNumber;
    /**
     * Срок действия
     */
    private int validityTime;
    /**
     * Тип служебной карты
     */
    private CardType cardType;
    /**
     * Флаг "Персонифицированная"
     */
    private PersonalizedFlag personalizedFlag;
    /**
     * Флаг "Должность"
     */
    private PostExistingFlag postExistingFlag;
    /**
     * Флаг "Обязательность проверки документов"
     */
    private MandatoryOfDocVerification mandatoryOfDocVerification;

    public BaseServiceDataV120V121(ServiceDataVersion version, int size) {
        super(version, size);
    }

    @Override
    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public int getValidityTime() {
        return validityTime;
    }

    public void setValidityTime(int validityTime) {
        this.validityTime = validityTime;
    }

    @Override
    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    @Override
    public PersonalizedFlag getPersonalizedFlag() {
        return personalizedFlag;
    }

    public void setPersonalizedFlag(PersonalizedFlag personalizedFlag) {
        this.personalizedFlag = personalizedFlag;
    }

    @Override
    public PostExistingFlag getPostExistingFlag() {
        return postExistingFlag;
    }

    public void setPostExistingFlag(PostExistingFlag postExistingFlag) {
        this.postExistingFlag = postExistingFlag;
    }

    @Override
    public MandatoryOfDocVerification getMandatoryOfDocVerification() {
        return mandatoryOfDocVerification;
    }

    public void setMandatoryOfDocVerification(MandatoryOfDocVerification mandatoryOfDocVerification) {
        this.mandatoryOfDocVerification = mandatoryOfDocVerification;
    }
}
