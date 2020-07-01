package ru.ppr.rfid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Считыватель смарт-карт.
 *
 * @author Artem Ushakov
 */
public interface IRfid {
    boolean open();

    void close();

    boolean isOpened();

    /**
     * Читает данные с карты типа классик c помощью алгоритма авторизации c использованием SAM-модуля.
     *
     * @param startSectorNumber        Номер стартового сектора для чтения
     * @param startBlockNumber         Номер стартового блока для чтения
     * @param blockCount               Количество считываемых блоков
     * @param samAuthorizationStrategy Алгоритм авторизации
     * @return Результат чтения
     */
    RfidResult<byte[]> readFromClassic(int startSectorNumber, int startBlockNumber, int blockCount, SamAuthorizationStrategy samAuthorizationStrategy);

    /**
     * Читает данные с карты типа классик c помощью алгоритма авторизации c использованием конкретных ключей.
     *
     * @param startSectorNumber              Номер стартового сектора для чтения
     * @param startBlockNumber               Номер стартового блока для чтения
     * @param blockCount                     Количество считываемых блоков
     * @param staticKeyAuthorizationStrategy Алгоритм авторизации
     * @return Результат чтения
     */
    RfidResult<byte[]> readFromClassic(int startSectorNumber, int startBlockNumber, int blockCount, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy);

    /**
     * Производит запись на карту типа классик c помощью алгоритма авторизации c использованием SAM-модуля.
     *
     * @param startSectorNumber        Номер стартового сектора для записи
     * @param startBlockNumber         Номер стартового блока для записи
     * @param data                     Данные для записи
     * @param cardUid                  Uid карты, на которую гарантированно необходимо произвести
     *                                 запись, если передали null, то записываем на любую приложенную карту
     * @param samAuthorizationStrategy Алгоритм авторизации
     * @return Результат записи
     */
    WriteToCardResult writeToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid, SamAuthorizationStrategy samAuthorizationStrategy);

    /**
     * Производит запись на карту типа классик c помощью алгоритма авторизации c использованием конкретных ключей.
     *
     * @param startSectorNumber              Номер стартового сектора для записи
     * @param startBlockNumber               Номер стартового блока для записи
     * @param data                           Данные для записи
     * @param cardUid                        Uid карты, на которую гарантированно необходимо произвести
     *                                       запись, если передали null, то записываем на любую приложенную карту
     * @param staticKeyAuthorizationStrategy Алгоритм авторизации
     * @return Результат записи
     */
    WriteToCardResult writeToClassic(int startSectorNumber, int startBlockNumber, byte[] data, byte[] cardUid, StaticKeyAuthorizationStrategy staticKeyAuthorizationStrategy);

    /**
     * Читает данные с карты типа классик стандартным ключом
     *
     * @param sectorNumber             номер сектора для чтения
     * @param blockNumber              номер блока для чтения
     * @param samAuthorizationStrategy алгоритм авторизации
     * @param isUseSam                 флаг использования сам модуля
     * @return
     */
    @Deprecated
    RfidResult<byte[]> readFromClassic(byte sectorNumber, byte blockNumber, SamAuthorizationStrategy samAuthorizationStrategy, boolean isUseSam);


    /**
     * Читает данные с карты типа классик с переданным ключом
     *
     * @param sectorNumber             номер сектора для чтения
     * @param blockNumber              номер блока для чтения
     * @param key                      ключ, с помощью которого необходимо авторизоваться на карту
     * @param samAuthorizationStrategy тип считываемой карты, например CPPK, может быть null
     * @param isUseSam                 флаг использования сам модуля
     * @return
     */
    @Deprecated
    RfidResult<byte[]> readFromClassic(byte sectorNumber, byte blockNumber, byte[] key, SamAuthorizationStrategy samAuthorizationStrategy, boolean isUseSam);

    /**
     * Производит запись на карту типа классик
     *
     * @param data                     данные для записи
     * @param cardUID                  ид карты на которую гарантированно необходимо произвести
     *                                 запись, если передали null, то записываем на любую приложенную
     *                                 карту
     * @param sector                   сектор для записи
     * @param block                    стартовый блок для записи
     * @param samAuthorizationStrategy алгоритм авторизации
     * @return результат записи
     */
    @Deprecated
    WriteToCardResult writeToClassic(byte[] data, byte[] cardUID, int sector, int block, SamAuthorizationStrategy samAuthorizationStrategy, boolean useSamNxp);

    /**
     * Очищает авторизационные данные
     */
    void clearAuthData();

    /**
     * Читает данные с карты типа ультралайт
     *
     * @param pageNumber номер страницы для чтения
     * @param startByte  номер байта в странице с которого необходимо начать чтение
     * @param length     количество байт, которые необходимо считать
     * @return
     */
    RfidResult<byte[]> readFromUltralight(byte pageNumber, byte startByte, byte length);

    /**
     * Записывает данные на карту ультралайт
     *
     * @param data    данные для записи
     * @param cardUID ид карты на которую гарантированно необходимо произвести
     *                запись, если передали null, то записываем на любую приложенную
     *                карту
     * @param addres  номер страницы, с которой нужно начать запись
     * @return
     */
    WriteToCardResult writeToUltralight(byte[] data, byte[] cardUID, int addres);

    /**
     * Читает значение счетчика с карты типа UltralightEV1.
     *
     * @param counterIndex Индекс счетчика [1..3]
     * @return Результат чтения карты
     */
    @NonNull
    RfidResult<byte[]> readCounterFromUltralightEV1(int counterIndex);

    /**
     * Увеличивает значение счетчика на карте типа UltralightEV1.
     *
     * @param counterIndex   Индекс счетчика [1..3]
     * @param incrementValue Значение, на которое увеличаются показания
     * @param cardUid        Uid карты
     * @return Результат записи на карту
     */
    @NonNull
    WriteToCardResult incrementCounterUltralightEV1(int counterIndex, int incrementValue, byte[] cardUid);

    /**
     * Стирает данные с карты
     *
     * @param cardUID                  ид карты на которую гарантированно необходимо произвести
     *                                 запись, если передали null, то записываем на любую приложенную
     *                                 карту
     * @param startSector              номер сектора
     * @param startBlock               номер блока в секторе
     * @param countBlockForDelete      количество блоков для очистки
     * @param samAuthorizationStrategy алгоритм авторизации
     * @return
     */
    WriteToCardResult deleteDataFromClassic(byte[] cardUID, byte startSector, byte startBlock,
                                            int countBlockForDelete, SamAuthorizationStrategy samAuthorizationStrategy, boolean useSamNxp);

    /**
     * Получает версию ридера
     *
     * @param version
     * @return
     */
    boolean getFWVersion(String[] version);

    /**
     * Возвращает RfidAttr карты если она была найдена
     *
     * @return
     */
    @Nullable
    CardData getRfidAtr();

    /**
     * Вернет модель устройства
     *
     * @param model
     * @return
     */
    boolean getModel(String[] model);
}
