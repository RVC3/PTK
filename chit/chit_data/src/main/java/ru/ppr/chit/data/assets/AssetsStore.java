package ru.ppr.chit.data.assets;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import ru.ppr.logger.Logger;

/**
 * Обертка для доступа к ресурсам из /assets.
 *
 * @author Aleksandr Brazhkin
 */
public class AssetsStore {

    private static String TAG = Logger.makeLogTag(AssetsStore.class);

    private final Context context;

    @Inject
    AssetsStore(Context context) {
        this.context = context;
    }

    /**
     * Загружает контент из /assets в виде строки.
     */
    public String getString(Entry entry) {
        return new String(loadAsByteArray(entry.path()));
    }

    /**
     * Загружает контент из /assets в виде {@link InputStream}.
     */
    public InputStream getInputStream(Entry entry) throws IOException {
        return context.getAssets().open(entry.path());
    }

    /**
     * Загружает контент из /assets в виде массива байтов.
     */
    private byte[] loadAsByteArray(String path) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(path);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            return buffer;
        } catch (IOException e) {
            Logger.error(TAG, "Load asset failed", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.error(TAG, "Couldn't close inputStream", e);
            }

        }
        return new byte[0];
    }

    public interface Entry {

        String path();

        String uri();
    }
}
