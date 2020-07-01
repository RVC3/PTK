package ru.ppr.chit.domain.ticketcontrol;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.repository.local.TicketBoardingRepository;

/**
 * Билдер события {@link TicketControlEvent} в статусе {@link TicketControlEvent.Status#COMPLETED}.
 *
 * @author Aleksandr Brazhkin
 */
public class CompletedTicketControlEventBuilder {

    private final TicketBoardingRepository ticketBoardingRepository;
    //////////////////////////////////////////////////////////////////
    private TicketControlEvent ticketControlEvent;
    /**
     * Признак «Посадка пассажира»
     */
    private boolean wasBoarded;

    @Inject
    CompletedTicketControlEventBuilder(TicketBoardingRepository ticketBoardingRepository) {
        this.ticketBoardingRepository = ticketBoardingRepository;
    }

    public CompletedTicketControlEventBuilder setTicketControlEvent(TicketControlEvent ticketControlEvent) {
        this.ticketControlEvent = ticketControlEvent;
        return this;
    }

    public CompletedTicketControlEventBuilder setWasBoarded(boolean wasBoarded) {
        this.wasBoarded = wasBoarded;
        return this;
    }

    public TicketControlEvent build() {
        // Обновляем посадку по билету
        TicketBoarding ticketBoarding = ticketControlEvent.getTicketBoarding(ticketBoardingRepository);
        if (ticketBoarding != null) {
            ticketBoarding.setWasBoarded(wasBoarded);
        }
        // Обновляем статус события
        ticketControlEvent.setStatus(TicketControlEvent.Status.COMPLETED);

        return ticketControlEvent;
    }
}
