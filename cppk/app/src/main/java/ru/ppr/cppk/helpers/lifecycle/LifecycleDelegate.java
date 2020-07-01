package ru.ppr.cppk.helpers.lifecycle;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Провайдер жизненного цикла {@link android.app.Activity} и {@link android.app.Fragment}
 *
 * @author Aleksandr Brazhkin
 */
public class LifecycleDelegate {

    private final Map<LifecycleEvent, Set<LifecycleListener>> listeners = new HashMap<>();

    public LifecycleDelegate() {
        listeners.put(LifecycleEvent.ON_CREATE, new LinkedHashSet<>());
        listeners.put(LifecycleEvent.ON_START, new LinkedHashSet<>());
        listeners.put(LifecycleEvent.ON_RESUME, new LinkedHashSet<>());
        listeners.put(LifecycleEvent.ON_PAUSE, new LinkedHashSet<>());
        listeners.put(LifecycleEvent.ON_STOP, new LinkedHashSet<>());
        listeners.put(LifecycleEvent.ON_DESTROY, new LinkedHashSet<>());
    }

    public void addLifecycleListener(LifecycleListener lifecycleListener, EnumSet<LifecycleEvent> lifecycleEvents) {
        for (LifecycleEvent lifecycleEvent : lifecycleEvents) {
            listeners.get(lifecycleEvent).add(lifecycleListener);
        }
    }

    public void removeLifecycleListener(LifecycleListener lifecycleListener, EnumSet<LifecycleEvent> lifecycleEvents) {
        for (LifecycleEvent lifecycleEvent : lifecycleEvents) {
            listeners.get(lifecycleEvent).remove(lifecycleListener);
        }
    }

    public void onCreate() {
        for (LifecycleListener lifecycleListener : listeners.get(LifecycleEvent.ON_CREATE)) {
            lifecycleListener.onEvent(LifecycleEvent.ON_CREATE);
        }
    }

    public void onStart() {
        for (LifecycleListener lifecycleListener : listeners.get(LifecycleEvent.ON_START)) {
            lifecycleListener.onEvent(LifecycleEvent.ON_START);
        }
    }

    public void onResume() {
        for (LifecycleListener lifecycleListener : listeners.get(LifecycleEvent.ON_RESUME)) {
            lifecycleListener.onEvent(LifecycleEvent.ON_RESUME);
        }
    }

    public void onPause() {
        for (LifecycleListener lifecycleListener : listeners.get(LifecycleEvent.ON_PAUSE)) {
            lifecycleListener.onEvent(LifecycleEvent.ON_PAUSE);
        }
    }

    public void onStop() {
        for (LifecycleListener lifecycleListener : listeners.get(LifecycleEvent.ON_STOP)) {
            lifecycleListener.onEvent(LifecycleEvent.ON_STOP);
        }
    }

    public void onDestroy() {
        for (LifecycleListener lifecycleListener : listeners.get(LifecycleEvent.ON_DESTROY)) {
            lifecycleListener.onEvent(LifecycleEvent.ON_DESTROY);
        }
    }
}
