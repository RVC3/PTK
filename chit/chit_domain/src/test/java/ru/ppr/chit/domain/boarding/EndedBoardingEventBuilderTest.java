package ru.ppr.chit.domain.boarding;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.util.Date;
import java.util.UUID;

import ru.ppr.chit.domain.BaseTest;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.chit.domain.model.local.Event;
import ru.ppr.chit.domain.model.local.TripServiceEvent;
import ru.ppr.chit.domain.repository.local.BoardingEventRepository;
import ru.ppr.chit.domain.repository.local.TripServiceEventRepository;
import ru.ppr.chit.domain.tripservice.TripServiceStatusChecker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Aleksandr Brazhkin
 */
public class EndedBoardingEventBuilderTest extends BaseTest {

    @Mock
    EventBuilder eventBuilder;
    @Mock
    BoardingEventRepository boardingEventRepository;
    @Mock
    TripServiceEventRepository tripServiceEventRepository;
    @Mock
    TripServiceStatusChecker tripServiceStatusChecker;
    @Mock
    BoardingStatusChecker boardingStatusChecker;
    @Mock
    TripServiceInfoStorage tripServiceInfoStorage;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void throwExceptionIfIsNotStarted() throws Exception {
        when(eventBuilder.build()).thenReturn(new Event());

        when(tripServiceStatusChecker.isStarted(null)).thenReturn(true);

        when(boardingStatusChecker.isStarted(null)).thenReturn(false);

        EndedBoardingEventBuilder endedBoardingEventBuilder = new EndedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Посадка не может быть завершена, так как она не была начата.");

        endedBoardingEventBuilder.build();
    }

    @Test
    public void throwExceptionIfWithoutControlStation() throws Exception {
        when(eventBuilder.build()).thenReturn(new Event());

        BoardingEvent prevBoardingEvent = new BoardingEvent();
        when(boardingEventRepository.loadLast()).thenReturn(prevBoardingEvent);

        when(tripServiceStatusChecker.isStarted(null)).thenReturn(true);

        when(boardingStatusChecker.isStarted(prevBoardingEvent)).thenReturn(true);

        EndedBoardingEventBuilder endedBoardingEventBuilder = new EndedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Посадка на может быть заверешна, так как не указана станция контроля.");

        endedBoardingEventBuilder.build();
    }

    @Test
    public void throwExceptionIfDifferentStation() throws Exception {
        when(eventBuilder.build()).thenReturn(new Event());

        TripServiceEvent tripServiceEvent = new TripServiceEvent();
        when(tripServiceEventRepository.loadLast()).thenReturn(tripServiceEvent);

        BoardingEvent prevBoardingEvent = new BoardingEvent();
        TripServiceEvent prevTripServiceEvent = new TripServiceEvent();
        prevTripServiceEvent.setId(8L);
        prevBoardingEvent.setTripServiceEvent(prevTripServiceEvent);
        prevBoardingEvent.setStationCode(34L);
        when(boardingEventRepository.loadLast()).thenReturn(prevBoardingEvent);

        when(tripServiceStatusChecker.isStarted(tripServiceEvent)).thenReturn(true);

        when(boardingStatusChecker.isStarted(prevBoardingEvent)).thenReturn(true);

        ControlStation controlStation = new ControlStation();
        controlStation.setCode(33L);
        when(tripServiceInfoStorage.getControlStation()).thenReturn(controlStation);

        EndedBoardingEventBuilder endedBoardingEventBuilder = new EndedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Посадка на может быть заверешна, так как была начата на другой станции.");

        endedBoardingEventBuilder.build();
    }

    @Test
    public void throwExceptionIfDifferentTripService() throws Exception {
        when(eventBuilder.build()).thenReturn(new Event());

        TripServiceEvent tripServiceEvent = new TripServiceEvent();
        tripServiceEvent.setId(7L);
        when(tripServiceEventRepository.loadLast()).thenReturn(tripServiceEvent);

        BoardingEvent prevBoardingEvent = new BoardingEvent();
        TripServiceEvent prevTripServiceEvent = new TripServiceEvent();
        prevTripServiceEvent.setId(8L);
        prevBoardingEvent.setTripServiceEvent(prevTripServiceEvent);
        prevBoardingEvent.setStationCode(34L);
        when(boardingEventRepository.loadLast()).thenReturn(prevBoardingEvent);

        when(tripServiceStatusChecker.isStarted(tripServiceEvent)).thenReturn(true);

        when(boardingStatusChecker.isStarted(prevBoardingEvent)).thenReturn(true);

        ControlStation controlStation = new ControlStation();
        controlStation.setCode(34L);
        when(tripServiceInfoStorage.getControlStation()).thenReturn(controlStation);

        EndedBoardingEventBuilder endedBoardingEventBuilder = new EndedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Boarding could not be ended in different trip service");

        endedBoardingEventBuilder.build();
    }

    @Test
    public void shouldHaveValidData() throws Exception {

        Date eventDate = new Date(1);
        Event event = new Event();
        event.setId(77L);
        event.setCreatedAt(eventDate);
        when(eventBuilder.build()).thenReturn(event);

        TripServiceEvent tripServiceEvent = new TripServiceEvent();
        tripServiceEvent.setId(5L);
        when(tripServiceEventRepository.loadLast()).thenReturn(tripServiceEvent);

        BoardingEvent prevBoardingEvent = new BoardingEvent();
        Date prevBoardingEventStartTime = new Date();
        prevBoardingEvent.setStartTime(prevBoardingEventStartTime);
        String prevBoardingEventUuid = UUID.randomUUID().toString();
        prevBoardingEvent.setBoardingUuid(prevBoardingEventUuid);
        TripServiceEvent prevTripServiceEvent = new TripServiceEvent();
        prevTripServiceEvent.setId(5L);
        prevBoardingEvent.setStationCode(34L);
        prevBoardingEvent.setTripServiceEvent(prevTripServiceEvent);
        when(boardingEventRepository.loadLast()).thenReturn(prevBoardingEvent);

        when(tripServiceStatusChecker.isStarted(tripServiceEvent)).thenReturn(true);

        when(boardingStatusChecker.isStarted(prevBoardingEvent)).thenReturn(true);

        ControlStation controlStation = new ControlStation();
        controlStation.setCode(34L);
        when(tripServiceInfoStorage.getControlStation()).thenReturn(controlStation);

        EndedBoardingEventBuilder endedBoardingEventBuilder = new EndedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        BoardingEvent boardingEvent = endedBoardingEventBuilder.build();

        assertEquals(prevBoardingEventUuid, boardingEvent.getBoardingUuid());
        assertEquals((Long) 77L, boardingEvent.getEventId());
        assertEquals((Long) 5L, boardingEvent.getTripServiceEventId());
        assertEquals(BoardingEvent.Status.ENDED, boardingEvent.getStatus());
        assertEquals(prevBoardingEventStartTime, boardingEvent.getStartTime());
        assertEquals((Long)34L, boardingEvent.getStationCode());
        assertEquals(eventDate, boardingEvent.getEndTime());
    }

}
