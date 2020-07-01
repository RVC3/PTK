package ru.ppr.chit.ui.activity.passengerlist;


import java.util.List;

import ru.ppr.chit.ui.activity.passengerlist.model.PassengerInfo;
import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
public interface PassengerListView extends MvpView {

    // Перегрузка существующего списка
    void reloadPassengers();

    // Подгрузка в список новых записей. Все записи, начиная с positionFrom будут удалены и заменены на новые
    // hasMoreData - признак, что не все данные загружены в список
    void updatePassengers(List<PassengerInfo> passengers, int positionFrom, boolean hasMoreData);
}
