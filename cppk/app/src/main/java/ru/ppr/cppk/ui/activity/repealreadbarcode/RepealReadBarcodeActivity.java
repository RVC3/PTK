package ru.ppr.cppk.ui.activity.repealreadbarcode;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import ru.ppr.cppk.R;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.repeal.RepealBSCReadErrorActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.base.readBarcode.BaseReadBarcodeActivity;
import ru.ppr.cppk.ui.activity.base.readBarcode.ReadBarcodeResult;
import ru.ppr.cppk.ui.fragment.readPd.ErrorReadPdFragment;

/**
 * @author Dmitry Nevolin
 */
public class RepealReadBarcodeActivity extends BaseReadBarcodeActivity {

    @NonNull
    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, RepealReadBarcodeActivity.class);
    }

    @Override
    protected void onBarcodeResultRead(@NonNull ReadBarcodeResult readBarcodeResult) {
        if (readBarcodeResult.getPdList() != null) {
            //если вернулся хотя бы 1 билет, запускаем активити для аннулирвоания Пд
            if (!readBarcodeResult.getPdList().isEmpty()) {
                Navigator.navigateToRepealFinishActivity(this, readBarcodeResult.getPdList(), 0);
            } else {
                startActivity(new Intent(this, RepealBSCReadErrorActivity.class));
            }
        }
    }

    @Override
    protected void onReadError() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = ErrorReadPdFragment.newFragment(ReaderType.TYPE_BARCODE, false);
        fragmentTransaction.replace(R.id.read_pd_fragment_container, fragment).commit();
    }

}
