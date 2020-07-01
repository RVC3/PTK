package ru.ppr.cppk.helpers;

import javax.inject.Inject;

/**
 * Created by Александр on 06.06.2016.
 */
public class DeferredActionHandler {

    private Runnable runnable;
    private boolean isPaused = false;

    @Inject
    public DeferredActionHandler() {

    }

    public void post(Runnable runnable) {
        if (this.runnable != null) {
            throw new IllegalStateException("Runnable already exists");
        }
        this.runnable = runnable;
        start();
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
        start();
    }

    public void start() {
        if (isPaused || runnable == null) {
            return;
        }
        runnable.run();
        runnable = null;
    }
}
