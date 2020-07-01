package ru.ppr.cppk.debug;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.androidquery.AQuery;

import java.util.Locale;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTask;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkEncoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.logger.Logger;
import ru.ppr.rfid.CardData;
import ru.ppr.rfid.IRfid;
import ru.ppr.rfid.RfidResult;
import ru.ppr.utils.CommonUtils;

public class DebugRfid1Activity extends LoggedActivity implements OnClickListener {

    private static final String TAG = Logger.makeLogTag(DebugRfid1Activity.class);
    private IRfid iRfid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_rfid_activity);
        AQuery aQuery = new AQuery(this);
        aQuery.id(R.id.readImage).clicked(this);
        aQuery.id(R.id.writeImage).clicked(this);
        aQuery.id(R.id.clearMarkUltralight).clicked(this);
        aQuery.id(R.id.setTrashToPassageMarkUltralight).clicked(this);
        aQuery.id(R.id.readPassageMarkFromUltralightC).clicked(this);
        aQuery.id(R.id.setPassageMarkV5ToUltralight).clicked(this);
        aQuery.id(R.id.readCard).clicked(this);
        aQuery.id(R.id.writeUlPd).clicked(this);
        iRfid = Dagger.appComponent().rfid();
    }

    /**
     * Считает образ карты в лог
     *
     * @param view
     */
    public void readImage(View view) {
        Globals.getInstance().getToaster().showToast("Not implemented");
//        byte[] data = CppkUtils.getDataFromCardImageFile();
//        if (data != null) {
//            addLog("image Found: " + MD5Utils.convertHashToString(data));
//            CardReader imageReader = new CardReaderMifareClassic(data);
//            for (int i = 0; i < 19; i++) {
//                Result<byte[]> dataToWrite = imageReader.readMoreThenOneBlock((byte) i, (byte) 0, (byte) 3);
//                if(!dataToWrite.isError()) {
//                    printSector(dataToWrite.getResult(), i);
//                } else {
//                    addLog(String.format(Locale.getDefault(),
//                            "Error read data from sector %1$d in card image - %2$s", i,
//                            dataToWrite.getTextError()));
//                }
//            }
//        } else {
//            addLog("образ не найден!");
//        }
    }

    /**
     * Запишет образ на карту
     *
     * @param view
     */
    public void writeImage(View view) {

        Globals.getInstance().getToaster().showToast("Not implemented");

//        addLog("Start write image in DebugRfid1Activity");
//
//        CardData cData = new CardData();
//        if (iRfid.getRfidAtr(cData)) {
//            byte[] data = CppkUtils.getDataFromCardImageFile();
//            if (data != null) {
//                String uidCard = CommonUtils.getHexString(cData.rfidAttr).replace(" ", "");
//                CardReader realReader = new CardReaderMifareClassic(iRfid, true);
//                CardReader imageReader = new CardReaderMifareClassic(data, true);
//                for (int i = 0; i < 19; i++) {
//                    addLog("Attempt read data from sector - " + i);
//                    Result<byte[]> dataToWrite = imageReader.readMoreThenOneBlock((byte) i, (byte) 0, (byte) 3);
//                    if(!dataToWrite.isError()) {
//                        Result<WriteToCardResult> res = realReader.writeDataToCard(dataToWrite.getResult(),
//                                uidCard, i * RfidReal.BLOCK_IN_THE_SECTOR);
//
//                        if(!res.isError()) {
//                            addLog(String.format(Locale.getDefault(), "Write data %1$s in sector %2$d success",
//                                    CommonUtils.byteArrayToString(dataToWrite.getResult()), i));
//                        } else {
//                            addLog(String.format(Locale.getDefault(), "Error write data to sector %1$s - %2$s",
//                                    CommonUtils.byteArrayToString(dataToWrite.getResult()),
//                                    WriteToCardResult.getErrorMessage(res.getResult(), getApplicationContext())));
//                        }
//                    } else {
//                        addLog("Error read data from card - " + dataToWrite.getTextError());
//                    }
//                }
//            } else {
//                addLog("Образ не найден!");
//            }
//        } else {
//            addLog("Карта не найдена!");
//        }
    }

    /**
     * очистит метку прохода на Ultralight карте
     */
    public void clearMarkUltralight(View view) {
        addLog("clearMarkUltralight() START");
        writePassageMarkToUl("05260011000000000000000000000000");
        addLog("clearMarkUltralight() FINISH");
    }

    /**
     * запишет мусор в метку прохода на Ultralight карте
     */
    public void setTrashToPassageMarkUltralight(View view) {
        addLog("setTrashToPassageMarkUltralight() START");
        writePassageMarkToUl("FFF6FFFFFFFFFFFF0000000000000000");
        addLog("setTrashToPassageMarkUltralight() FINISH");
    }

    /**
     * запишет метку 5й версии на Ultralight
     */
    public void setPassageMarkV5ToUltralight(View view) {
        addLog("setPassageMarkV5ToUltralight() START");
        writePassageMarkToUl("051C00FF0086841E03DD490200000000");
        addLog("setPassageMarkV5ToUltralight() FINISH");
    }

    /**
     * запишет байты в место для метки прохода
     *
     * @param passMarkBytes
     */
    private void writePassageMarkToUl(String passMarkBytes) {
        addLog("writePassageMarkToUl() START");

        byte[] markNew = CommonUtils.hexStringToByteArray(passMarkBytes);

        FindCardTask findCardTask = Dagger.appComponent().findCardTaskFactory().create();

        ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader cardReader = findCardTask.find();

        if (cardReader instanceof WritePassageMarkReader) {
            WritePassageMarkReader writePassageMarkReader = (WritePassageMarkReader) cardReader;
            PassageMarkDecoder passageMarkDecoder = Dagger.appComponent().passageMarkDecoderFactory().create(markNew);
            PassageMark passageMark = passageMarkDecoder.decode(markNew);
            WriteCardResult passageMarkWriteResult = writePassageMarkReader.writePassageMark(passageMark);
            if (passageMarkWriteResult.isSuccess()) {
                Globals.getInstance().getToaster().showToast(R.string.PassageMark_WriteOk);
            } else {
                Globals.getInstance().getToaster().showToast(R.string.PassageMark_ErrorWrite);
            }

            if (cardReader instanceof ReadPassageMarkReader) {
                ReadPassageMarkReader readPassageMarkReader = (ReadPassageMarkReader) cardReader;
                ReadCardResult<PassageMark> passageMarkReadResult = readPassageMarkReader.readPassageMark();
                if (passageMarkReadResult.isSuccess()) {
                    PassageMarkEncoder passageMarkEncoder = Dagger.appComponent().passageMarkEncoderFactory().create(passageMarkReadResult.getData());
                    byte[] data = passageMarkEncoder.encode(passageMarkReadResult.getData());
                    addLog("writePassageMarkToUl() Passage mark on card after write - " + CommonUtils.bytesToHexWithoutSpaces(data));
                } else {
                    addLog("writePassageMarkToUl() Error read passage mark from card - " + passageMarkReadResult.getDescription());
                    Globals.getInstance().getToaster().showToast(R.string.PassageMark_ErrorRead);
                }
            } else {
                Globals.getInstance().getToaster().showToast(R.string.PassageMark_ErrorCardType);
            }
        } else {
            Globals.getInstance().getToaster().showToast(R.string.PassageMark_ErrorCardType);
        }

        addLog("writePassageMarkToUl() FINISH");
    }

    /**
     * Прочитать метку прохода с Ultralight
     *
     * @param view
     */
    public void readPassageMarkFromUltralight(View view) {
        addLog("readPassageMarkFromUltralight() START");

        FindCardTask findCardTask = Dagger.appComponent().findCardTaskFactory().create();

        ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader cardReader = findCardTask.find();

        if (cardReader instanceof ReadPassageMarkReader) {
            ReadPassageMarkReader readPassageMarkReader = (ReadPassageMarkReader) cardReader;
            ReadCardResult<PassageMark> passageMarkResult = readPassageMarkReader.readPassageMark();
            if (passageMarkResult.isSuccess()) {
                PassageMarkEncoder passageMarkEncoder = Dagger.appComponent().passageMarkEncoderFactory().create(passageMarkResult.getData());
                byte[] data = passageMarkEncoder.encode(passageMarkResult.getData());
                Globals.getInstance().getToaster().showToast("Passage mark - " + CommonUtils.bytesToHexWithoutSpaces(data));
            } else {
                Globals.getInstance().getToaster().showToast(R.string.PassageMark_ErrorRead);
            }
        } else {
            Globals.getInstance().getToaster().showToast(R.string.PassageMark_ErrorCardType);
        }

        addLog("readPassageMarkFromUltralight() FINISH");
    }

    private void readCard() {
        FindCardTask findCardTask = Dagger.appComponent().findCardTaskFactory().create();

        ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader cardReader = findCardTask.find();

        if (cardReader instanceof MifareClassicReader) {
            MifareClassicReader mifareClassicReader = (MifareClassicReader) cardReader;
            for (int i = 0; i < 19; i++) {
                ReadCardResult<byte[]> readData = mifareClassicReader.readBlocks(i, 0, 3);
                if (readData.isSuccess()) {
                    printSector(readData.getData(), i);
                } else {
                    addLog(String.format(Locale.ROOT, "Error read data from sector %1$d, error %2$s ",
                            i, readData.getDescription()));
                }
            }

        } else {
            addLog("Карта не найдена!");
        }
    }

    private void printSector(byte[] data, int sector) {
        if (data != null && data.length == 48) {
            String s = CommonUtils.getHexString(data).replace(" ", "");
            StringBuffer sb = new StringBuffer();
            sb.append("данные сектора ").append(sector).append(": ");
            sb.append(s.substring(0, 32)).append(" ");
            sb.append(s.substring(32, 64)).append(" ");
            sb.append(s.substring(64, 96)).append(" ");
            addLog(sb.toString());
        } else {
            addLog("данные сектора " + sector + ": ошибка чтения!");
        }
    }

    private void addLog(String text) {
        Logger.trace(getClass(), text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.readImage: {
                readImage(v);
                break;
            }

            case R.id.writeImage: {
                writeImage(v);
                break;
            }

            case R.id.clearMarkUltralight: {
                clearMarkUltralight(v);
                break;
            }

            case R.id.setPassageMarkV5ToUltralight: {
                setPassageMarkV5ToUltralight(v);
                break;
            }

            case R.id.setTrashToPassageMarkUltralight: {
                setTrashToPassageMarkUltralight(v);
                break;
            }

            case R.id.readPassageMarkFromUltralightC: {
                readPassageMarkFromUltralight(v);
                break;
            }

            case R.id.writeUlPd: {
                writeUlPd(v);
                break;
            }

            case R.id.readCard: {
                readCard();
                break;
            }


            default:
                break;
        }
    }

    private void writeUlPd(View v) {
        addLog("Start write pd on ultralight");
        byte[] dataPd = CommonUtils.hexStringToByteArray("07150000804c0a565666000019005400c20100c000000000000000000000000000000000000000000000000000000000e600a4d6ce50aa59dada2772c0d8cb6d963367c13695ceb7f3b71756703a1c1c6bcf791f086fa3da040c51cfaa788f7bf3dfc607ae851e3ea524202f7cf652f9");
        CardData cardData = iRfid.getRfidAtr();
        RfidResult<byte[]> pd = iRfid.readFromUltralight((byte) 8, (byte) 0, (byte) 112);
        if (cardData != null && pd.isOk()) {
            addLog("PD + passage mark: " + CommonUtils.getHexString(pd.getResult()));
            iRfid.writeToUltralight(dataPd, cardData.getCardUID(), (byte) 8);
            final RfidResult<byte[]> dataAfterWrite = iRfid.readFromUltralight((byte) 8, (byte) 0, (byte) 112);
            if (dataAfterWrite.isOk()) {
                addLog("PD + passage mark: " + CommonUtils.getHexString(dataAfterWrite.getResult()));
            } else {
                addLog("Error read data after write from ultralight");
            }
        } else {
            addLog("Error read data from ultralight - " + pd.getErrorMessage());
        }

    }
}
