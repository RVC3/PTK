package ru.ppr.cppk.pos;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import javax.inject.Inject;

import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.managers.handler.IngenicoBluetoothHandler;
import ru.ppr.cppk.managers.handler.IngenicoNetworkHandler;
import ru.ppr.cppk.managers.handler.InpasBluetoothHandler;
import ru.ppr.cppk.managers.handler.InpasNetworkHandler;
import ru.ppr.cppk.utils.InternalPos9000S;
import ru.ppr.ingenico.core.IngenicoTerminal;
import ru.ppr.inpas.terminal.InpasTerminal;
import ru.ppr.ipos.IPos;
import ru.ppr.ipos.stub.StubTerminal;
import ru.ppr.ipos.stub.db.PosDbStorage;

/**
 * Фабрика POS терминала.
 */
public class PosTerminalFactory implements IPosTerminalFactory {

    private final Context context;

    @Inject
    public PosTerminalFactory(Context context) {
        this.context = context;
    }

    /**
     * С помощью данного метода можно получить конкретный экземпляр POS терминала.
     * По умолчанию возвращается {@link StubTerminal}.
     *
     * @param posType    тип POS терминала.
     * @param macAddress MAC адрес POS терминала.
     * @return конкретный экземпляр POS терминала.
     */
    @Nullable
    @Override
    public IPos getPosTerminal(@NonNull final PosType posType, @NonNull final String macAddress) {
        final Globals globals = Globals.getInstance();
        IPos posTerminal = null;

        switch (posType) {
            case DEFAULT: {
                PosDbStorage posDbStorage = new PosDbStorage(context);
                //https://aj.srvdev.ru/browse/CPPKPP-32274
                if (BuildConfig.DISABLE_STUB_POS)
                    throw new IllegalStateException("StubTerminal запрещен для этой сборки");
                posTerminal = new StubTerminal(globals, 5000, posDbStorage);
            }
            break;

            case BUILTIN: {
                posTerminal = new InternalPos9000S(globals,
                        Di.INSTANCE.networkManager(),
                        20501,
                        15000);
                break;
            }
            case INGENICO: {
                posTerminal = new IngenicoTerminal(
                        globals,
                        new IngenicoBluetoothHandler(Di.INSTANCE.bluetoothManager()),
                        new IngenicoNetworkHandler(Di.INSTANCE.networkManager()),
                        IngenicoTerminal.DEFAULT_PREFS_FILE_NAME,
                        BuildConfig.APPLICATION_ID,
                        IngenicoTerminal.DEFAULT_CONFIG_FILE_NAME,
                        macAddress,
                        9301,
                        15000
                );
            }
            break;

            case INPAS: {
                final String mac = TextUtils.isEmpty(macAddress) ? "00:00:00:00:00:00" : macAddress;

                posTerminal = new InpasTerminal(
                        globals,
                        mac,
                        new InpasBluetoothHandler(Di.INSTANCE.bluetoothManager()),
                        new InpasNetworkHandler(Di.INSTANCE.networkManager())
                );
                posTerminal.setConnectionTimeout(60_000L);
            }
            break;

            default: {
                throw new IllegalArgumentException("Not implemented POS terminal.");
            }
        }

        return posTerminal;
    }

}