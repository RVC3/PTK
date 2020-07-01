package ru.ppr.chit.data.db;

import io.reactivex.Observable;

/**
 * Класс для работы с БД.
 *
 * @author Aleksandr Brazhkin
 */
public interface DbManager<DS> {

    DS daoSession();

    Observable<Boolean> connectionState();

    Observable<Boolean> endsOfTransactions();
}
