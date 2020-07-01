package ru.ppr.cppk.debug;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.export.Exchange;
import ru.ppr.cppk.export.Response;
import ru.ppr.cppk.export.model.request.GetEventsReq;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.managers.ScreenLockManager;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.utils.CommonSettingsUtils;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils;

/**
 * Created by григорий on 08.09.2016.
 */
public class DebugSyncActivity extends LoggedActivity implements View.OnClickListener {

    private static final String TAG = Logger.makeLogTag(DebugSyncActivity.class);
    private static final Object LOCK = new Object();

    private TextView commonSettingsLastUpdateTextView;

    private FeedbackProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_sync_activity);

        //состояние
        findViewById(R.id.getStateResp).setOnClickListener(this);
        findViewById(R.id.lastShift_resp).setOnClickListener(this);

        //выгрузки
        findViewById(R.id.createAllEventResp).setOnClickListener(this);
        findViewById(R.id.createShiftEventsResp).setOnClickListener(this);
        findViewById(R.id.createTestTicketResp).setOnClickListener(this);
        findViewById(R.id.createTicketContolResp).setOnClickListener(this);
        findViewById(R.id.createServiceTicketContolResp).setOnClickListener(this);
        findViewById(R.id.createTicketSalesResp).setOnClickListener(this);
        findViewById(R.id.createTicketReturnsResp).setOnClickListener(this);
        findViewById(R.id.createTicketReSignsResp).setOnClickListener(this);
        findViewById(R.id.createServiceSalesResp).setOnClickListener(this);
        findViewById(R.id.createTicketTapeEventResp).setOnClickListener(this);
        findViewById(R.id.createMonthClosingResp).setOnClickListener(this);
        findViewById(R.id.createFinePaidEventsResp).setOnClickListener(this);
        findViewById(R.id.createBankTransactionsResp).setOnClickListener(this);

        //настройки
        commonSettingsLastUpdateTextView = (TextView) findViewById(R.id.commonSettingsLastUpdate);
        refreshCommonSettingsLastUpdate();
        findViewById(R.id.exportCommonSettingsBtn).setOnClickListener(this);
        findViewById(R.id.importCommonSettingsBtn).setOnClickListener(this);
        findViewById(R.id.getTimeRespBtn).setOnClickListener(this);
        findViewById(R.id.getPrivateSettingsBtn).setOnClickListener(this);

        //ПО
        findViewById(R.id.updateApp).setOnClickListener(this);

        //Резарвное копирование
        findViewById(R.id.syncBackup).setOnClickListener(this);

        //SFT
        findViewById(R.id.createTransmissionCompleteResp).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        Logger.trace(TAG, "onClick()");

        String syncType = ((Button) v).getText().toString();
        dialog = new FeedbackProgressDialog(DebugSyncActivity.this);
        dialog.setMessage("Выгружаем: " + syncType);
        dialog.show();

        long time = System.currentTimeMillis();

        final int id = v.getId();

        SchedulersCPPK.backgroundExecutor().execute(() -> {
            switch (id) {
                case R.id.getStateResp: {
                    Logger.trace(TAG, "case R.id.getStateResp");
                    Globals.getInstance().getResponse().createGetStateResp(System.currentTimeMillis());
                    break;
                }

                case R.id.lastShift_resp: {
                    Logger.trace(TAG, "case R.id.lastShift_resp");
                    Globals.getInstance().getResponse().createGetLastShiftResp(new Exchange.Error(), System.currentTimeMillis());
                    break;
                }

                case R.id.createAllEventResp: {
                    Logger.trace(TAG, "case R.id.createAllEventResp");
                    Globals.getInstance().getResponse().createGetEventsResp(new GetEventsReq(), new Exchange.Error(), System.currentTimeMillis());
                    break;
                }

                case R.id.createShiftEventsResp: {
                    Logger.trace(TAG, "case R.id.createShiftEventsResp");
                    Globals.getInstance().getResponse().createShiftEventsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.createTestTicketResp:
                    Logger.trace(TAG, "case R.id.createTestTicketResp");
                    Globals.getInstance().getResponse().createTestTicketsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;

                case R.id.createTicketContolResp:
                    Logger.trace(TAG, "case R.id.createTicketContolResp");
                    Globals.getInstance().getResponse().createTicketControlsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;

                case R.id.createServiceTicketContolResp:
                    Logger.trace(TAG, "case R.id.createServiceTicketContolResp");
                    Globals.getInstance().getResponse().createServiceTicketControlsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;

                case R.id.createTicketSalesResp: {
                    Logger.trace(TAG, "case R.id.createTicketSalesResp");
                    Globals.getInstance().getResponse().createTicketSalesResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.createTicketReturnsResp: {
                    Logger.trace(TAG, "case R.id.createTicketReturnsResp");
                    Globals.getInstance().getResponse().createTicketReturnsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.createTicketReSignsResp: {
                    Logger.trace(TAG, "case R.id.createTicketReSignsResp");
                    Globals.getInstance().getResponse().createTicketReSignsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.createServiceSalesResp: {
                    Logger.trace(TAG, "case R.id.createServiceSalesResp");
                    Globals.getInstance().getResponse().createServiceSalesResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.createTicketTapeEventResp: {
                    Logger.trace(TAG, "case R.id.createTicketTapeEventResp");
                    Globals.getInstance().getResponse().createTicketPaperRollsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.createMonthClosingResp: {
                    Logger.trace(TAG, "case R.id.createMonthClosingResp");
                    Globals.getInstance().getResponse().createMonthClosuresResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.createFinePaidEventsResp: {
                    Logger.trace(TAG, "case R.id.createFinePaidEventsResp");
                    Globals.getInstance().getResponse().createFinePaidEventsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.createBankTransactionsResp: {
                    Logger.trace(TAG, "case R.id.createBankTransactionsResp");
                    Globals.getInstance().getResponse().createBankTransactionsResp(0, new Response.ResponsePacket(), System.currentTimeMillis());
                    break;
                }

                case R.id.exportCommonSettingsBtn: {
                    Logger.trace(TAG, "case R.id.exportCommonSettingsBtn");
                    exportCommonSettings();
                    break;
                }

                case R.id.importCommonSettingsBtn: {
                    Logger.trace(TAG, "case R.id.importCommonSettingsBtn");
                    importCommonSettings();
                    break;
                }

                case R.id.getPrivateSettingsBtn: {
                    Logger.trace(TAG, "case R.id.getPrivateSettingsBtn");
                    Globals.getInstance().getResponse().createGetSettingsResp(new Exchange.Error(), System.currentTimeMillis());
                    break;
                }

                case R.id.getTimeRespBtn: {
                    Logger.trace(TAG, "case R.id.getTimeRespBtn");
                    Globals.getInstance().getResponse().createGetTimeResp(new Exchange.Error(), System.currentTimeMillis());
                    break;
                }

                case R.id.updateApp: {
                    Logger.trace(TAG, "case R.id.updateApp");
                    Globals.getInstance().getResponse().createNewVersionResp(new File(Exchange.SOFTWARE + "/newVersion.apk_temp"), new Exchange.Error(), System.currentTimeMillis());
                    break;
                }

                case R.id.syncBackup: {
                    Logger.trace(TAG, "case R.id.syncBackup");
                    Globals.getInstance().getResponse().createGetBackupResp(new Exchange.Error(), System.currentTimeMillis());
                    break;
                }

                case R.id.createTransmissionCompleteResp: {
                    Logger.trace(TAG, "case R.id.createTransmissionCompleteResp");
                    Globals.getInstance().getResponse().createTransmissionCompleteResp1(new File(Exchange.SFT + "/in.tar.gz_temp"), new File(Exchange.SFT + "/sftfilestodelete.bin_temp"), System.currentTimeMillis());
                    break;
                }


            }

            runOnUiThread(() -> {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                long fullMs = System.currentTimeMillis() - time;
                long h = fullMs / 1000 / 60 / 60;
                long m = (fullMs - h * 1000 * 60 * 60) / 1000 / 60;
                long s = (fullMs - h * 1000 * 60 * 60 - m * 1000 * 60) / 1000;
                long ms = (fullMs - h * 1000 * 60 * 60 - m * 1000 * 60 - s * 1000);
                String log = "Завершена выгрузка " + syncType + ": " + fullMs + "ms - " + h + ":" + m + ":" + s + "." + ms;
                Logger.trace(TAG, log);
                Globals.getInstance().getToaster().showToast(log);
            });
        });

    }

    /**
     * Экспортирует текущие общие настройки в файл
     */
    private void exportCommonSettings() {
        Logger.trace(TAG, "exportCommonSettings()");
        File file = new File(Exchange.DIR + "/CommonSettings.bin");
        file.getParentFile().mkdirs();
        file.delete();

        try {
            if (file.createNewFile())
                CommonSettingsUtils.saveCommonSettingsToXmlFile(Dagger.appComponent().commonSettingsStorage().get(), file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        FileUtils.makeFileVisibleMtp(Globals.getInstance(), file);
    }

    /**
     * Импортирует текущие общие настройки из файла.
     */
    private void importCommonSettings() {
        Logger.trace(TAG, "importCommonSettings()");

        final Thread thread = new Thread(() -> {
            try {
                final File settingsFile = new File(Exchange.DIR + "/CommonSettings.bin");
                if (settingsFile.exists()) {
                    final CommonSettings commonSettings = CommonSettingsUtils.loadCommonSettingsFromXmlFile(settingsFile);
                    if (commonSettings != null) {
                        applySettings(commonSettings);
                    }
                }
            } catch (Exception ex) {
                Logger.error(TAG, ex);
            }
        });
        thread.start();
    }

    private void refreshCommonSettingsLastUpdate() {
        Date lastCommonSettingsUpdate = SharedPreferencesUtils.getCommonSettingsLastUpdate(Globals.getInstance());
        if (lastCommonSettingsUpdate != null) {
            String lastCommonSettingsUpdateText = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastCommonSettingsUpdate.getTime());
            commonSettingsLastUpdateTextView.setText(lastCommonSettingsUpdateText);
        } else {
            commonSettingsLastUpdateTextView.setText("");
        }
    }

    /**
     * Метод в котором осуществляется обновление настроек.
     *
     * @param settings настройки для обновления.
     */
    private void applySettings(@NonNull final CommonSettings settings) {
        synchronized (LOCK) {
            final Globals globals = Globals.getInstance();
            Dagger.appComponent().commonSettingsStorage().update(settings);

            runOnUiThread(() -> {
                final ScreenLockManager screenLockManager = globals.getScreenLockManager();
                screenLockManager.setLockDelay(settings.getAutoBlockingTimeout());
                screenLockManager.reset();
                refreshCommonSettingsLastUpdate();
            });
        }
    }

    /**
     * Копирует из reader в writer.
     *
     * @param is поток из которого будет производиться чтение.
     * @param os поток в который будет производиться запись.
     * @throws IOException
     */
    private void copy(@NonNull final InputStream is, @NonNull final OutputStream os) throws IOException {
        final byte[] buffer = new byte[1024];
        int bytesRead = 0;

        while ((bytesRead = is.read(buffer, 0, buffer.length)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
    }
}
