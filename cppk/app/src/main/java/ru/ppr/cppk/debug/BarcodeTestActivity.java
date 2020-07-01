package ru.ppr.cppk.debug;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import ru.ppr.barcode.IBarcodeReader;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;
import rx.Completable;

/**
 * Окно тестирования работоспособности сканера ШК
 */
public class BarcodeTestActivity extends LoggedActivity {

    private static final String TAG = Logger.makeLogTag(BarcodeTestActivity.class);

    private Button scanBtn;
    private EditText logView;
    private UiThread uiThread;

    private IBarcodeReader barcodeReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_barcode_test_activity);

        uiThread = Dagger.appComponent().uiThread();
        scanBtn = (Button) findViewById(R.id.startTestBarcode);
        logView = (EditText) findViewById(R.id.barcode_test_log);

        barcodeReader = Dagger.appComponent().barcodeReader();

        scanBtn.setOnClickListener(v -> {
            addLog((new Date()).toString());
            scanBtn.setEnabled(false);
            String[] model = new String[1];
            barcodeReader.getModel(model);
            addLog(model[0]);

            Completable
                    .fromAction(() -> {
                        byte[] data = barcodeReader.scan();
                        uiThread.post(() -> {
                            scanBtn.setEnabled(true);
                            addLog(CommonUtils.bytesToHex(data));
                            addLog("---------------------------");
                        });
                    })
                    .subscribeOn(SchedulersCPPK.barcode())
                    .subscribe();
        });

    }

    /**
     * Выводит данные в лог на экране
     *
     * @param text
     */
    private void addLog(String text) {
        StringBuilder sb = new StringBuilder(text).append("\n");
        sb.append(logView.getText());
        logView.setText(sb.toString());
        Logger.info(TAG, text);
    }

}
