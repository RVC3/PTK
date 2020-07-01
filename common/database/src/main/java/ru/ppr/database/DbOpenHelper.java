package ru.ppr.database;

/**
 * @author Aleksandr Brazhkin
 */
public interface DbOpenHelper {

    void onCreate(Database db);

    void onUpgrade(Database db, int oldVersion, int newVersion);

    void onOpen(Database db);

    String getDatabaseName();

    Database getWritableDatabase();

    Database getReadableDatabase();

    void close();
}
