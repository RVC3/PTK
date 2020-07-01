package ru.ppr.chit.ui.activity.readbsqrcode;

import dagger.Binds;
import dagger.Module;
import ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.AuthInfoReader;
import ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.barcode.BarcodeAuthInfoReader;
import ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.network.NetworkAuthInfoModule;
import ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.network.NetworkAuthInfoReader;

/**
 * @author Aleksandr Brazhkin
 */
@Module(includes = NetworkAuthInfoModule.class)
abstract class ReadBsQrCodeModule {

    @Binds
    public abstract AuthInfoReader authInfoReader(BarcodeAuthInfoReader authInfoReader);

}
