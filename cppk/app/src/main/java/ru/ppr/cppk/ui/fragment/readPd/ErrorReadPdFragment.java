package ru.ppr.cppk.ui.fragment.readPd;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.listeners.SellNewPdOnClickListener;
import ru.ppr.cppk.pd.utils.reader.OnRepeatRead;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PermissionDvc;

import static ru.ppr.cppk.systembar.SystemBarActivity.DIALOG_ATTENTION_CLOSE_SHIFT;

public class ErrorReadPdFragment extends FragmentParent {

    private static final String TAG = ErrorReadPdFragment.class.getSimpleName();

    private static final String WITH_SELL_PD = "WITH_SELL_PD";
    private static final String WHERE_PRINT = "WHERE_PRINT";

    private OnRepeatRead repeatListener = null;
    private ReaderType wherePrint = null;
    private boolean withSellPd = true;

    public static ErrorReadPdFragment newFragment(ReaderType readerType, boolean withSellPd) {

        ErrorReadPdFragment fragment = new ErrorReadPdFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(WHERE_PRINT, readerType);
        bundle.putBoolean(WITH_SELL_PD, withSellPd);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        wherePrint = (ReaderType) bundle.getSerializable(WHERE_PRINT);
        withSellPd = bundle.getBoolean(WITH_SELL_PD, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.read_pd_is_error, container, false);

        Button repeatBtn = (Button) view.findViewById(R.id.btnAgain);
        repeatBtn.setOnClickListener(v -> onRepeatBtnClicked());

        /**
         * Показываем подсказку при неверном считывании
         */
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(R.string.barcode_howto),
                getString(R.string.Yes),
                null,
                LinearLayout.HORIZONTAL,
                0);

        //simpleDialog.setCancelable(false);
        simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> {
            dialog.dismiss();
        });
        simpleDialog.setOnCancelListener(dialogInterface -> {
            dialogInterface.dismiss();
        });
        Button howtoButton = (Button)view.findViewById(R.id.btnWhyNotRead);
        howtoButton.setVisibility(View.VISIBLE);
        howtoButton.setOnClickListener(v ->
                simpleDialog.show(getFragmentManager(), DIALOG_ATTENTION_CLOSE_SHIFT));

        Button salePdBtn = (Button) view.findViewById(R.id.sale_pd);
        salePdBtn.setOnClickListener(new SellNewPdOnClickListener(getActivity()));
        salePdBtn.setVisibility(withSellPd
                && Dagger.appComponent().privateSettings().isSaleEnabled()
                && Dagger.appComponent().permissionChecker().checkPermission(PermissionDvc.SalePd)
                ? View.VISIBLE : View.GONE);

        String errorString = createErrorMessage();
        Logger.trace(TAG, errorString);

        TextView messageView = (TextView) view.findViewById(R.id.read_pd_is_error_error_message);
        messageView.setText(errorString);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            repeatListener = (OnRepeatRead) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.toString() + " must implement ");
        }
    }

    private String createErrorMessage() {
        switch (wherePrint) {
            case TYPE_BSC:
                return getString(R.string.fail_read_from_bsc);
            case TYPE_BARCODE:
                return getString(R.string.fail_read_frim_barcode);
            default:
                return "";
        }
    }

    private void onRepeatBtnClicked() {
        if (repeatListener != null)
            repeatListener.repeatRead();
    }
}
