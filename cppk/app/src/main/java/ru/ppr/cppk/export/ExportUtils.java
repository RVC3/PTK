package ru.ppr.cppk.export;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.legacy.EcpUtils;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;
import ru.ppr.utils.MD5Utils;
import rx.Observable;

import static ru.ppr.logger.Logger.makeLogTag;

/**
 * Класс с функциями используемыми при экспорте данных, для работы с АРМ.
 *
 * @author G.Kashka
 */
public class ExportUtils {

    private static final String TAG = makeLogTag(ExportUtils.class);

    /**
     * создает подпись для файла. первые 4 байта номер эцп остальные сама
     * подпись
     *
     * @param file
     * @return
     */
    public static SignDataResult createSigToFile(File file, Globals globals) {
        return Observable
                .fromCallable(() -> {
                    Logger.trace(TAG, "createSigToFile(" + file + ") START");
                    final byte[] md5 = MD5Utils.generateMD5(file);
                    return Di.INSTANCE.getEdsManager().signData(md5, new Date());
                })
                .observeOn(SchedulersCPPK.background())
                .doOnNext(signDataResult -> Logger.trace(TAG, "createSigToFile(" + file + ") FINISH return: " + (new Gson().toJson(signDataResult))))
                .doOnError(error -> Logger.trace(TAG, error))
                .subscribeOn(SchedulersCPPK.eds())
                .toBlocking()
                .single();
    }

    /**
     * формирует строку, которую нужно записать в файл подписи.
     *
     * @param sign
     * @param numberEcp
     * @return
     */
    public static byte[] createSigData(byte[] sign, long numberEcp) {
        byte[] keyEcpAndEcp = new byte[1];
        byte[] ecpKey = ByteBuffer.allocate(4).putInt(EcpUtils.convertLongToInt(numberEcp)).array();
        if (sign == null) {
            Logger.trace(TAG, "createSigData: sign is null!");
            return keyEcpAndEcp;
        }
        keyEcpAndEcp = new byte[sign.length + ecpKey.length];
        System.arraycopy(ecpKey, 0, keyEcpAndEcp, 0, ecpKey.length);
        System.arraycopy(sign, 0, keyEcpAndEcp, ecpKey.length, sign.length);

        Logger.trace(TAG, "createSigData: sign - " + CommonUtils.bytesToHexWithoutSpaces(sign) + ", "
                + "numberEcp - " + numberEcp + ", "
                + "numberEcpBytes - " + CommonUtils.bytesToHexWithoutSpaces(ecpKey) + ", "
                + "result - " + CommonUtils.bytesToHexWithoutSpaces(keyEcpAndEcp));

        return keyEcpAndEcp;
    }

    /**
     * создает файл подписи. сначала временный файл, потом переименовывает
     *
     * @param sig
     * @param sigfile
     * @return
     * @throws IOException
     */
    public static boolean saveSigToFile(byte[] sig, File sigfile) {
        // вставляем костыль чтобы подпись всегда была минимум 4 байта
        if (sig == null || sig.length < 4)
            sig = new byte[4];
        if (sigfile.exists()) {
            sigfile.delete();
        } else {
            sigfile.getParentFile().mkdirs();
        }
        Logger.trace(TAG, "saveSigToFile() " + CommonUtils.bytesToHexWithoutSpaces(sig) + " -> " + sigfile.getPath());
        FileOutputStream fos = null;
        boolean result = false;
        try {
            fos = new FileOutputStream(sigfile);
            fos.write(sig);
            fos.flush();
            result = true;
        } catch (IOException e) {
            Logger.error(TAG, e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Logger.trace(TAG, "saveSigToFile() Error close FileOutputStream", e);
                }
            }
        }
        return result;
    }


    /**
     * создает папки для обмена данными с АРМ
     */
    public static void createExchangeFolders(Globals g) {
        (new File(Exchange.KPP)).mkdirs();
        (new File(Exchange.RDS)).mkdirs();
        (new File(Exchange.SECURITY)).mkdirs();
        (new File(Exchange.SFT_OUT)).mkdirs();
        (new File(Exchange.SFT_LIC)).mkdirs();
        (new File(Exchange.SOFTWARE)).mkdirs();
        (new File(Exchange.SOFTWARE_NEW)).mkdirs();
        (new File(Exchange.STATE)).mkdirs();
        (new File(Exchange.CANCEL)).mkdirs();

        Dagger.appComponent().filePathProvider().getBackupsReplaceDir().mkdirs();

        Dagger.appComponent().filePathProvider().getBackupsRestoreDir().mkdirs();
        (new File(PathsConstants.PRINTER)).mkdirs();
        (new File(PathsConstants.IMAGE_RFID)).mkdirs();
        (new File(PathsConstants.IMAGE_BARCODE)).mkdirs();
        (new File(PathsConstants.LOG_FATALS)).mkdirs();
        (new File(PathsConstants.LOG_APP)).mkdirs();
        Dagger.appComponent().filePathProvider().getInfotecsLogsDir().mkdirs();

        Di.INSTANCE.getEdsManager().getEdsDirs().getEdsTransportInDir().mkdirs();
        Di.INSTANCE.getEdsManager().getEdsDirs().getEdsTransportOutDir().mkdirs();
    }

    @SuppressWarnings("unchecked")
    public static JSONObject deepMerge(JSONObject source, JSONObject target) throws JSONException {
        if (source == null) {
            return target;
        }
        Iterator<String> itr = source.keys();
        while (itr.hasNext()) {
            String key = itr.next();
            Object value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.put(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) value;
                    deepMerge(valueJson, target.getJSONObject(key));
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }

    /**
     * Вернет временный файл с уникальным именем, привязанным к времени создания
     *
     * @param file
     * @return
     */
    public static File getTempFile(File file, long requestTimeStamp) {
        //https://aj.srvdev.ru/browse/CPPKPP-26791
        String datetime = DateFormatOperations.getUtcString(new Date()).replace(":", "-").replace("T", "_").replace("Z", "");
        return new File(file.getAbsolutePath() + "_temp_" + datetime + "_" + requestTimeStamp);
    }

    public static File getTempFile(String absFilePath, long requestTimeStamp) {
        return getTempFile(new File(absFilePath), requestTimeStamp);
    }

}
