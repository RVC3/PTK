package ru.ppr.chit.helpers;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class ActivityCallbacks implements Application.ActivityLifecycleCallbacks {

    private final static String TAG = Logger.makeLogTag(ActivityCallbacks.class);

    private final HashMap<Activity, State> activities = new HashMap<>();

    @Inject
    ActivityCallbacks() {

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activities.put(activity, State.CREATED);
        logActivities();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        activities.put(activity, State.STARTED);
        logActivities();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        activities.put(activity, State.RESUMED);
        logActivities();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        activities.put(activity, State.PAUSED);
        logActivities();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activities.put(activity, State.STOPPED);
        logActivities();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activities.remove(activity);
        logActivities();
    }

    enum State {
        CREATED,
        STARTED,
        RESUMED,
        PAUSED,
        STOPPED
    }

    private void logActivities() {
        Logger.trace(TAG, "Launched activities:");
        for (Map.Entry<Activity, State> entry : activities.entrySet()) {
            Logger.trace(TAG, "" + entry.getKey() + ", " + entry.getValue());
        }
    }
}
