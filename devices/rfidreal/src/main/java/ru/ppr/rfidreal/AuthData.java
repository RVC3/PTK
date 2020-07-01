package ru.ppr.rfidreal;

import ru.ppr.rfid.CardData;
import ru.ppr.rfid.SamAccessRule;
import ru.ppr.rfid.StaticKeyAccessRule;

/**
 * Вспомогательный класс, хранящий информациию о текущем состоянии авторизации
 * в секторе смарт-карты Mifare Classic.
 *
 * @author Artem Ushakov
 */
public class AuthData {
    /**
     * Флаг успешной последней авторизации. Баг: если авторизация была
     * неуспешная, то чтобы следующая авторизация увенчалась успехом - нужно
     * выполнить перед ней методы
     * | mifareSamNxpKillAuthentication OK - 14 ms
     * | cscEnterHuntPhaseParameters OK - 7 ms
     * | searchCard OK - 214 ms
     */
    private boolean isAuthSuccess = false;

    /**
     * Версия последнего ключа по которопу последний раз успешно авторизовались
     */
    private byte lastAuthKeyVersion = 0;

    /**
     * Тип ключа по которому последний раз успешно авторизовались
     */
    private CardData lastCardData = new CardData();

    /**
     * Флаг загруженности ключа
     */
    private boolean isKeyLoaded = false;

    /**
     * Флаг Инициализированности SAM
     */
    private boolean isSamActive = false;

    /**
     * Номер сектора, на котором не удалось авторизоваться в последний раз
     */
    private int lastAuthSector = -1;

    /**
     * Номер Sam слота, которым была выполнена авторизация в секторе
     */
    private int samSlot;

    private SamAccessRule previousSamAccessSchemeRule;

    /**
     * Флаг Запрета на использование команды mifareSamNxpReAuthenticate
     * Нужно взводить в true, при переинициализации SAM,
     * в этом случае нужно использовать команду mifareSamNxpAuthenticate
     */
    private boolean isReAuthDisabled = false;

    private StaticKeyAccessRule previousStaticKeyAccessRule;

    public SamAccessRule getPreviousSamAccessSchemeRule() {
        return previousSamAccessSchemeRule;
    }

    public void setPreviousSamAccessSchemeRule(SamAccessRule previousSamAccessSchemeRule) {
        this.previousSamAccessSchemeRule = previousSamAccessSchemeRule;
    }

    public StaticKeyAccessRule getPreviousStaticKeyAccessRule() {
        return previousStaticKeyAccessRule;
    }

    public void setPreviousStaticKeyAccessRule(StaticKeyAccessRule previousStaticKeyAccessRule) {
        this.previousStaticKeyAccessRule = previousStaticKeyAccessRule;
    }

    public boolean isAuthSuccess() {
        return isAuthSuccess;
    }

    public void setIsAuthSuccess(boolean isAuthSuccess) {
        this.isAuthSuccess = isAuthSuccess;
    }

    public CardData getLastCardData() {
        return lastCardData;
    }

    public void setLastCardData(CardData lastCardData) {
        this.lastCardData = lastCardData;
    }

    public boolean isKeyLoaded() {
        return isKeyLoaded;
    }

    public void setIsKeyLoaded(boolean isKeyLoaded) {
        this.isKeyLoaded = isKeyLoaded;
    }

    public boolean isSamActive() {
        return isSamActive;
    }

    public void setIsSamActive(boolean isSamActive) {
        this.isSamActive = isSamActive;
    }

    public int getLastAuthSector() {
        return lastAuthSector;
    }

    public void setLastAuthSector(int lastAuthSector) {
        this.lastAuthSector = lastAuthSector;
    }

    public int getSamSlot() {
        return samSlot;
    }

    public void setSamSlot(int samSlot) {
        this.samSlot = samSlot;
    }

    public byte getLastAuthKeyVersion() {
        return lastAuthKeyVersion;
    }

    public void setLastAuthKeyVersion(byte lastAuthKeyVersion) {
        this.lastAuthKeyVersion = lastAuthKeyVersion;
    }

    public boolean isReAuthDisabled() {
        return isReAuthDisabled;
    }

    public void setIsReAuthDisabled(boolean reAuthDisabled) {
        isReAuthDisabled = reAuthDisabled;
    }
}
