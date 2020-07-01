package ru.ppr.cppk.export.model.request;

import ru.ppr.cppk.export.model.EventsDateTime;

/**
 * Структура запроса, который касса присылает на ПТК когда хочет получить все события
 *
 * @author Grigoriy Kashka
 */
public class GetEventsReq {
    //время последнего события которое уже есть на кассе
    public EventsDateTime lastTimestampsForEvent = new EventsDateTime();

    //время последнего удачно принятого ЦОДом события
    public EventsDateTime lastTimestampsForSentEvent = new EventsDateTime();

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("LastTimestampsForEvent = ").append(lastTimestampsForEvent.toString()).append("\n");
        out.append("LastTimestampsForSentEvent=").append(lastTimestampsForSentEvent).toString();
        return out.toString();
    }
}
