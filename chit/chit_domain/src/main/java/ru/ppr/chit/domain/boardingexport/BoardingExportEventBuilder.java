package ru.ppr.chit.domain.boardingexport;

import javax.inject.Inject;

import ru.ppr.chit.domain.event.EventBuilder;
import ru.ppr.chit.domain.model.local.BoardingEvent;
import ru.ppr.chit.domain.model.local.BoardingExportEvent;
import ru.ppr.chit.domain.model.local.Event;

/**
 * Создаёт события выгрузки посадки
 * 
 * @author Dmitry Nevolin
 */
public class BoardingExportEventBuilder {

    private final EventBuilder eventBuilder;

    private BoardingEvent boardingEvent;

    @Inject
    BoardingExportEventBuilder(EventBuilder eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

    public BoardingExportEventBuilder setBoardingEvent(BoardingEvent boardingEvent) {
        this.boardingEvent = boardingEvent;
        return this;
    }

    public BoardingExportEvent build() {
        if (boardingEvent == null) {
            throw new IllegalStateException("boardingEvent required");
        }
        BoardingExportEvent boardingExportEvent = new BoardingExportEvent();
        // Заполняем базовое событие
        Event event = eventBuilder.build();
        boardingExportEvent.setEvent(event);
        // Заполняем собственные поля
        boardingExportEvent.setBoardingEvent(boardingEvent);
        return boardingExportEvent;
    }

}
