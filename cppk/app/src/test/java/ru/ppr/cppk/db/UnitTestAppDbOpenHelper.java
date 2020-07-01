package ru.ppr.cppk.db;

import android.content.Context;

import ru.ppr.core.database.AppDbOpenHelper;
import ru.ppr.database.Database;
import ru.ppr.database.StandardDbOpenHelper;

/**
 * Created by Александр on 24.05.2016.
 *
 * Альтернатива {@link AppDbOpenHelper )} для тестов
 */
public class UnitTestAppDbOpenHelper extends StandardDbOpenHelper {

    public UnitTestAppDbOpenHelper(Context context, String name) {
        super(context, name, 1);
    }

    @Override
    public void onCreate(Database db) {

    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(Database db) {

    }
}
