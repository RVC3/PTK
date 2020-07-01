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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * @author Aleksandr Brazhkin
 */
public class StartedBoardingEventBuilderTest extends BaseTest {

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
    public void throwExceptionIfPreviousEventIsStarted() throws Exception {
        when(eventBuilder.build()).thenReturn(new Event());

        BoardingEvent prevBoardingEvent = new BoardingEvent();
        when(boardingEventRepository.loadLast()).thenReturn(prevBoardingEvent);

        when(boardingStatusChecker.isStarted(prevBoardingEvent)).thenReturn(true);

        StartedBoardingEventBuilder startedBoardingEventBuilder = new StartedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Посадка не может быть начата повторно.");

        startedBoardingEventBuilder.build();
    }

    @Test
    public void throwExceptionIfWithoutControlStation() throws Exception {
        when(eventBuilder.build()).thenReturn(new Event());

        BoardingEvent prevBoardingEvent = new BoardingEvent();
        when(boardingEventRepository.loadLast()).thenReturn(prevBoardingEvent);

        when(tripServiceStatusChecker.isStarted(null)).thenReturn(false);

        when(boardingStatusChecker.isStarted(prevBoardingEvent)).thenReturn(false);

        StartedBoardingEventBuilder startedBoardingEventBuilder = new StartedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Посадка не может быть начата, не задана станция контроля.");

        startedBoardingEventBuilder.build();
    }

    @Test
    public void throwExceptionIfTripServiceIsNotStarted() throws Exception {
        when(eventBuilder.build()).thenReturn(new Event());

        BoardingEvent prevBoardingEvent = new BoardingEvent();
        when(boardingEventRepository.loadLast()).thenReturn(prevBoardingEvent);

        when(tripServiceStatusChecker.isStarted(null)).thenReturn(false);

        when(boardingStatusChecker.isStarted(prevBoardingEvent)).thenReturn(false);

        when(tripServiceInfoStorage.getControlStation()).thenReturn(new ControlStation());

        StartedBoardingEventBuilder startedBoardingEventBuilder = new StartedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Посадка не может быть начата без начала поездки.");

        startedBoardingEventBuilder.build();
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
        when(boardingEventRepository.loadLast()).thenReturn(prevBoardingEvent);

        when(tripServiceStatusChecker.isStarted(tripServiceEvent)).thenReturn(true);

        when(boardingStatusChecker.isStarted(prevBoardingEvent)).thenReturn(false);

        ControlStation controlStation = new ControlStation();
        controlStation.setCode(450L);
        when(tripServiceInfoStorage.getControlStation()).thenReturn(controlStation);

        StartedBoardingEventBuilder startedBoardingEventBuilder = new StartedBoardingEventBuilder(
                eventBuilder,
                boardingEventRepository,
                tripServiceEventRepository,
                tripServiceStatusChecker,
                boardingStatusChecker,
                tripServiceInfoStorage);

        BoardingEvent boardingEvent = startedBoardingEventBuilder.build();

        assertNotEquals(prevBoardingEventUuid, boardingEvent.getBoardingUuid());
        assertEquals((Long) 77L, boardingEvent.getEventId());
        assertEquals((Long) 5L, boardingEvent.getTripServiceEventId());
        assertEquals(BoardingEvent.Status.STARTED, boardingEvent.getStatus());
        assertEquals(eventDate, boardingEvent.getStartTime());
        assertEquals((Long)450L, boardingEvent.getStationCode());
        assertNull(boardingEvent.getEndTime());
    }

}
