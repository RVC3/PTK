package ru.ppr.cppk.helpers.lifecycle;

import java.util.LinkedList;

/**
 * Класс-помощник для выполнения операций только в активном состояни экрана.
 *
 * @author Aleksandr Brazhkin
 */
public class LifecycleActionHandler implements LifecycleListener {

    public static LifecycleActionHandler newStartStopInstance() {
        return new LifecycleActionHandler(LifecycleEvent.ON_START, LifecycleEvent.ON_STOP);
    }

    public static LifecycleActionHandler newResumePauseInstance() {
        return new LifecycleActionHandler(LifecycleEvent.ON_RESUME, LifecycleEvent.ON_PAUSE);
    }

    private LinkedList<Action> actions = new LinkedList<>();
    private boolean isStarted;
    private final LifecycleEvent mStartAction;
    private final LifecycleEvent mStopAction;

    private LifecycleActionHandler(LifecycleEvent startAction, LifecycleEvent stopAction) {
        this.mStartAction = startAction;
        this.mStopAction = stopAction;
    }

    public void post(Action action) {
        actions.addLast(action);
        startInternal();
    }

    private void stop() {
        isStarted = false;
    }

    private void start() {
        isStarted = true;
        startInternal();
    }

    private void startInternal() {
        if (!isStarted) {
            return;
        }
        while (!actions.isEmpty()) {
            Action action = actions.removeFirst();
            action.call();
        }
    }

    @Override
    public void onEvent(LifecycleEvent lifecycleEvent) {
        if (lifecycleEvent == mStartAction) {
            start();
        } else if (lifecycleEvent == mStopAction) {
            stop();
        }
    }

    public interface Action {
        void call();
    }
}
