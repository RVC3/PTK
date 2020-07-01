package ru.ppr.rfid;

/**
 * Реализация алгоритма авторизации в секторах карт Mifare Classic c использованием конкретных ключей.
 * Нужна для совместимости со сторыми методами чтения карт, куда передается конкретный ключ.
 *
 * @author Aleksandr Brazhkin
 */
public class ForLegacyStaticKeyAuthorizationStrategy implements StaticKeyAuthorizationStrategy {

    private int index = 0;
    private boolean lastAuthSuccess = false;
    private StaticKeyAccessRule currentStaticKeyAccessRule;

    public ForLegacyStaticKeyAuthorizationStrategy(byte[] key) {
        StaticKeyAccessRule staticKeyAccessRule = new StaticKeyAccessRule();
        staticKeyAccessRule.setKey(key);
        staticKeyAccessRule.setKeyName(0x0A);
        currentStaticKeyAccessRule = staticKeyAccessRule;
    }

    @Override
    public StaticKeyAccessRule getKey(int sectorNum, boolean forRead) {

        if (lastAuthSuccess) {
            return currentStaticKeyAccessRule;
        }

        if (index < 1) {
            return currentStaticKeyAccessRule;
        }

        return null;
    }

    @Override
    public void setLastStaticKeyStatus(boolean success) {
        lastAuthSuccess = success;
        if (!success) {
            index++;
        }
    }
}
