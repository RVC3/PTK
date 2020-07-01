package ru.ppr.chit.data.assets;

/**
 * Ресурс из assets.
 *
 * @author Aleksandr Brazhkin
 */
public enum AssetsEntry implements AssetsStore.Entry {
    /**
     * Файл локальной БД
     */
    LOCAL_DB("databases/localDb.db"),
    /**
     * Файл БД НСИ
     */
    NSI_DB("databases/nsiDb.db"),
    /**
     * Файл БД безопасности
     */
    SECURITY_DB("databases/securityDb.db");

    /**
     * Относительный путь до ресурса в папке /assets
     */
    private final String path;

    AssetsEntry(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String uri() {
        return "file:///android_asset/" + path;
    }
}
