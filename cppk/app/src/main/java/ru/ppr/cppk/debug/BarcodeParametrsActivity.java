package ru.ppr.cppk.debug;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.androidquery.AQuery;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.logger.Logger;
import rx.android.schedulers.AndroidSchedulers;

public class BarcodeParametrsActivity extends LoggedActivity {

    /* default parametrs */
    private int horizOffset = 300;
    private int resHoriz = 200;
    private int resVertic = 200;
    private int maxHeightPrintBlock = 60;
    private int startPosHoriz = 10;
    private int startPosVertic = 1;
    private int widthLittleElem = 2;
    private int heightLittleElem = 6;
    private int columnCount = 3;
    private int securLevel = 2;

    private TextView horizOffsetTextView;
    private TextView resHorizTextView;
    private TextView resVerticTextView;
    private TextView maxHeightPrintBlockTextView;
    private TextView startPosHorizTextView;
    private TextView startPosVerticTextView;
    private TextView widthLittleElemTextView;
    private TextView heightLittleElemTextView;
    private TextView columnCountTextView;
    private TextView securLevelTextView;

    private static final String pdData = "011000014ca18154805e03000000290000c0704e";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_barcode_parametrs);
        AQuery aQuery = new AQuery(this);
        setupView(aQuery);
    }

    private void setupView(AQuery aQuery) {
        //устанавливаем стандартные параметры
        horizOffsetTextView = aQuery.id(R.id.barcode_parametrs_horizontal_offset).text(String.valueOf(horizOffset)).getTextView();
        resHorizTextView = aQuery.id(R.id.barcode_parametrs_horizontal_rezolution_offset).text(String.valueOf(resHoriz)).getTextView();
        resVerticTextView = aQuery.id(R.id.barcode_parametrs_vertical_rezolution).text(String.valueOf(resVertic)).getTextView();
        maxHeightPrintBlockTextView = aQuery.id(R.id.barcode_parametrs_max_height_print_block).text(String.valueOf(maxHeightPrintBlock)).getTextView();
        startPosHorizTextView = aQuery.id(R.id.barcode_parametrs_start_print_position_horizontal).text(String.valueOf(startPosHoriz)).getTextView();
        startPosVerticTextView = aQuery.id(R.id.barcode_parametrs_start_print_pos_vertick).text(String.valueOf(startPosVertic)).getTextView();
        widthLittleElemTextView = aQuery.id(R.id.barcode_parametrs_width_little_elem).text(String.valueOf(widthLittleElem)).getTextView();
        heightLittleElemTextView = aQuery.id(R.id.barcode_parametrs_height_little_elem).text(String.valueOf(heightLittleElem)).getTextView();
        columnCountTextView = aQuery.id(R.id.barcode_parametrs_column_count).text(String.valueOf(columnCount)).getTextView();
        securLevelTextView = aQuery.id(R.id.barcode_parametrs_security_level).text(String.valueOf(securLevel)).getTextView();
        //слушатель для кнопки печати
        aQuery.id(R.id.barcode_parametrs_print_test_pd).clicked(this, "printBarcode");
    }

    public void printBarcode(View view) {
        int horizOffset = Integer.valueOf(horizOffsetTextView.getText().toString());
        int resHoriz = Integer.valueOf(resHorizTextView.getText().toString());
        int resVertic = Integer.valueOf(resVerticTextView.getText().toString());
        int maxHeightPrintBlock = Integer.valueOf(maxHeightPrintBlockTextView.getText().toString());
        int startPosHoriz = Integer.valueOf(startPosHorizTextView.getText().toString());
        int startPosVertic = Integer.valueOf(startPosVerticTextView.getText().toString());
        int widthLittleElem = Integer.valueOf(widthLittleElemTextView.getText().toString());
        int heightLittleElem = Integer.valueOf(heightLittleElemTextView.getText().toString());
        int columnCount = Integer.valueOf(columnCountTextView.getText().toString());
        int securLevel = Integer.valueOf(securLevelTextView.getText().toString());

//		String dataPdString = Opos.decode(pdData);

        StringBuilder print = new StringBuilder();
        print.append("! ").append(horizOffset).append(" ").append(resHoriz).append(" ").append(resVertic).append(" ").append(maxHeightPrintBlock).append(" 1").append("\r\n");
        //print.append("B PDF-417 10 1 XD 2 YD 18 C 3 S 2\r\n");
        print.append("B PDF-417").append(" ").append(startPosHoriz).append(" ").append(startPosVertic).append(" XD ").append(widthLittleElem).append(" YD ").append(heightLittleElem)
                .append(" C ").append(columnCount).append(" S ").append(securLevel).append("\r\n");
        print.append(pdData).append("\r\n");
        print.append("ENDPDF\r\n");
        print.append("FORM\r\n");
        print.append("PRINT\r\n");

        try {
            String commandString = print.toString();
            Logger.info("BARCODE", commandString);
            if (commandString.length() > 256) {
                Globals.getInstance().getToaster().showToast("Длинна команды превышает 256");
                return;
            }

            byte[] barcodeData = commandString.getBytes("UTF-8");

            Di.INSTANCE.printerManager().getOperationFactory().getPrintBarcodeOperation(barcodeData)
                    .call()
                    //Т.к. нас не интересует результат выполнения печати штрихкода, то проглотим ошибку
                    .onErrorReturn(throwable -> null)
                    .subscribeOn(SchedulersCPPK.printer())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        } catch (Exception e) {
            e.printStackTrace();
            Globals.getInstance().getToaster().showToast("Ошибка печати(см. LogCat)");
        }
    }
}
