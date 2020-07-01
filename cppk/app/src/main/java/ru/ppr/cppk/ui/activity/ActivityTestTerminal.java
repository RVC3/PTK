package ru.ppr.cppk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.model.BluetoothDevice;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;

/**
 * Created by Dmitry Nevolin on 14.12.2015.
 */
public class ActivityTestTerminal extends Activity implements OnClickListener {

    private static final String TAG = Logger.makeLogTag(ActivityTestTerminal.class);

    private static final int REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE = 101;

    private EditText log;
    private EditText mac_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_terminal);

        log = (EditText) findViewById(R.id.log);
        mac_address = (EditText) findViewById(R.id.mac_address);

        findViewById(R.id.clear_log).setOnClickListener(this);
        findViewById(R.id.open_bluetooth_system_settings).setOnClickListener(this);
        findViewById(R.id.start_search_activity).setOnClickListener(this);
        findViewById(R.id.open_terminal_day).setOnClickListener(this);
        findViewById(R.id.close_terminal_day).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_log:
                log.setText("");

                break;
            case R.id.open_bluetooth_system_settings:
                startActivity(new Intent().setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));

                break;
            case R.id.start_search_activity:
                Navigator.navigateToBluetoothDeviceSearchActivity(this, null, REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE, new BluetoothDevice(SharedPreferencesUtils.getPosMacAddress(this), null));

                break;

            case R.id.open_terminal_day:
                Globals.getInstance().getPosManager().dayStart(new PosManager.AbstractTransactionListener() {
                    @Override
                    public void onConnectionTimeout() {
                        runOnUiThread(() -> log.setText("onConnectionTimeout"));
                    }

                    @Override
                    public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                        runOnUiThread(() -> log.setText("onResult"));
                    }
                });

                break;

            case R.id.close_terminal_day:
                Globals.getInstance().getPosManager().dayEnd(new PosManager.AbstractTransactionListener() {
                    @Override
                    public void onConnectionTimeout() {
                        runOnUiThread(() -> log.setText("onConnectionTimeout"));
                    }

                    @Override
                    public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                        runOnUiThread(() -> log.setText("onResult"));
                    }
                });

                break;
        }
    }

    @Override
    protected void onResume() {
        log.setText("");
        log.clearFocus();

        final String macAddress = SharedPreferencesUtils.getPosMacAddress(this);
        mac_address.setText(macAddress);

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SEARCH_BLUETOOTH_DEVICE: {
                    BluetoothDevice bluetoothDevice = data.getParcelableExtra(BluetoothDeviceSearchActivity.EXTRA_SELECTED_DEVICE);
                    SharedPreferencesUtils.setPosMacAddress(this, bluetoothDevice.getAddress());
                    Globals.updatePosTerminalWithoutCallback(this, SharedPreferencesUtils.getPosMacAddress(this), SharedPreferencesUtils.getPosPort(this), SharedPreferencesUtils.getPosTerminalType(this));
                    break;
                }
                default: {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
