package ru.ppr.cppk.helpers;

import javax.inject.Inject;

import ru.ppr.nsi.entity.TicketCategory;

/**
 * Чекер, группирующий категории билета.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketCategoryChecker {

    @Inject
    public TicketCategoryChecker() {
    }

    /**
     * Проверяет, является ли эта категория билета комбинированным абонементом на количество поездок.
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isCombinedCountTripsSeasonTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.COMBINED_COUNT_TRIPS_SEASON_TICKET; //Комбинированный абонемент 6000 + 7000
    }

    /**
     * Проверяет, является ли эта категория билета абонементом на поезд
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isTrainSeasonTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_BY_PERIOD //Абонемент на период
                || ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_ON_WEEKEND //Абонемент выходного дня
                || ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_ON_WORKDAYS //Абонемент рабочего дня
                || ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_ON_CERTAIN_DAYS //Абонемент на определенные дни
                || ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_BY_TRIPS_COUNT //Абонемент на количество поездок
                || ticketCategoryCode == TicketCategory.Code.SPECIAL_OFFER; // Специальное предложение
    }

    /**
     * Проверяет, является ли эта категория билета разовым ПД на поезд
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isTrainOneOffTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.SINGLE;
    }

    /**
     * Проверяет, является ли эта категория билета багажом
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isTrainBaggageTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.BAGGAGE;
    }

    /**
     * Проверяет, является ли эта категория билета разовым разовым ПД на поезд
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isTrainSingleTicket(long ticketCategoryCode) {
        return isTrainOneOffTicket(ticketCategoryCode) || isTrainBaggageTicket(ticketCategoryCode);
    }

    /**
     * Проверяет, является ли эта категория билета разовым трансвером
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isTransferSingleTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.SINGLE_TRANSFER;
    }

    /**
     * Проверяет, является ли эта категория билета абонементом трансфера
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isTransferSeasonTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.TRANSFER_SEASON_TICKET_BY_PERIOD // Трансфер абонемент «Ежедневно»
                || ticketCategoryCode == TicketCategory.Code.TRANSFER_SEASON_TICKET_ON_WEEKEND // Трансфер абонемент «Выходного дня»
                || ticketCategoryCode == TicketCategory.Code.TRANSFER_SEASON_TICKET_ON_WORKDAYS // Трансфер абонемент «Рабочего дня»
                || ticketCategoryCode == TicketCategory.Code.TRANSFER_SEASON_TICKET_BY_TRIPS_COUNT // Трансфер абонемент «На количество поездок»
                || ticketCategoryCode == TicketCategory.Code.TRANSFER_SPECIAL_OFFER; // Трансфер Специальные предложения
    }

    /**
     * Проверяет, является ли эта категория билета разовым
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isSingleTicket(long ticketCategoryCode) {
        return isTrainSingleTicket(ticketCategoryCode) || isTransferSingleTicket(ticketCategoryCode);
    }

    /**
     * Проверяет, является ли эта категория билета абонементом
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isSeasonTicket(long ticketCategoryCode) {
        return isTrainSeasonTicket(ticketCategoryCode) || isTransferSeasonTicket(ticketCategoryCode);
    }

    /**
     * Проверяет, является ли эта категория билета трансвером
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isTransferTicket(long ticketCategoryCode) {
        return isTransferSeasonTicket(ticketCategoryCode) || isTransferSingleTicket(ticketCategoryCode);
    }

    /**
     * Проверяет, является ли эта категория билета абонементом на дни
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isSeasonForDaysTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_ON_CERTAIN_DAYS;
    }

    /**
     * Проверяет, является ли эта категория билета абонементом на количество поездок
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isCountTripsSeasonTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_BY_TRIPS_COUNT
                || ticketCategoryCode == TicketCategory.Code.COMBINED_COUNT_TRIPS_SEASON_TICKET
                || ticketCategoryCode == TicketCategory.Code.TRANSFER_SEASON_TICKET_BY_TRIPS_COUNT;
    }

    /**
     * Проверяет, является ли эта категория билета абонементом на выходные дни
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isWeekendSeasonTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_ON_WEEKEND
                || ticketCategoryCode == TicketCategory.Code.TRANSFER_SEASON_TICKET_ON_WEEKEND;
    }

    /**
     * Проверяет, является ли эта категория билета абонементом на рабочие дни
     *
     * @param ticketCategoryCode Код категории ПД
     */
    public boolean isWorkDaysSeasonTicket(long ticketCategoryCode) {
        return ticketCategoryCode == TicketCategory.Code.SEASON_TICKET_ON_WORKDAYS
                || ticketCategoryCode == TicketCategory.Code.TRANSFER_SEASON_TICKET_ON_WORKDAYS;
    }
}
