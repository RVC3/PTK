package ru.ppr.cppk.export.model;

/**
 * Список timestamp-ов для всех типов событий
 *
 * @author Grigoriy Kashka
 */
public class EventsDateTime {

    /**
     * Сменные события
     */
    public long shiftEvents;

    /**
     * Событие контроля
     */
    public long ticketControls;

    /**
     * Событие продажи
     */
    public long ticketSales;

    /**
     * сСобытие печати тестового ПД
     */
    public long testTickets;

    /**
     * Событие аннулирования
     */
    public long ticketReturns;

    /**
     * Месячное событие
     */
    public long monthClosures;

    /**
     * Событие установки/окончания билетной ленты
     */
    public long ticketPaperRolls;

    /**
     * Операция по банку
     */
    public long bankTransactions;

    /**
     * Событие переподписи ПД
     */
    public long ticketReSigns;

    /**
     * Событие продажи услуг
     */
    public long serviceSales;

    /**
     * Событие продажи штрафа
     */
    public long finePaidEvents;
    /**
     * События контроля/прохода по служебной карте
     */
    public long serviceTicketControls;

    @Override
    public String toString() {
        return "EventsDateTime{" +
                "shiftEvents=" + shiftEvents +
                ", ticketControls=" + ticketControls +
                ", ticketSales=" + ticketSales +
                ", testTickets=" + testTickets +
                ", ticketReturns=" + ticketReturns +
                ", monthClosures=" + monthClosures +
                ", ticketPaperRolls=" + ticketPaperRolls +
                ", bankTransactions=" + bankTransactions +
                ", ticketReSigns=" + ticketReSigns +
                ", serviceSales=" + serviceSales +
                ", finePaidEvents=" + finePaidEvents +
                ", serviceTicketControls=" + serviceTicketControls +
                '}';
    }
}
