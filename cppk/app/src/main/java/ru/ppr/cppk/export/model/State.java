package ru.ppr.cppk.export.model;

/**
 * Модель сущности состояния ПТК (файл State.bin)
 *
 * @author Grigoriy Kashka
 */
public class State {

    public static final String PtkType = "PTK";

    /**
     * тип устройства
     */
    public String terminalType;

    /**
     * идентификатор устройства
     */
    public String id;

    /**
     * Версия ПО
     */
    public String softwareVersion;

    /**
     * Версия дата-контрактов
     */
    public int dataContracts;

    /**
     * Версия НСИ
     */
    public int rdsVersion;

    /**
     * Дата последнего изменения базы безопасности. Используется как версия
     */
    public String securityVersion;

    /**
     * Версия открытых ключей СФТ
     */
    public String sftInKeysVersion;

    /**
     * Дата последнего именющегося на ПТК стоп-листа
     */
    public String ticketStopListItem;

    /**
     * Дата создания последнего именющегося на ПТК билета из разрешенных списков
     */
    public String ticketWhitelistItem;

    /**
     * Дата последнего именющегося на ПТК стоп-листа для смарт-карт
     */
    public String smartCardStopListItem;

    /**
     * Состояние SFT на ПТК
     */
    public int ptkSftState;

    /**
     * Время последнего события на ПТК. Это уже не стребуется, но из-за проблемм с поддержкой датаконтрактов на кассе приходится поддерживать на ПТК
     */
    @Deprecated
    public long latestEventTimestamp;

    /**
     * Timestamp-ы последних событий
     */
    public EventsDateTime lastTimestampsForEvent;

    /**
     * Timestamp-ы последних принятых цодом событий
     */
    public EventsDateTime lastTimestampsForSentEvent;

}
