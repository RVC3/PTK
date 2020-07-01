package ru.ppr.chit.ui.activity.workingstate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.model.nsi.Version;
import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface WorkingStateView extends MvpView {

    void setTerminalId(@Nullable Long terminalId);

    void setWifiSsid(@Nullable String wifiSsid);

    void setBsId(@Nullable String bsId);

    void setSoftwareVersion(@NonNull String softwareVersion);

    void setNsiVersion(@Nullable Version version);

    void setForceSyncVisible(boolean visible);

    void setBsConnectVisible(boolean visible);

    void setBsConnectBrokenError(String message);

    void setSyncProgressVisible(boolean visible);

    void setSyncProgressMessage(String message);

    void setSyncErrorMessage(String message);

    void setLastExchangeInfo(@Nullable LastExchangeInfo lastExchangeEvent);

    void setNotExported(int notExported);

    class LastExchangeInfo {

        boolean success;
        Date date;

    }

}
