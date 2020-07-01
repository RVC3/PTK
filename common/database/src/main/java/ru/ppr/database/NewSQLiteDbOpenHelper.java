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
package ru.ppr.database;

import android.content.Context;

import org.sqlite.database.sqlite.SQLiteDatabase;

/**
 * Like android.database.sqlite.SQLiteOpenHelper, but uses greenDAO's {@link Database} abstraction to create and update an unencrypted database.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class NewSQLiteDbOpenHelper implements DbOpenHelper {

    static {
        System.loadLibrary("sqliteX");
    }

    private final Adapter delegate;

    private Database database = null;
    private SQLiteDatabase prevDelegateSQLiteDatabase = null;

    public NewSQLiteDbOpenHelper(Context context, String name, int version) {
        delegate = new Adapter(context, name, version);
    }

    public String getDatabaseName() {
        return delegate.getDatabaseName();
    }

    @Override
    public Database getWritableDatabase() {
        return wrap(delegate.getWritableDatabase());
    }

    @Override
    public Database getReadableDatabase() {
        return wrap(delegate.getReadableDatabase());
    }

    @Override
    public void close() {
        delegate.close();
    }

    protected Database wrap(SQLiteDatabase sqLiteDatabase) {
        if (prevDelegateSQLiteDatabase != sqLiteDatabase) {
            if (sqLiteDatabase == null) {
                database = null;
                prevDelegateSQLiteDatabase = null;
            } else {
                database = new NewSQLiteDatabase(sqLiteDatabase);
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
            NewSQLiteDbOpenHelper.this.onCreate(wrap(db));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            NewSQLiteDbOpenHelper.this.onUpgrade(wrap(db), oldVersion, newVersion);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            NewSQLiteDbOpenHelper.this.onOpen(wrap(db));
        }
    }

}
