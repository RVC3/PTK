package ru.ppr.cppk.settings;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Хранилище состояния окна {@Link SplashActivity}
 *
 * @author Dmitry Vinogradov
 */
@Singleton
public class SplashViewManager {

    private State state;
    private String backupPath;
    private boolean successBackup;

    @Inject
    public SplashViewManager() {
        state = State.DEFAULT;
        backupPath = "";
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public boolean getSuccessBackup() {
        return successBackup;
    }

    public void setSuccessBackup(boolean successBackup) {
        this.successBackup = successBackup;
    }

    public enum State {
        DEFAULT,
        ATTENTION_DIALOG,
        IN_PROGRESS,
        BACKUP_COMPLETED_DIALOG;
    }
}
