package ru.ppr.cppk.ui.fragment.pd.servicefee;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.base.MvpFragment;
import ru.ppr.cppk.ui.fragment.pd.simple.model.ServicePdViewModel;
import ru.ppr.cppk.ui.fragment.pd.zoom.ZoomPdFragment;

/**
 * @author Dmitry Nevolin
 */
public class ServiceFeePdFragment extends MvpFragment implements ServiceFeePdView {

    // Args
    private static final String ARG_PD = "ARG_PD";

    /**
     * @param pd билет
     */
    public static ServiceFeePdFragment newInstance(@NonNull PD pd) {
        ServiceFeePdFragment fragment = new ServiceFeePdFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_PD, pd);

        fragment.setArguments(args);

        return fragment;
    }

    // Dependencies
    private ServiceFeePdComponent component;
    private ServiceFeePdPresenter presenter;
    // Views
    private View serviceFeeInfo;
    private TextView pdTitle;
    private TextView pdValid;
    private TextView pdNumber;
    private TextView pdError;
    private TextView dateActionsFrom;
    private TextView dateActionsTo;
    private View serviceFeeNotFound;

    private Callback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // если тут что-то упало по NPE значит фрагмент криво создали.
        // Надо пользоваться ServiceFeePdFragment.newInstance(pd);
        component = DaggerServiceFeePdComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .pd(getArguments().getParcelable(ARG_PD))
                .build();

        super.onCreate(savedInstanceState);

        presenter = getMvpDelegate().getPresenter(component::serviceFeePdPresenter, ServiceFeePdPresenter.class);
        presenter.initialize();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_fee_pd, container, false);

        // Устанавливаем ClickListener для открытия окна с увеличенным шрифтом
        ((SystemBarActivity) getActivity()).addLockedView(view);
        view.setOnClickListener(v -> presenter.onZoomPdClicked());

        serviceFeeInfo = view.findViewById(R.id.service_fee_info);
        pdTitle = (TextView) view.findViewById(R.id.pd_title);
        pdValid = (TextView) view.findViewById(R.id.pd_valid);
        pdNumber = (TextView) view.findViewById(R.id.pd_number);
        pdError = (TextView) view.findViewById(R.id.pd_error);
        dateActionsFrom = (TextView) view.findViewById(R.id.date_actions_from);
        dateActionsTo = (TextView) view.findViewById(R.id.date_actions_to);
        dateActionsTo = (TextView) view.findViewById(R.id.date_actions_to);
        serviceFeeNotFound = view.findViewById(R.id.service_fee_not_found);

        return view;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void updatePdTitle(@Nullable String pdTitle) {
        this.pdTitle.setText(pdTitle == null ? "" : pdTitle);
    }

    @Override
    public void updatePdValid(boolean isPdValid) {
        String text;
        int color;

        if (isPdValid) {
            text = getString(R.string.service_fee_pd_valid_pd);
            color = ActivityCompat.getColor(getActivity(), R.color.green);
        } else {
            text = getString(R.string.service_fee_pd_invalid_pd);
            color = ActivityCompat.getColor(getActivity(), R.color.red);
        }

        this.pdValid.setText(text);
        this.pdValid.setBackgroundColor(color);
    }

    @Override
    public void updatePdNumber(@Nullable Integer pdNumber) {
        this.pdNumber.setText(pdNumber == null ? "" : String.format(getString(R.string.service_fee_pd_number_format), pdNumber));
    }

    @Override
    public void updatePdErrors(@NonNull List<PassageResult> pdErrors) {
        backlightReason(pdErrors);
    }

    @Override
    public void updateDateActionsFrom(@Nullable Date from) {
        dateActionsFrom.setText(from == null ? "" : DateFormatOperations.getOutDate(from));
    }

    @Override
    public void updateDateActionsTo(@Nullable Date to) {
        String text;

        if (to != null) {
            text = DateFormatOperations.getOutDate(to);
        } else {
            text = getString(R.string.service_fee_pd_date_to_unlimited);
        }

        dateActionsTo.setText(text);
    }

    @Override
    public void updateServiceFeeNotFound(boolean visible) {
        if (visible) {
            serviceFeeInfo.setVisibility(View.GONE);
            serviceFeeNotFound.setVisibility(View.VISIBLE);
        } else {
            serviceFeeInfo.setVisibility(View.VISIBLE);
            serviceFeeNotFound.setVisibility(View.GONE);
        }
    }

    @Override
    public void showZoomPdDialog(ServicePdViewModel pdViewModel) {
        callback.setHardwareButtonsEnabled(false);
        ZoomPdFragment zoomPdFragment = ZoomPdFragment.newInstance();
        zoomPdFragment.setCancelable(true);
        zoomPdFragment.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        zoomPdFragment.setPdViewModel(pdViewModel);
        zoomPdFragment.setInteractionListener(() -> callback.setHardwareButtonsEnabled(true));
    }

    /**
     * Определяет вьюхи с некореекными данными в зависимости от причин невалидности ПД.
     *
     * @param errors Список ошибок при проверке ПД
     * @deprecated Скопирован старый функционал, Переписать, когда будет время.
     */
    @Deprecated
    private void backlightReason(@NonNull List<PassageResult> errors) {
        String text;
        int visibility;
        List<Integer> listOfIdView = new ArrayList<>();

        if (errors.isEmpty()) {
            text = "";
            visibility = View.GONE;
        } else {
            text = "";

            for (PassageResult errorType : errors) {
                switch (errorType) {
                    case WeekendOnly:
                    case WorkingDayOnly:
                        listOfIdView.add(R.id.pd_title);
                        break;

                    case TooEarly:
                        listOfIdView.add(R.id.date_actions_title);
                        listOfIdView.add(R.id.date_actions_from);
                        break;

                    case TooLate:
                        listOfIdView.add(R.id.date_actions_title);
                        listOfIdView.add(R.id.date_actions_to);
                        break;

                    case InvalidSign:
                        text = getString(R.string.read_bsc_error_ecp_error);
                        break;

                    case SignKeyRevoked:
                        text = getString(R.string.read_bsc_error_ecp_revoked);
                        break;

                    case BannedByStopListTickets:
                        text = getString(R.string.read_bsc_error_pd_in_stop_list);
                        break;
                }
            }

            visibility = View.VISIBLE;
        }

        this.pdError.setText(text);
        this.pdError.setVisibility(visibility);

        backlightView(listOfIdView, this.serviceFeeInfo, !errors.isEmpty());
    }

    /**
     * Раскрасит красным вьюхи, содержащие некорректные данные
     *
     * @param viewId       - Список идентификаторов вьюх
     * @param view         - Контейнер с вьюхами
     * @param setGrayColor - необходимость установки серого цвета для остальных вьюх
     * @deprecated Скопирован старый функционал, Переписать, когда будет время.
     */
    @Deprecated
    private void backlightView(List<Integer> viewId, View view, boolean setGrayColor) {
        final int red = ActivityCompat.getColor(getActivity(), R.color.red);
        final int gray = ActivityCompat.getColor(getActivity(), R.color.gray);

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();

            for (int i = 0; i < childCount; i++) {
                View view2 = viewGroup.getChildAt(i);

                if (view2 instanceof ViewGroup) {
                    backlightView(viewId, view2, setGrayColor);
                } else if (view2 instanceof Button) {
                    break;
                } else if (view2 instanceof TextView) {
                    TextView textView = (TextView) view2;
                    //поле со статусом не трогаем
                    //поле с описанием ошибки тоже не трогаем
                    if (textView.getId() != R.id.pd_valid && textView.getId() != R.id.pd_error) {
                        Iterator<Integer> iterator = viewId.iterator();
                        boolean colored = false;

                        while (iterator.hasNext()) {
                            int id = iterator.next();
                            if (id == textView.getId()) {
                                // красим вью с причиной недействия и выходим
                                textView.setTextColor(red);
                                textView.setTypeface(null, Typeface.BOLD);
                                iterator.remove();
                                colored = true;

                                break;
                            }
                        }
                        if (setGrayColor && !colored) {
                            // красим в сервый цвет
                            textView.setTextColor(gray);
                        }
                    }
                }
            }
        }
    }

    public interface Callback {
        void setHardwareButtonsEnabled(boolean value);
    }

}
