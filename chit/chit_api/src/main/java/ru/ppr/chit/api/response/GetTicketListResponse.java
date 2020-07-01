package ru.ppr.chit.api.response;

import java.util.List;

import ru.ppr.chit.api.entity.TicketEntity;

/**
 * @author Dmitry Nevolin
 */
public class GetTicketListResponse extends BaseResponse {

    /**
     * Список билетов по данной нити поезда, полученный из ЦОД
     */
    private List<TicketEntity> tickets;

    public List<TicketEntity> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketEntity> tickets) {
        this.tickets = tickets;
    }

}
