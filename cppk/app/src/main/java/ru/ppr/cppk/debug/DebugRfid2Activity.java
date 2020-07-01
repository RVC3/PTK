package ru.ppr.cppk.debug;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTask;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareUltralightReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsUltralightCReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.CppkNumberOfTripsUltralightEv1OnePdReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.domain.model.RfidType;
import ru.ppr.core.helper.Toaster;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.ui.adapter.base.BaseAdapter;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.CardData;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.RfidResult;
import ru.ppr.rfid.WriteToCardResult;
import ru.ppr.utils.CommonUtils;

/**
 * Всякие тестовые функции для работы с БСК
 *
 * @author G.Kashka
 */
public class DebugRfid2Activity extends LoggedActivity implements OnClickListener {

    public static final String TAG = Logger.makeLogTag(DebugRfid2Activity.class);

    private FeedbackProgressDialog progressDialog;
    private EditText et;
    private EditText sectorNumberEditText;
    private IRfid iRfid;
    private Toaster toaster;

    byte[] passagemarkRawVersionFive = new byte[]{0x05, 0x05, 0x00, 0x7D,
            (byte) 0xC8, (byte) 0x98, (byte) 0xCD, 0x54,
            (byte) 0xc2, (byte) 0xa0, (byte) 0x6e, (byte) 0xff,
            0x40, (byte) 0xc6, (byte) 0xA8, 00};

    byte[] passagemarkRawVersionFour = new byte[]{0x04, 0x01, 0x00, 0x7D,
            (byte) 0xC8, (byte) 0x98, (byte) 0xCD, 0x54,
            (byte) 0xc2, (byte) 0xa0, (byte) 0x6e, (byte) 0xff,
            0x40, (byte) 0xc6, (byte) 0xA8, 00};
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_rfid);

        iRfid = Dagger.appComponent().rfid();
        toaster = Dagger.appComponent().toaster();

        et = (EditText) findViewById(R.id.out);
        sectorNumberEditText = (EditText) findViewById(R.id.sectorNumber);
        findViewById(R.id.clearCPPK).setOnClickListener(this);
        findViewById(R.id.clearETT).setOnClickListener(this);
        findViewById(R.id.clearSKM).setOnClickListener(this);
        findViewById(R.id.clearTRK).setOnClickListener(this);
        findViewById(R.id.clearSTR).setOnClickListener(this);
        findViewById(R.id.clearETT).setOnClickListener(this);
        findViewById(R.id.clearEcpFromSkm).setOnClickListener(this);
        findViewById(R.id.clearUltralight).setOnClickListener(this);
        findViewById(R.id.readCard).setOnClickListener(this);
        findViewById(R.id.writePD).setOnClickListener(this);
        findViewById(R.id.startTestRfid).setOnClickListener(this);
        findViewById(R.id.writePassageMarkVersionFive).setOnClickListener(this);
        findViewById(R.id.writePassageMarkVersionFour).setOnClickListener(this);
        findViewById(R.id.clearLog).setOnClickListener(this);
        findViewById(R.id.testCardType).setOnClickListener(this);
        findViewById(R.id.clearSectorBtn).setOnClickListener(this);

        Spinner spinner = (Spinner) findViewById(R.id.readerType);
        RfidTypeAdapter adapter = new RfidTypeAdapter();
        adapter.setItems(new ArrayList<>(Arrays.asList(RfidType.values())));
        spinner.setAdapter(adapter);
        int selectedPosition = adapter.getPosition(SharedPreferencesUtils.getRfidType(this));
        spinner.setSelection(selectedPosition);
        spinner.setOnItemSelectedListener(rfidItemSelectedListener);

        progressDialog = new FeedbackProgressDialog(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearEcpFromSkm: {
                clearEcpFromSkm(v);
                break;
            }
            case R.id.clearSectorBtn:
                clearSector();
                break;
            case R.id.clearCPPK:
                clearCPPK();
                break;
            case R.id.clearETT:
                clearETT();
                break;
            case R.id.clearSKM:
                clearSCM();
                break;
            case R.id.clearTRK:
                clearTRK();
                break;
            case R.id.clearSTR:
                clearSTR();
                break;
            case R.id.clearUltralight:
                clearUltralight();
                break;
            case R.id.writePD:
                writePD();
                break;
            case R.id.readCard:
                readCard(SharedPreferencesUtils.isSamModuleUseEnable(getApplicationContext()));
                break;
            case R.id.startTestRfid:
                startActivity(new Intent(getApplicationContext(), RfidTest.class));
                break;

            case R.id.writePassageMarkVersionFive:
                writePassageMark(5);
                break;

            case R.id.writePassageMarkVersionFour:
                writePassageMark(4);
                break;

            case R.id.clearLog:
                et.setText("");
                break;
            case R.id.testCardType:
                startGetMifareCardTypeTest();
                break;

            default:
                break;
        }

    }

    /**
     * Запускает тест получения физического типа карты
     */
    private void startGetMifareCardTypeTest() {
        for (int i = 0; i < 10; i++)
            getMifareCardType();
    }

    private void getMifareCardType() {
        iRfid.clearAuthData();
        CardData res = iRfid.getRfidAtr();
        StringBuilder sb = new StringBuilder((res != null) ? "Успешно" : "Ошибка");
        if (res != null) {
            sb.append("\n").append("   UID = ").append(CommonUtils.bytesToHexWithSpaces(res.getCardUID()));
            sb.append("\n").append("   ATR = ").append(CommonUtils.bytesToHexWithSpaces(res.getRfidAttr()));
            sb.append("\n").append("   ATQA = ").append(CommonUtils.bytesToHexWithSpaces(res.getAtqa()));
            sb.append("\n").append("    SAK = ").append(CommonUtils.bytesToHexWithSpaces(res.getSak()));
            sb.append("\n").append("    COM = ").append(CommonUtils.bytesToHexWithSpaces(res.getCom()));
            sb.append("\n").append("     UI = ").append(res.getMifareCardType());
        }
        addLog(sb.toString());
    }

    private void readCard(boolean useSamNxp) {
        for (int sector = 0; sector < 16; sector++) {
            for (int block = 0; block < 3; block++) {

                RfidResult<byte[]> data = iRfid.readFromClassic((byte) sector, (byte) block, Dagger.appComponent().samAuthorizationStrategyFactory().create(), useSamNxp);
                if (data.isOk()) {
                    addLog("[" + sector + ", " + block + "] " + CommonUtils.bytesToHexWithoutSpaces(data.getResult()));
                } else {
                    addLog("Error read data from classick - " + data.getErrorMessage());
                }
            }
        }

    }

    private void clearSCM() {
        WriteToCardResult res = clearCardSector(17);
        if (res.isOk())
            res = clearCardSector(18);

        toaster.showToast(((res.isOk()) ? "Успешно" : "Неудача - " + res.toString()));
    }

    private void clearSector() {
        int sector;
        try {
            sector = Integer.valueOf(sectorNumberEditText.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Dagger.appComponent().toaster().showToast("Неудача - " + e.getMessage());
            return;
        }
        WriteToCardResult res = clearCardSector(sector);
        toaster.showToast(((res.isOk()) ? "Успешно" : "Неудача - " + res.toString()));
    }

    private void clearCPPK() {
        WriteToCardResult res = clearCardSector(7);
        if (res.isOk())
            res = clearCardSector(8);
        if (res.isOk())
            res = clearCardSector(9);
        toaster.showToast(((res.isOk()) ? "Успешно" : "Неудача - " + res.toString()));
    }

    private void clearTRK() {
        WriteToCardResult res = clearCardSector(6);
        if (res.isOk())
            res = clearCardSector(15);
        toaster.showToast(((res.isOk()) ? "Успешно" : "Неудача - " + res.toString()));
    }

    private void clearSTR() {
        WriteToCardResult res = clearCardSector(5);
        if (res.isOk())
            res = clearCardSector(6);
        toaster.showToast(((res.isOk()) ? "Успешно" : "Неудача - " + res.toString()));
    }

    private void clearETT() {
        WriteToCardResult res = clearCardSector(14);
        if (res.isOk())
            res = clearCardSector(15);
        toaster.showToast(((res.isOk()) ? "Успешно" : "Неудача - " + res.toString()));
    }

    /**
     * Стирает билет и метку прохода с ультралайта C и Ev1
     */
    private void clearUltralight() {

        boolean out = false;

        FindCardTask findCardTask = Dagger.appComponent().findCardTaskFactory().create();

        ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader cardReader = findCardTask.find();

        if (cardReader instanceof MifareUltralightReader) {
            MifareUltralightReader mifareUltralightReader = (MifareUltralightReader) cardReader;
            //стираем билет
            WriteCardResult writeCardResult = mifareUltralightReader.writeBytes(new byte[4 * 6], (byte) 8);
            if (writeCardResult.isSuccess()) {
                //на разных ультралайтах метка в разных местах
                if (cardReader instanceof CppkNumberOfTripsUltralightCReader) {
                    //стираем метку прохода
                    writeCardResult = mifareUltralightReader.writeBytes(new byte[4 * 4], (byte) 30);
                    out = writeCardResult.isSuccess();
                } else if (cardReader instanceof CppkNumberOfTripsUltralightEv1OnePdReader) {
                    //стираем метку прохода
                    writeCardResult = mifareUltralightReader.writeBytes(new byte[4 * 4], (byte) 36);
                    out = writeCardResult.isSuccess();
                }
            }
        } else {
            toaster.showToast(R.string.UlErrorCardType);
        }

        toaster.showToast(out ? "Успешно" : "Неудача");
    }

    public WriteToCardResult clearCardSector(int sector) {
        iRfid.clearAuthData();
        boolean useSamNxp = SharedPreferencesUtils.isSamModuleUseEnable(getApplicationContext());
        WriteToCardResult res = iRfid.writeToClassic(new byte[16 * 3], null, sector, 0, Dagger.appComponent().samAuthorizationStrategyFactory().create(), useSamNxp);
        addLog("Очистка сектора " + sector + " - " + ((res.isOk()) ? "Успешно" : "Неудача - " + res.toString()));
        return res;
    }

    public void writePD() {
        iRfid.clearAuthData();
        byte[] pdData = {5, 17, 0, 1, -115, -94, 83, 84, 95, 13, 3, 0, 0, 0, 19, 0, 0, 0, 10, 11, 12, 13, 14};
        boolean useSamNxp = SharedPreferencesUtils.isSamModuleUseEnable(getApplicationContext());
        WriteToCardResult result = iRfid.writeToClassic(pdData, null, 7, 0, Dagger.appComponent().samAuthorizationStrategyFactory().create(), useSamNxp);
        toaster.showToast(getErrorMessage(result, getApplicationContext()));
    }

    public static String getErrorMessage(WriteToCardResult result, Context context) {

        String error = null;

        switch (result) {
            case SUCCESS:
                error = context.getString(R.string.write_to_card_success);
                break;

            case UID_DOES_NOT_MATCH:
                error = context.getString(R.string.uid_does_not_mathc);
                break;

            case CAN_NOT_SEARCH_CARD:
                error = context.getString(R.string.can_not_search_card);
                break;

            case WRITE_ERROR:
                error = context.getString(R.string.write_error);
                break;

            case UNKNOWN_ERROR:
            default:
                error = context.getString(R.string.unknown_error);
                break;
        }

        return error;
    }

    private void clearEcpFromSkm(View v) {
        WriteToCardResult res = clearCardSector(18);
        toaster.showToast(((res.isOk()) ? "Успешно" : "Неудача - " + res.toString()));
    }

    private void addLog(String text) {
        et.setText(text + "\n" + et.getText());
        Logger.trace(DebugRfid2Activity.class.getSimpleName(), text);
    }

    private void writePassageMark(final int versionMark) {

        String message = "";

        switch (versionMark) {
            case 5:
                message = "Данная метка записывается на карту UltraLight.\n"
                        + "Убедитесь что вы поднесли карту Ultralight!";
                break;

            case 6:
                message = "Данная метка записывается на карту\n"
                        + "БСК ЦППК на период - Mifare Classic 1k.\n"
                        + "Убедитесь что вы поднесли необходимуюк карту!";
                break;

            default:
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ВНИМАНИЕ")
                .setMessage(message)
                .setPositiveButton("Продолжить", (dialog, which) -> {
                    new WriteAsynceTask().executeOnExecutor(SchedulersCPPK.backgroundExecutor(), versionMark);
                }).setNegativeButton("Отмена", null).create().show();
    }

    private class WriteAsynceTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("подождите. Идет запись");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {

            boolean result = false;

            FindCardTask findCardTask = Dagger.appComponent().findCardTaskFactory().create();

            ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader cardReader = findCardTask.find();

            if (cardReader instanceof WritePassageMarkReader) {
                WritePassageMarkReader writePassageMarkReader = (WritePassageMarkReader) cardReader;
                PassageMark passageMark = null;
                switch (params[0]) {
                    case 5:
                        passageMark = Dagger.appComponent()
                                .passageMarkDecoderFactory()
                                .create(passagemarkRawVersionFive)
                                .decode(passagemarkRawVersionFive);
                        break;

                    case 4:
                        passageMark = Dagger.appComponent()
                                .passageMarkDecoderFactory()
                                .create(passagemarkRawVersionFour)
                                .decode(passagemarkRawVersionFour);
                        break;

                    default:
                        break;
                }
                if (passageMark != null) {
                    WriteCardResult writeCardResult = writePassageMarkReader.writePassageMark(passageMark);
                    result = writeCardResult.isSuccess();
                }
            }
            Logger.info(TAG, "Write passage mark done with result -  " + result);

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result) {
                toaster.showToast("Готово");
            } else {
                toaster.showToast("Ошибка при записи");
            }
        }
    }

    private AdapterView.OnItemSelectedListener rfidItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            RfidType type = (RfidType) parent.getAdapter().getItem(position);
            RfidType currentType = SharedPreferencesUtils.getRfidType(getApplicationContext());
            if (!currentType.equals(type)) {
                SharedPreferencesUtils.setRfidType(getApplicationContext(), type);
                Dagger.appComponent().rfidManager().updateRfid(type);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private class RfidTypeAdapter extends BaseAdapter<RfidType> {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView != null) {
                textView = (TextView) convertView;
            } else {
                textView = (TextView) getLayoutInflater()
                        .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            textView.setText(getItem(position).name());
            return textView;
        }

        public int getPosition(RfidType type) {
            int i = 0;
            for (RfidType locaType : getItems()) {
                if (locaType.equals(type)) {
                    break;
                }
                i++;
            }
            return i;
        }
    }

}
