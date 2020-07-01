package com.ingenico.pclutilities;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Результат декомпиляции библиотеки PclUtilities_1.4.jar
 * удалить этот модуль целиком, когда Инженика исправит проблему на своей стороне.
 *
 * @author Grigoriy Kashka
 */
public class PclUtilities {
    private static final String mVersionName = "1.2";
    private static final String TAG = "PCLUTIL_1.2";
    private Context mAppContext = null;
    private String mPackageName = null;
    private String mFileName = null;
    private BluetoothAdapter mBtAdapter;
    private static final String INGENICO_BT_IDENT_1 = "00:03:81";
    private static final String INGENICO_BT_IDENT_2 = "54:7F:54";
    private static final String INGENICO_BT_IDENT_3 = "00:7F:54";
    private static final String INGENICO_BT_IDENT_4 = "F8:43:60";
    private char[] mBtAddressRead;
    private String sbtAddressRead;

    public PclUtilities(Context paramContext, String paramString1, String paramString2) {
        this.mAppContext = paramContext;
        this.mPackageName = paramString1;
        this.mFileName = paramString2;
        this.mBtAddressRead = new char[17];
    }

    public Set<BluetoothCompanion> GetPairedCompanions() {
        HashSet localHashSet = new HashSet();
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.mBtAdapter == null) {
            return null;
        }
        if (!this.mBtAdapter.isEnabled()) {
            return null;
        }
        this.sbtAddressRead = getStoredAddress();
        Set localSet = this.mBtAdapter.getBondedDevices();
        if (localSet.size() > 0) {
            Iterator localIterator = localSet.iterator();
            while (localIterator.hasNext()) {
                BluetoothDevice localBluetoothDevice = (BluetoothDevice) localIterator.next();
                if ((localBluetoothDevice.getAddress().startsWith("00:03:81")) || (localBluetoothDevice.getAddress().startsWith("54:7F:54")) || (localBluetoothDevice.getAddress().startsWith("00:7F:54")) || (localBluetoothDevice.getAddress().startsWith("F8:43:60"))
                        // -------------------------------------------------------------------------------------
                        // Фикс проблемы невозможности использовать терминал с mac-ардесом: 54:E1:40:4F:69:EE
                        // http://agile.srvdev.ru/browse/CPPKPP-36862
                        || (localBluetoothDevice.getAddress().startsWith("54:E1:40"))
                        //-------------------------------------------------------------------------------------
                        ) {
                    BluetoothCompanion localBluetoothCompanion = new BluetoothCompanion(localBluetoothDevice);
                    if ((this.sbtAddressRead != null) && (localBluetoothDevice.getAddress().equals(this.sbtAddressRead))) {
                        localBluetoothCompanion.setActivated(true);
                    }
                    localHashSet.add(localBluetoothCompanion);
                }
            }
        }
        return localHashSet;
    }

    public int ActivateCompanion(String paramString) {
        Set localSet = GetPairedCompanions();
        Iterator localIterator = localSet.iterator();
        while (localIterator.hasNext()) {
            BluetoothCompanion localBluetoothCompanion = (BluetoothCompanion) localIterator.next();
            if (localBluetoothCompanion.mBtDevice.getAddress().equals(paramString)) {
                storeAddress(paramString);
                return 0;
            }
        }
        return 1;
    }

    private void storeAddress(String paramString) {
        try {
            FileOutputStream localFileOutputStream = this.mAppContext.createPackageContext(this.mPackageName, 0).openFileOutput(this.mFileName, 1);
            OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(localFileOutputStream);
            localOutputStreamWriter.write(paramString);
            localOutputStreamWriter.flush();
            localOutputStreamWriter.close();
        } catch (Exception localException) {
            System.out.println("IOException : " + localException);
        }
    }

    private String getStoredAddress() {
        System.out.println("getStoredAddress");
        String str;
        try {
            FileInputStream localFileInputStream = this.mAppContext.createPackageContext(this.mPackageName, 0).openFileInput(this.mFileName);
            InputStreamReader localInputStreamReader = new InputStreamReader(localFileInputStream);
            int i = 0;
            i = localInputStreamReader.read(this.mBtAddressRead);
            if (i == 17) {
                str = new String(this.mBtAddressRead);
                Log.d("PCLUTIL_1.2", String.format("BT ADR FOR PCL = %s", new Object[]{str}));
            } else {
                str = "";
            }
        } catch (Exception localException) {
            str = "";
            System.out.println("Exception : " + localException);
            localException.printStackTrace();
        }
        return str;
    }

    public class BluetoothCompanion {
        private BluetoothDevice mBtDevice;
        private boolean mActivated;

        protected BluetoothCompanion(BluetoothDevice paramBluetoothDevice) {
            this.mBtDevice = paramBluetoothDevice;
            this.mActivated = false;
        }

        protected void setActivated(boolean paramBoolean) {
            this.mActivated = paramBoolean;
        }

        public boolean isActivated() {
            return this.mActivated;
        }

        public BluetoothDevice getBluetoothDevice() {
            return this.mBtDevice;
        }
    }
}
