package ru.ppr.cppkupdater;

public interface InstallListener {
    /**
     * true - удалить apk файл по окончанию, false - не удалять
     *
     * @param isOk
     * @param error
     * @return
     */
    public boolean ready(boolean isOk, String error);
}
