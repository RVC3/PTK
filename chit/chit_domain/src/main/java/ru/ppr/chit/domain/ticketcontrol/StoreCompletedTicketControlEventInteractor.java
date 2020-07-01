package ru.ppr.chit.domain.ticketcontrol;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.TicketBoarding;
import ru.ppr.chit.domain.model.local.TicketControlEvent;
import ru.ppr.chit.domain.repository.local.TicketBoardingRepository;
import ru.ppr.chit.domain.repository.local.TicketControlEventRepository;
import ru.ppr.chit.domain.repository.local.base.LocalDbTransaction;

/**
 * @author Aleksandr Brazhkin
 */
public class StoreCompletedTicketControlEventInteractor {

    private final LocalDbTransaction localDbTransaction;
    private final TicketBoardingRepository ticketBoardingRepository;
    private final TicketControlEventRepository ticketControlEventRepository;

    @Inject
    StoreCompletedTicketControlEventInteractor(LocalDbTransaction localDbTransaction,
                                                      TicketBoardingRepository ticketBoardingRepository,
                                                      TicketControlEventRepository ticketControlEventRepository) {
        this.localDbTransaction = localDbTransaction;
        this.ticketBoardingRepository = ticketBoardingRepository;
        this.ticketControlEventRepository = ticketControlEventRepository;
    }


    public void store(TicketControlEvent ticketControlEvent) {
        try {
            localDbTransaction.begin();

            // Обновляем в БД информацию о посадке пассажира
            TicketBoarding ticketBoarding = ticketControlEvent.getTicketBoarding(ticketBoardingRepository);
            ticketBoardingRepository.update(ticketBoarding);

            // Обновляем в БД событие контроля
            ticketControlEventRepository.update(ticketControlEvent);

            localDbTransaction.commit();
        } finally {
            localDbTransaction.end();
        }
    }
}
