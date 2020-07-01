package ru.ppr.cppk.ui.activity.transfersalestart;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.ui.mvp.view.MvpView;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Dmitry Nevolin
 */
interface TransferSaleStartView extends MvpView {

    void showLoadingDialog();

    void hideLoadingDialog();

    void setDepartureStationName(String departureStationName);

    void setDestinationStationName(String destinationStationName);

    void setDepartureStations(@NonNull List<Station> stations);

    void setDestinationStations(@NonNull List<Station> stations);

    void setTicketTypes(@NonNull List<TicketType> ticketTypes);

    void setTicketTypesSelectVisible(boolean visible);

    void setSelectedTicketTypePosition(int position);

    void setContinueBtnVisible(boolean visible);

    void setCriticalNsiBackDialogVisible(boolean visible);

    void setCriticalNsiCloseShiftDialogVisible(boolean visible);

    void showNoStationsForSaleError();
}
