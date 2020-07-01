package ru.ppr.cppk.settings.AdditionalSettingsFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;

import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;

/**
 * Фрагмент настроек "Обновления".
 */
public class SettingsUpdateFragment extends FragmentParent {

    public interface OnFragmentInteraction {
        void onChangeStopListActionDayCount();
    }

    private ViewGroup stopListActionDayLayout;
    private TextView countDayView;
    private View stopListArrowView;

    private Globals globals;
    private OnFragmentInteraction listener;

    private Holder<PrivateSettings> privateSettingsHolder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteraction) activity;
        } catch (ClassCastException exception) {
            throw new IllegalStateException("Activity " + activity.getClass().getName()
                    + " must implement SettingsUpdateFragment#OnFragmentInteraction interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globals = (Globals) getActivity().getApplication();
        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.additional_setting_update_fragment, container, false);

        final View dbInfoView = view.findViewById(R.id.dbInfo);
        dbInfoView.setOnClickListener(v -> showDbInfo());

        stopListActionDayLayout = (ViewGroup) view.findViewById(R.id.additional_settings_stop_list_action_day);
        countDayView = (TextView) view.findViewById(R.id.additional_setting_stop_list_count_day);
        countDayView.setOnClickListener(v -> changeStopListActionDays());
        stopListArrowView = view.findViewById(R.id.additional_settings_stop_list_arrow);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setStopListActionDays();
        setCurrentStopListActionDay();
    }

    /**
     * Метод для отображения информации о БД.
     */
    private void showDbInfo() {
        Navigator.navigateToUpdateInfoActivity(getActivity());
    }

    /**
     * Метод для установления текущего значения срока действия версии стоп-листов.
     */
    private void setCurrentStopListActionDay() {
        countDayView.setText(String.format(getString(R.string.stop_list_action_day_count), privateSettingsHolder.get().getStopListValidTime()));
    }

    /**
     * Метод для проверки наличия прав у данного пользователя для изменения срока дейтсвия версии стоп-листов.
     *
     * @return результат провреки.
     */
    private boolean canChangeStopListActionDay() {
        return getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.StopListValidDays);
    }

    /**
     * Метод для обновления срока действия версии стоп-листов.
     */
    private void setStopListActionDays() {
        final boolean isCanSetStopListActionDays = hasPermission(PermissionDvc.StopListValidDays);

        stopListActionDayLayout.setEnabled(isCanSetStopListActionDays);
        stopListActionDayLayout.setBackgroundResource(isCanSetStopListActionDays ? R.color.white : R.color.gray_inactive);
        stopListArrowView.setVisibility(isCanSetStopListActionDays ? View.VISIBLE : View.GONE);
    }

    /**
     * Метод для изменения срока дейтсвия версии стоп-листов.
     */
    private void changeStopListActionDays() {
        if (canChangeStopListActionDay()) {
            if (listener != null) {
                listener.onChangeStopListActionDayCount();
            }
        } else {
            ((SystemBarActivity) getActivity()).makeErrorAccessToast();
        }
    }

}
