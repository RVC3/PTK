package ru.ppr.cppk.ui.fragment.pd.invalid;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.logger.Logger;
import ru.ppr.cppk.pd.utils.ValidityPdVariants;

public class ErrorFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(ErrorFragment.class);

    // ARGS
    private static final String ARG_ERROR = "ERROR_FRAGMENT_ERROR";
    private static final String ARG_VARIANTS = "ARG_VARIANTS";
    private static final String ARG_FROM_CONTROL = "ARG_FROM_CONTROL";

    public enum Errors {NO_TICKET_FOR_CANCEL, NO_TICKET, NO_TARIFF, INCORRECT_DEVICE_ID, OTHER}

    private Errors argError;
    private ValidityPdVariants argVariants;

    public static Fragment newInstance(
            ValidityPdVariants variants,
            Errors error,
            boolean fromControl) {
        Logger.trace(TAG, "Создаем фрагмент с ошибкой: " + error.toString());
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ERROR, error);
        bundle.putSerializable(ARG_VARIANTS, variants);
        bundle.putBoolean(ARG_FROM_CONTROL, fromControl);
        Fragment fragment = new ErrorFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            argError = (Errors) getArguments().getSerializable(ARG_ERROR);
            argVariants = (ValidityPdVariants) getArguments().getSerializable(ARG_VARIANTS);
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;

        switch (argVariants) {
            case TWO_PD_IS_VALID:
            case ONE_OF_TWO_PD_IS_VALID:
            case TWO_PD_IS_INVALID: {
                // если считали 2 билета, то нужно показать разделитель и высоту фрагмента
                view = inflater.inflate(R.layout.fragment_error_tariff_small, container, false);
                View separatorView = view.findViewById(R.id.one_off_bottom_separator);
                separatorView.setVisibility(View.VISIBLE);
                break;
            }
            default: {
                //если считали 1 билет, то разделитель между билетами ненужен
                view = inflater.inflate(R.layout.fragment_error_tariff, container, false);
                View separatorView = view.findViewById(R.id.one_off_bottom_separator);
                separatorView.setVisibility(View.GONE);
            }

        }

        TextView errorTextView = (TextView) view.findViewById(R.id.error_fragment_message);
        errorTextView.setText(getErrorMessage(argError));

        ImageView circleImageView = (ImageView) view.findViewById(R.id.circleImage);
        if (circleImageView != null) {
            circleImageView.setVisibility(View.VISIBLE);
        }

        TextView errorHeaderTextView = (TextView) view.findViewById(R.id.error_fragment_header);
        errorHeaderTextView.setVisibility(argError == Errors.NO_TARIFF ? View.VISIBLE : View.GONE);

        return view;
    }

    @NonNull
    private String getErrorMessage(Errors error) {
        String errorString;
        switch (error) {
            case NO_TICKET_FOR_CANCEL:
                errorString = getString(R.string.error_fragment_not_pd_for_cancel_not_found);
                break;

            case NO_TICKET:
                errorString = getString(R.string.error_fragment_not_pd_on_bsc);
                break;

            case NO_TARIFF:
                errorString = getString(R.string.error_fragment_tariff_not_found);
                break;

            case INCORRECT_DEVICE_ID:
                //http://agile.srvdev.ru/browse/CPPKPP-26809
                errorString = getString(R.string.error_fragment_invalid_device_id);
                break;

            default:
                errorString = getString(R.string.error_fragment_incorrect_data);
                break;
        }
        return errorString;
    }

}
