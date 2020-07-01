package ru.ppr.cppk.ui.activity.controlreadbarcode;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ru.ppr.cppk.R;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.base.readBarcode.BaseReadBarcodeActivity;
import ru.ppr.cppk.ui.activity.base.readBarcode.ReadBarcodeResult;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.fragment.readPd.ErrorReadPdFragment;

/**
 * @author Dmitry Nevolin
 */
public class ControlReadBarcodeActivity extends BaseReadBarcodeActivity {

    private static final String EXTRA_READ_FOR_TRANSFER_PARAMS = "EXTRA_READ_FOR_TRANSFER_PARAMS";

    /**
     * Данные для оформления трансфера по считанному ПД
     */
    private ReadForTransferParams readForTransferParams;

    @NonNull
    public static Intent getCallingIntent(@NonNull Context context, @Nullable ReadForTransferParams readForTransferParams) {
        Intent intent = new Intent(context, ControlReadBarcodeActivity.class);
        intent.putExtra(EXTRA_READ_FOR_TRANSFER_PARAMS, readForTransferParams);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        readForTransferParams = getIntent().getParcelableExtra(EXTRA_READ_FOR_TRANSFER_PARAMS);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onBarcodeResultRead(@NonNull ReadBarcodeResult readBarcodeResult) {
        if (readBarcodeResult.getPdList() != null) {
            Navigator.navigateToResultBarcodeActivity(this, new ArrayList<>(readBarcodeResult.getPdList()), readForTransferParams);
        } else if (readBarcodeResult.getCouponReadEventId() != null) {
            Navigator.navigateToResultBarcodeCouponActivity(this, readBarcodeResult.getCouponReadEventId());
        } else {
            throw new IllegalStateException("pdList == null && couponReadEventId == null");
        }
    }

    @Override
    protected void onReadError() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = ErrorReadPdFragment.newFragment(ReaderType.TYPE_BARCODE, true);
        fragmentTransaction.replace(R.id.read_pd_fragment_container, fragment).commit();
    }

}
