package ru.ppr.chit;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import ru.ppr.chit.api.auth.OAuth2TokenStorage;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.bs.oauth2token.OAuth2TokenManager;
import ru.ppr.chit.bs.synchronizer.SynchronizerInformer;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.chit.domain.model.local.AppRuntimeProperty;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.boarding.BoardingManager;
import ru.ppr.chit.domain.controlstation.ControlStationManager;
import ru.ppr.chit.domain.exchangeevent.ExchangeEventManager;
import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.domain.ticket.TicketManager;
import ru.ppr.chit.domain.tripservice.TripServiceModeManager;
import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.chit.helpers.PdWithPlaceBarcodeStorage;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.chit.helpers.readbscstorage.PdWithPlaceCardDataStorage;
import ru.ppr.chit.manager.SoftwareUpdateManager;
import ru.ppr.chit.manager.WiFiManager;
import ru.ppr.core.dataCarrier.findcardtask.FindCardTaskFactory;
import ru.ppr.core.dataCarrier.pd.PdEncoderFactory;
import ru.ppr.core.dataCarrier.readbarcodetask.ReadBarcodeTaskFactory;
import ru.ppr.core.domain.model.ApplicationInfo;
import ru.ppr.core.domain.model.DeviceInfo;
import ru.ppr.core.manager.BatteryManager;
import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.core.ui.mvp.core.MvpProcessor;
import ru.ppr.rfid.IRfid;

/**
 * @author Dmitry Nevolin
 */
@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(App app);

    App app();

    Context context();

    MvpProcessor mvpProcessor();

    FilePathProvider filePathProvider();

    PdWithPlaceCardDataStorage pdWithPlaceCardDataStorage();

    PdWithPlaceBarcodeStorage pdWithPlaceBarcodeStorage();

    UiThread uiThread();

    ApiManager apiManager();

    LocalDbManager localDbManager();

    NsiDbManager nsiDbManager();

    SecurityDbManager securityDbManager();

    ApplicationInfo applicationInfo();

    DeviceInfo deviceInfo();

    AppProperties appProperties();

    AppRuntimeProperty appRuntimeProperty();

    BatteryManager batteryManager();

    IBluetoothManager bluetoothManager();

    TripServiceInfoStorage tripServiceInfoStorage();

    OAuth2TokenStorage oAuth2TokenStorage();

    IRfid rfid();

    FindCardTaskFactory findCardTaskFactory();

    ReadBarcodeTaskFactory readBarcodeTaskFactory();

    BoardingManager boardingManager();

    PdEncoderFactory pdEncoderFactory();

    WiFiManager wiFiManager();

    EdsManager edsManager();

    SoftwareUpdateManager softwareUpdateManager();

    ExchangeEventManager exchangeEventManager();

    OAuth2TokenManager oAuth2TokenManager();

    RegistrationInformant registrationInformant();

    SynchronizerInformer synchronizerInformer();

    AppPropertiesRepository appPropertiesRepository();

    ControlStationManager controlStationManager();

    TicketManager ticketManager();

    TripServiceModeManager tripServiceModeManager();

}
