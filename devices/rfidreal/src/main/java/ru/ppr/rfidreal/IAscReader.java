package ru.ppr.rfidreal;

import android.support.annotation.NonNull;

/**
 * Обертка над считывателем смарт-карт от Coppernic SDK.
 *
 * @author G.Kashka
 */
public interface IAscReader {
    /**
     * Подает или убирает питание на ридер
     *
     * @param on
     * @return
     */
    void setPower(boolean on);

    /**
     * Вернет версию ридера
     *
     * @param firmwareVersion
     * @return
     */
    boolean cscVersionCsc(StringBuilder firmwareVersion);

    /**
     * Закружает ключ в память ридера, для авторизации без SAM
     *
     * @param keyIndex интекс ячейси памяти 0 или 1
     * @param keyVal   значение ключа
     * @return
     */
    boolean mifareLoadReaderKeyIndex(byte keyIndex, byte[] keyVal);

    /**
     * Пишет данные в блок с использованием SAM
     *
     * @param address
     * @param data
     * @return
     */
    boolean mifareSamNxpWriteBlock(byte address, byte[] data);

    /**
     * Читает данные с ультралайта
     *
     * @param mfulType
     * @param add
     * @param length
     * @param data
     * @return
     */
    boolean mifareUlRead(byte mfulType, byte add, byte length, byte[] data);

    /**
     * Метод настройки ридера
     *
     * @param inputMask
     * @param enablePullUp
     * @param enableFilter
     * @param outputMask
     * @param outputDefaultValue
     * @param outputEnableOpenDrain
     * @param outputEnablePullUp
     * @return
     */
    int cscConfigIoExt(byte inputMask, byte enablePullUp, byte enableFilter, byte outputMask, byte outputDefaultValue, byte outputEnableOpenDrain, byte outputEnablePullUp);

    /**
     * Метод настройки ридера
     *
     * @param ioToWrite
     * @param value
     * @return
     */
    int cscWriteIoExt(byte ioToWrite, byte value);

    /**
     * Метод настрйки SAM
     *
     * @param nSam
     * @param type
     * @return
     */
    int cscSelectSam(byte nSam, byte type);

    /**
     * Сбрасывает SAM
     *
     * @param nSam
     * @param lpAtr
     * @param lpcbAtr
     * @return
     */
    int cscResetSam(byte nSam, byte[] lpAtr, int[] lpcbAtr);

    /**
     * Настройка SAM
     *
     * @param proProt
     * @param paramFd
     * @param status
     * @return
     */
    int cscSetSamBaudratePps(byte proProt, byte paramFd, byte[] status);

    /**
     * Поиск карты
     *
     * @param com
     * @param lpcbATR
     * @param lpATR
     * @return
     */
    boolean cscSearchCardExt(byte[] com, int[] lpcbATR, byte[] lpATR);

    /**
     * @param timeout
     * @param bufIn
     * @param lnIn
     * @return
     */
    int cscSendReceive(int timeout, byte[] bufIn, int lnIn);

    /**
     * @param bufIn
     * @param lnIn
     * @param status
     * @param lnOut
     * @param bufOut
     * @return
     */
    int cscTransparentCommand(byte[] bufIn, int lnIn, byte[] status, int[] lnOut, byte[] bufOut);

    /**
     * @param iso
     * @param addCRC
     * @param checkCRC
     * @param field
     * @param configISO
     * @param configAddCRC
     * @param configCheckCRC
     * @param configField
     * @return
     */
    int cscTransparentCommandConfig(byte iso, byte addCRC, byte checkCRC, byte field, byte[] configISO, byte[] configAddCRC, byte[] configCheckCRC, byte[] configField);

    /**
     * Чтение блока с SAM
     *
     * @param numBlock
     * @param dataRead
     * @return
     */
    boolean mifareSamNxpReadBlock(byte numBlock, byte[] dataRead);

    /**
     * Чтение блока без SAM
     *
     * @param numBlock
     * @param dataRead
     * @return
     */
    boolean mifareReadBlock(byte numBlock, byte[] dataRead);

    /**
     * чтение счетчика с карты UL EV1
     *
     * @param add
     * @param length
     * @param counterValue
     * @return
     */
    boolean mifareUlEv1ReadCounter(byte add, byte length, byte[] counterValue);

    /**
     * Пишет данные на ультралайт
     *
     * @param mfulType
     * @param page
     * @param dataToWrite
     * @return
     */
    boolean mifareUlWrite(byte mfulType, int page, byte[] dataToWrite);

    /**
     * Какаято настройка ридера
     *
     * @return
     */
    boolean cscEnterHuntPhaseParameters();

    /**
     * открывает ридер
     *
     * @return
     */
    boolean cscOpen();

    /**
     * закрывает ридер
     *
     * @return
     */
    void cscClose();

    /**
     * Инкрементирует счетчик на карте UL Ev1
     *
     * @param add            Индекс счетчика [1..3]
     * @param incrementValue Значение, на которое увеличаются показания
     * @return
     */
    boolean mifareUlEv1IncrementCounter(byte add, @NonNull byte[] incrementValue);

    /**
     * Вернет тип UL карты
     *
     * @param status - тип карты
     * @return
     */
    boolean mifareUlIdentifyType(byte[] status);

    /**
     * Вызывается после неудачной авторизации
     *
     * @return
     */
    boolean mifareSamNxpKillAuthentication();

    /**
     * Авторизация первая или после неуспешной авторизации
     *
     * @param numKey     - Block to authenticate
     * @param versionKey - Version key
     * @param keyAorB    - PICC key
     * @param numBlock   - адрес начиная с которого читать
     * @return
     */
    boolean mifareSamNxpAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock);

    /**
     * Авторизация после успешной авторизации
     *
     * @param numKey     - Block to authenticate
     * @param versionKey - Version key
     * @param keyAorB    - PICC key
     * @param numBlock   - адрес начиная с которого читать
     * @return
     */
    boolean mifareSamNxpReAuthenticate(byte numKey, byte versionKey, byte keyAorB, byte numBlock);

    /**
     * Авторизация без использования SAM
     *
     * @param numSector Sector to authenticate
     * @param keyAorB   Choice of the key needed for authentication
     * @param keyIndex  Index from 0 to 31 of the Reader key used for authentication
     * @return
     */
    boolean mifareAuthenticate(byte numSector, byte keyAorB, byte keyIndex);


}
