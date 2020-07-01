package ru.ppr.chit.api.request;

import java.util.List;

import ru.ppr.chit.api.entity.TicketBoardingEntity;

/**
 * @author Dmitry Nevolin
 */
public class PushBoardingListRequest extends BaseRequest {

    /**
     * Коллекция событий посадки
     */
    private List<TicketBoardingEntity> ticketBoardingList;

    public List<TicketBoardingEntity> getTicketBoardingList() {
        return ticketBoardingList;
    }

    public void setTicketBoardingList(List<TicketBoardingEntity> ticketBoardingList) {
        this.ticketBoardingList = ticketBoardingList;
    }

}
