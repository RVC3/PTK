package ru.ppr.chit.bs.synchronizer.base;

/**
 * Класс заглушка с пустой реализацией
 *
 * @author m.sidorov
 */
public class BackupManagerStub implements BackupManager {

    @Override
    public void backup() throws SynchronizeException {
    }

    @Override
    public void restore() throws SynchronizeException {
    }

    @Override
    public boolean hasBackup() {
        return false;
    }
}
