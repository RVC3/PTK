package ru.ppr.cppk.dataCarrier.rfid;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
}
