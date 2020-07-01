/*
 * Copyright (C) 2011-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ppr.database.greendao;

import android.content.Context;

import org.sqlite.database.sqlite.SQLiteDatabase;

import ru.ppr.database.DbOpenHelper;
import ru.ppr.database.NewSQLiteReadOnlyOpenHelper;

/**
 * @author Aleksandr Brazhkin
 */
public abstract class GreenDaoNewSQLiteDbOpenHelper implements DbOpenHelper {

    static {
        System.loadLibrary("sqliteX");
    }

    private final Adapter delegate;

    private GreenDaoDatabase database = null;
    private SQLiteDatabase prevDelegateSQLiteDatabase = null;

    public GreenDaoNewSQLiteDbOpenHelper(Context context, String name, int version) {
        delegate = new Adapter(context, name, version);
    }

    public String getDatabaseName() {
        return delegate.getDatabaseName();
    }

    @Override
    public GreenDaoDatabase getWritableDatabase() {
        return wrap(delegate.getWritableDatabase());
    }

    @Override
    public GreenDaoDatabase getReadableDatabase() {
        return wrap(delegate.getReadableDatabase());
    }

    @Override
    public void close() {
        delegate.close();
    }

    private GreenDaoDatabase wrap(SQLiteDatabase sqLiteDatabase) {
        if (prevDelegateSQLiteDatabase != sqLiteDatabase) {
            if (sqLiteDatabase == null) {
                database = null;
                prevDelegateSQLiteDatabase = null;
            } else {
                database = new GreenDaoNewSQLiteDatabase(sqLiteDatabase);
                prevDelegateSQLiteDatabase = sqLiteDatabase;
            }
        }

        return database;
    }

    private class Adapter extends NewSQLiteReadOnlyOpenHelper {

        Adapter(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            GreenDaoNewSQLiteDbOpenHelper.this.onCreate(wrap(db));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            GreenDaoNewSQLiteDbOpenHelper.this.onUpgrade(wrap(db), oldVersion, newVersion);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            GreenDaoNewSQLiteDbOpenHelper.this.onOpen(wrap(db));
        }
    }

}
