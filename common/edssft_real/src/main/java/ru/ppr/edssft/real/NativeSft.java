package ru.ppr.edssft.real;

/**
 * Обертка над нативной частью SFT.
 *
 * @author Aleksandr Brazhkin
 */
final class NativeSft {

    static final int DEFAULT_SFT_STATE = -777;

    static {
        System.loadLibrary("cryptoc");
        System.loadLibrary("safetickets-sdk4");
        System.loadLibrary("SftEdsChecker");
    }

    /**
     * Номер ключа от последней операции nativeSignData
     */
    private long keyNumber = 0;
    /**
     * Подпись данных от последней операции nativeSignData
     */
    private byte[] signData = new byte[64];
    /**
     * Дата начала действия ключа ЭЦП от последней операции nativeGetKeyInfo
     */
    private long keyValidSince = 0;
    /**
     * Дата окончания действия ключа ЭЦП от последней операции nativeGetKeyInfo
     */
    private long keyValidTill = 0;
    /**
     * Дата отзыва ключа ЭЦП от последней операции nativeGetKeyInfo
     */
    private long keyWhenRevocated = 0;
    /**
     * Id устройства создавшего подпись от последней операции nativeGetKeyInfo
     */
    private byte[] deviceId = null;
    /**
     * Результат проверки подписи от последней операции nativeVerifySign
     */
    private boolean isSignValid = false;
    /**
     * Статус SFT от последней операции nativeGetState
     */
    private int state = DEFAULT_SFT_STATE;

    NativeSft() {

    }

    byte[] getSignData() {
        return signData;
    }

    long getKeyNumber() {
        return keyNumber;
    }

    void setKeyNumber(long keyNumber) {
        this.keyNumber = keyNumber;
    }

    void setSignData(byte[] signData) {
        this.signData = signData;
    }

    long getKeyValidSince() {
        return keyValidSince;
    }

    void setKeyValidSince(long keyValidSince) {
        this.keyValidSince = keyValidSince;
    }

    long getKeyValidTill() {
        return keyValidTill;
    }

    void setKeyValidTill(long keyValidTill) {
        this.keyValidTill = keyValidTill;
    }

    long getKeyWhenRevocated() {
        return keyWhenRevocated;
    }

    void setKeyWhenRevocated(long keyWhenRevocated) {
        this.keyWhenRevocated = keyWhenRevocated;
    }

    byte[] getDeviceId() {
        return deviceId;
    }

    void setDeviceId(byte[] deviceId) {
        this.deviceId = deviceId;
    }

    boolean isSignValid() {
        return isSignValid;
    }

    void setSignValid(boolean signValid) {
        isSignValid = signValid;
    }

    int getState() {
        return state;
    }

    void setState(int state) {
        this.state = state;
    }

    /**
     * Инициализирует буфер для подписи.
     * Вызывается из нативного кода.
     *
     * @param size Размер буфера
     */
    private void setSignDataSize(int size) {
        this.signData = new byte[size];
    }

    /**
     * Инициализирует буфер для идентификатор устройства.
     * Вызывается из нативного кода.
     *
     * @param size Размер буфера
     */
    private void setDeviceIdBuffer(int size) {
        this.deviceId = new byte[size];
    }

    /**
     * Setup userId for sdk Прямая нативная функция использовать только для
     * тестов
     *
     * @param userId
     * @return
     */
    native int nativeSetUserId(int userId);

    /**
     * Read additional info by key
     *
     * @param keyNumber
     * @return
     */
    native int nativeGetKeyInfo(int keyNumber);

    /**
     * Init sdk Прямая нативная функция использовать только для тестов
     *
     * @param pathToWorking   path to working directory
     * @param pathToTransport path to transport directory
     * @return
     */
    native int nativeOpenProcessor(String pathToWorking, String pathToTransport);

    /**
     * Close sdk Прямая нативная функция использовать только для тестов
     *
     * @return
     */
    native int nativeCloseProcessor();

    /**
     * Sign data. After this operations ecp stored in the signData, ecp key int
     * the keyNumber Прямая нативная функция использовать только для тестов
     *
     * @param data current data
     * @param time current time in the POSIX time
     * @return result code
     */
    native int nativeSignData(byte[] data, long time);

    /**
     * Check ecp for data, verify result are stored in the isSignValid Прямая
     * нативная функция использовать только для тестов
     *
     * @param data      data for verify
     * @param sign      ecp
     * @param keyNumber ecp key
     * @return result code
     */
    native int nativeVerifySign(byte[] data, byte[] sign, long keyNumber);

    /**
     * Return string representation for last error Прямая нативная функция
     * использовать только для тестов
     *
     * @return
     */
    native String nativeGetLastError();

    /**
     * Return current sft state Прямая нативная функция использовать только для
     * тестов
     *
     * @return
     */
    native int nativeGetState();
}
