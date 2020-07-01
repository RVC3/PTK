package ru.ppr.chit.rfid;

import dagger.Module;
import dagger.Provides;
import ru.ppr.core.dataCarrier.findcardtask.authstrategy.nsi.NsiDataProvider;
import ru.ppr.core.manager.RfidManager;
import ru.ppr.rfid.IRfid;

/**
 * @author Aleksandr Brazhkin
 */
@Module
public class RfidModule {

    @Provides
    RfidManager.ConfigProvider rfidManagerConfigProvider(RfidManagerConfigProvider rfidManagerConfigProvider) {
        return rfidManagerConfigProvider;
    }

    @Provides
    IRfid rfid(RfidManager rfidManager) {
        return rfidManager.getRfid();
    }

    @Provides
    NsiDataProvider nsiDataProvider(NsiDataProviderImpl nsiDataProvider) {
        return nsiDataProvider;
    }
}
