package ru.ppr.chit.domain.tripservice;

/**
 * Режимы обслуживания поезда.
 *
 * @author Dmitry Nevolin
 */
public enum TripServiceMode {

    /**
     * Обслуживание начинается с информацией о поезде
     */
    ONLINE,
    /**
     * Обслуживание начинается без информации о поезде
     */
    OFFLINE

}
