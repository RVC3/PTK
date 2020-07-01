package ru.ppr.cppk.managers;

/**
 * Created by Александр on 17.10.2016.
 */

public class ScreenLockManagerStub implements ScreenLockManager {

    public ScreenLockManagerStub(){

    }

    @Override
    public void destroy() {

    }

    @Override
    public void updateLastActionTimestamp() {

    }

    @Override
    public boolean isScreenShouldBeLocked() {
        return false;
    }

    @Override
    public void setLockDelay(int lockDelay) {

    }

    @Override
    public void reset() {

    }

    @Override
    public void addScreenLockListener(ScreenLockListener screenLockListener) {

    }

    @Override
    public void removeScreenLockListener(ScreenLockListener screenLockListener) {

    }
}
