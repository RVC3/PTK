package ru.ppr.chit.domain.event;

import android.support.annotation.NonNull;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.chit.domain.model.local.Event;

/**
 * Билдер сущности {@link Event}.
 *
 * @author Aleksandr Brazhkin
 */
public class EventBuilder {

    @Inject
    EventBuilder() {
    }

    @NonNull
    public Event build() {
        Event event = new Event();
        event.setCreatedAt(new Date());
        return event;
    }
}
