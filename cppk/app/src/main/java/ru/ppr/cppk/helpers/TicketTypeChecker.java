package ru.ppr.cppk.helpers;

import javax.inject.Inject;

import ru.ppr.nsi.entity.TicketType;

/**
 * Чекер, группирующий типы билета
 * http://agile.srvdev.ru/browse/CPPKPP-43912
 *
 * @author Grigoriy Kashka
 */
public class TicketTypeChecker {

    @Inject
    public TicketTypeChecker() {
    }

    /**
     * Проверяет, является ли этот тип билета трансфером, требующим отдельного билета (Домодедово)
     *
     * @param ticketTypeCode Код типа ПД
     */
    public boolean isCommonTransferTypeForSeparateSale(long ticketTypeCode) {
        return ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_ONE_WAY
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_TWO_WAY
                || ticketTypeCode == TicketType.Code.SINGLE_CHILD_TRANSFER_ONE_WAY
                || ticketTypeCode == TicketType.Code.SINGLE_CHILD_TRANSFER_TWO_WAY
                || ticketTypeCode == TicketType.Code.ONE_MONTH_TRANSFER_SEASON
                || ticketTypeCode == TicketType.Code.FIVE_DAYS_TRANSFER_SEASON
                || ticketTypeCode == TicketType.Code.WORK_DAY_TRANSFER_SEASON
                || ticketTypeCode == TicketType.Code.MIKS_TRANSFER_SEASON;
    }

    /**
     * Проверяет, является ли этот тип билета трансфером, нетребующим отдельного билета (Жуковский)
     *
     * @param ticketTypeCode Код типа ПД
     */
    public boolean isAirportTransferTypeForSeparateSale(long ticketTypeCode) {
        return ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_AIRPORT_FULL
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_AIRPORT_CHILD
                || ticketTypeCode == TicketType.Code.ONE_MONTH_TRANSFER_AIRPORT
                || ticketTypeCode == TicketType.Code.WORK_DAY_TRANSFER_AIRPORT
                || ticketTypeCode == TicketType.Code.WEEKEND_TRANSFER_AIRPORT;
    }

    /**
     * Проверяет, является ли этот тип билета детским
     *
     * @param ticketTypeCode Код типа ПД
     */
    public boolean isChild(long ticketTypeCode) {
        return ticketTypeCode == TicketType.Code.SINGLE_CHILD
                || ticketTypeCode == TicketType.Code.SINGLE_CHILD_TRANSFER_ONE_WAY
                || ticketTypeCode == TicketType.Code.SINGLE_CHILD_TRANSFER_TWO_WAY
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_AIRPORT_CHILD;
    }


    /**
     * Проверяет, является ли этот тип билета трансфером ТУДА
     *
     * @param ticketTypeCode Код типа ПД
     */
    public boolean isOneWayTransfer(long ticketTypeCode) {
        return ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_ONE_WAY
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_ONE_WAY_WITH_TICKET
                || ticketTypeCode == TicketType.Code.SINGLE_CHILD_TRANSFER_ONE_WAY
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_AIRPORT_FULL
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_AIRPORT_FULL_WITH_TICKET
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_AIRPORT_CHILD
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_AIRPORT_CHILD_WITH_TICKET;

    }

    /**
     * Проверяет, является ли этот тип билета трансфером ТУДА/ОБРАТНО
     *
     * @param ticketTypeCode Код типа ПД
     */
    public boolean isTwoWayTransfer(long ticketTypeCode) {
        return ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_TWO_WAY
                || ticketTypeCode == TicketType.Code.SINGLE_TRANSFER_TWO_WAY_WITH_TICKET
                || ticketTypeCode == TicketType.Code.SINGLE_CHILD_TRANSFER_TWO_WAY
                || ticketTypeCode == TicketType.Code.ONE_MONTH_TRANSFER_SEASON
                || ticketTypeCode == TicketType.Code.FIVE_DAYS_TRANSFER_SEASON
                || ticketTypeCode == TicketType.Code.WORK_DAY_TRANSFER_SEASON
                || ticketTypeCode == TicketType.Code.MIKS_TRANSFER_SEASON
                || ticketTypeCode == TicketType.Code.ONE_MONTH_TRANSFER_AIRPORT
                || ticketTypeCode == TicketType.Code.ONE_MONTH_TRANSFER_AIRPORT_JOINT
                || ticketTypeCode == TicketType.Code.WORK_DAY_TRANSFER_AIRPORT
                || ticketTypeCode == TicketType.Code.WORK_DAY_TRANSFER_AIRPORT_JOINT
                || ticketTypeCode == TicketType.Code.WEEKEND_TRANSFER_AIRPORT
                || ticketTypeCode == TicketType.Code.WEEKEND_TRANSFER_AIRPORT_JOINT;
    }
}
