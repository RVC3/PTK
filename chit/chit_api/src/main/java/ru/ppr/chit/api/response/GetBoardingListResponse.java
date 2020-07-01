package ru.ppr.chit.api.response;

import java.util.List;

import ru.ppr.chit.api.entity.TicketBoardingEntity;

/**
 * @author Dmitry Nevolin
 */
public class GetBoardingListResponse extends BaseResponse {

    private List<TicketBoardingEntity> ticketBoardingList;

    public List<TicketBoardingEntity> getTicketBoardingList() {
        return ticketBoardingList;
    }

    public void setTicketBoardingList(List<TicketBoardingEntity> ticketBoardingList) {
        this.ticketBoardingList = ticketBoardingList;
    }

}
