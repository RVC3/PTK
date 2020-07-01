package ru.ppr.cppk.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.model.BluetoothDevice;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;
import ru.ppr.logger.Logger;
import ru.ppr.utils.ObjectUtils;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

public class BluetoothDeviceSearchActivity extends SystemBarActivity {

    // EXTRAS
    private static final String EXTRA_CURRENT_DEVICE = "EXTRA_CURRENT_DEVICE";
    public static final String EXTRA_SELECTED_DEVICE = "EXTRA_SELECTED_DEVICE";

    public static Intent getCallingIntent(Context context, BluetoothDevice currentDevice) {
        Intent intent = new Intent(context, BluetoothDeviceSearchActivity.class);
        intent.putExtra(EXTRA_CURRENT_DEVICE, currentDevice);
        return intent;
    }

    private BluetoothAdapter mBtAdapter;
    private BluetoothDeviceAdapter mNewBluetoothDeviceAdapter;
    private BluetoothDeviceAdapter mPairedBluetoothDeviceAdapter;

    private Button button_scan;
    private FeedbackProgressDialog progressDialog;

    private IBluetoothManager IBluetoothManager;

    private BluetoothDevice bondingDevice;
    private BluetoothDevice selectedDevice;

    private boolean receiversAreRegistered = false;


    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_bluetooth_device_search);

        // см. http://agile.srvdev.ru/browse/CPPKPP-34713
        resetRegisterReceiver();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            selectedDevice = extras.getParcelable(EXTRA_CURRENT_DEVICE);
        }

        button_scan = (Button) findViewById(R.id.button_scan);
        button_scan.setOnClickListener(paramAnonymousView -> {
            button_scan.setText(R.string.scanning);
            button_scan.setEnabled(false);

            doDiscovery();
        });

        mPairedBluetoothDeviceAdapter = new BluetoothDeviceAdapter(this);
        mNewBluetoothDeviceAdapter = new BluetoothDeviceAdapter(this);

        ListView paired_devices = (ListView) findViewById(R.id.paired_devices);
        paired_devices.setAdapter(mPairedBluetoothDeviceAdapter);
        paired_devices.setOnItemClickListener(mDeviceClickListener);

        ListView new_devices = (ListView) findViewById(R.id.new_devices);
        new_devices.setAdapter(mNewBluetoothDeviceAdapter);
        new_devices.setOnItemClickListener(mDeviceClickListener);

        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setCancelable(false);

        IBluetoothManager = Di.INSTANCE.bluetoothManager();

        if (!IBluetoothManager.isEnabled()) {
            progressDialog.show();
            Single.fromCallable(() -> IBluetoothManager.enableBluetoothForExternalDevice(this))
                    .subscribeOn(SchedulersCPPK.background())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(enabled -> {
                        progressDialog.dismiss();
                        if (enabled) {
                            reloadBondedDevices();
                        }
                    })
                    .doOnError(throwable -> progressDialog.dismiss())
                    .subscribe();
        } else {
            // Ожидается, что метод выполнится быстро.
            // Нужен для того, чтобы IBluetoothManager знал, что Bluetooth кому-то нужен
            IBluetoothManager.enableBluetoothForExternalDevice(this);
            reloadBondedDevices();
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (view.isEnabled()) {
                mBtAdapter.cancelDiscovery();
                if (parent.getId() == R.id.new_devices) {
                    BluetoothDevice bluetoothDevice = mNewBluetoothDeviceAdapter.getItem(position);
                    bondingDevice = bluetoothDevice;
                    Di.INSTANCE.bluetoothManager().pair(mBtAdapter.getRemoteDevice(bluetoothDevice.getAddress()));
                } else {
                    setSelectedDevice(mPairedBluetoothDeviceAdapter.getItem(position));
                }
            }
        }
    };

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Logger.trace("BroadcastReceiver", "BroadcastReceiver " + action);
            if (android.bluetooth.BluetoothDevice.ACTION_FOUND.equals(action)) {
                android.bluetooth.BluetoothDevice androidBluetoothDevice = intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE);
                Logger.trace("BroadcastReceiver", "BroadcastReceiver FOUND " + androidBluetoothDevice.getName());

                if (androidBluetoothDevice.getBondState() != android.bluetooth.BluetoothDevice.BOND_BONDED) {
                    BluetoothDevice bluetoothDevice = new BluetoothDevice(androidBluetoothDevice.getAddress(), androidBluetoothDevice.getName());
                    if (!mNewBluetoothDeviceAdapter.getItems().contains(bluetoothDevice))
                        mNewBluetoothDeviceAdapter.appendItem(bluetoothDevice);
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                onDiscoveryCanceled();
            }
        }
    };

    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE, android.bluetooth.BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(android.bluetooth.BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, android.bluetooth.BluetoothDevice.ERROR);

                android.bluetooth.BluetoothDevice androidBluetoothDevice = intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE);

                if (state == android.bluetooth.BluetoothDevice.BOND_BONDED && prevState == android.bluetooth.BluetoothDevice.BOND_BONDING) {
                    BluetoothDevice bluetoothDevice = new BluetoothDevice(androidBluetoothDevice.getAddress(), androidBluetoothDevice.getName());
                    reloadBondedDevices();
                    if (ObjectUtils.equals(bondingDevice, bluetoothDevice)) {
                        int indexOfBondingDevice = mNewBluetoothDeviceAdapter.getItems().indexOf(bondingDevice);
                        if (indexOfBondingDevice >= 0) {
                            mNewBluetoothDeviceAdapter.removeItem(indexOfBondingDevice);
                        }
                        // https://aj.srvdev.ru/browse/CPPKPP-29708
                        // Не будем автоматически уведомлять о том, что устройство выбрано.
                        // Например, Ingenico работает в разных режимах при сопряжении и при работе.
                        // Если тот, кто просил от нас сейчас Ingenico найти сразу начнет с ней работать, просто не сможет к ней подключиться.
                        // Поэтому, пускай пользователь сам выберет после этого устройство из списка уже сопреженных, когда переведет устройство в нужный режим.
                        //setSelectedDevice(bondingDevice);
                        bondingDevice = null;
                    }
                }
            }
        }
    };

    private void doDiscovery() {
        Logger.trace("DeviceListActivity", "doDiscovery()");

        if (this.mBtAdapter.isDiscovering())
            this.mBtAdapter.cancelDiscovery();

        this.mBtAdapter.startDiscovery();
    }

    private void reloadBondedDevices() {

        Set<android.bluetooth.BluetoothDevice> bluetoothDeviceSet = this.mBtAdapter.getBondedDevices();

        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
        for (android.bluetooth.BluetoothDevice androidBluetoothDevice : bluetoothDeviceSet) {
            BluetoothDevice bluetoothDevice = new BluetoothDevice(androidBluetoothDevice.getAddress(), androidBluetoothDevice.getName());
            if (!bluetoothDevices.contains(bluetoothDevice))
                bluetoothDevices.add(bluetoothDevice);
        }
        this.mPairedBluetoothDeviceAdapter.setItems(bluetoothDevices);
    }

    private void setSelectedDevice(BluetoothDevice bluetoothDevice) {
        selectedDevice = bluetoothDevice;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SELECTED_DEVICE, bluetoothDevice);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onDiscoveryCanceled() {
        button_scan.setText(R.string.button_scan);
        button_scan.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!receiversAreRegistered) {
            IntentFilter localIntentFilter1 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND);
            registerReceiver(discoveryReceiver, localIntentFilter1);
            IntentFilter localIntentFilter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(discoveryReceiver, localIntentFilter2);
            IntentFilter localIntentFilter3 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(pairReceiver, localIntentFilter3);

            receiversAreRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (progressDialog != null)
            progressDialog.dismiss();

        if (mBtAdapter != null)
            mBtAdapter.cancelDiscovery();

        onDiscoveryCanceled();

        if (receiversAreRegistered) {
            unregisterReceiver(discoveryReceiver);
            unregisterReceiver(pairReceiver);
            receiversAreRegistered = false;
        }

    }

    @Override
    protected void onDestroy() {
        IBluetoothManager.disableBluetoothForExternalDevice(this, false);
        super.onDestroy();
    }

    private class BluetoothDeviceAdapter extends BaseAdapter<BluetoothDevice> {

        private final LayoutInflater layoutInflater;

        BluetoothDeviceAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            TextView content = (TextView) convertView;
            BluetoothDevice bluetoothDevice = getItem(position);

            boolean isEnabled = true;

            if (selectedDevice != null) {
                //если старое устройство и новое имеют один и тот же адрес, запретим его выбор
                if (selectedDevice.getAddress() != null && TextUtils.equals(selectedDevice.getAddress(), bluetoothDevice.getAddress())) {
                    isEnabled = false;
                    //если старое устройство и новое имеют одно и тоже имя, отличное от null - запретим его выбор
                } else if (bluetoothDevice.getName() != null && TextUtils.equals(selectedDevice.getName(), bluetoothDevice.getName())) {
                    isEnabled = false;
                }
            }

            String bluetoothName = TextUtils.isEmpty(bluetoothDevice.getName()) ? getString(R.string.device_collection_item_empty_name) : bluetoothDevice.getName();

            content.setText(bluetoothName + "\n" + bluetoothDevice.getAddress());
            content.setEnabled(isEnabled);

            return convertView;
        }
    }
}
