package ru.ppr.cppk.settings.AdditionalSettingsFragments;

import android.annotation.SuppressLint;
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
import ru.ppr.cppk.settings.inputs.InputDataActivity.ChangeAction;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;

/**
 * Фрагмент настроек "Интерфейс".
 */
public class InterfaceFragment extends FragmentParent {

    private ViewGroup repealTimeLayout;
    private TextView repealTimeTextView;
    private View repealTimeArrowView;

    private ViewGroup timeCloseShiftLayout;
    private TextView timeCloseShiftTextView;
    private View timeCloseShiftArrowView;

    private Globals globals = null;
    private String shortMinString = null;

    private Holder<PrivateSettings> privateSettingsHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        globals = (Globals) getActivity().getApplication();
        shortMinString = getString(R.string.short_minute);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.additional_setting_interface_fragment, null);

        repealTimeLayout = (ViewGroup) view.findViewById(R.id.additional_setting_repeal_time_layout);
        repealTimeLayout.setOnClickListener(v -> changeTimeForRepeal());
        repealTimeTextView = (TextView) view.findViewById(R.id.additional_settings_time_of_repeal_value);
        repealTimeArrowView = view.findViewById(R.id.additional_settings_time_of_repeal_arrow);

        timeCloseShiftLayout = (ViewGroup) view.findViewById(R.id.additional_setting_time_close_shift_layout);
        timeCloseShiftLayout.setOnClickListener(v -> changeTimeToCloseShiftMessage());
        timeCloseShiftTextView = (TextView) view.findViewById(R.id.additional_settings_time_close_shift_value);
        timeCloseShiftArrowView = view.findViewById(R.id.additional_settings_time_close_shift_arrow);

        final ViewGroup setSound = (ViewGroup) view.findViewById(R.id.additional_setting_set_sound);
        setSound.setOnClickListener(v -> changeSound());

        View toFineListManagement = view.findViewById(R.id.to_fine_list_management);
        toFineListManagement.setOnClickListener(v -> toFineListManagement());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setTimeForRepeal();
        setTimeToCloseShiftMessage();
    }

    /**
     * Метод для получения текущего значения времени на аннулирование ПД.
     *
     * @return текущее значение времени на аннулирование ПД.
     */
    private int getCurrentTimeForRepeal() {
        return privateSettingsHolder.get().getTimeForAnnulate();
    }

    /**
     * Метод для обновления времени на аннулирование ПД.
     */
    private void setTimeForRepeal() {
        final boolean isCanSetTimeForRepeal = hasPermission(PermissionDvc.ChangeTimeForAnnulate);

        repealTimeLayout.setEnabled(isCanSetTimeForRepeal);
        repealTimeLayout.setBackgroundResource(isCanSetTimeForRepeal ? R.color.white : R.color.gray_inactive);
        repealTimeTextView.setText(String.format(shortMinString, getCurrentTimeForRepeal()));
        repealTimeArrowView.setVisibility(isCanSetTimeForRepeal ? View.VISIBLE : View.GONE);
    }

    /**
     * Метод для изменения времени на аннулирование ПД.
     */
    private void changeTimeForRepeal() {
        if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.ChangeTimeForAnnulate)) {
            Navigator.navigateToInputDataActivity(getActivity(), ChangeAction.ANNUL_PD_TIME);
        } else {
            ((SystemBarActivity) getActivity()).makeErrorAccessToast();
        }
    }

    /**
     * Метод для получения текущего значения периода времени для вывода предупреждений о закрытии смены.
     *
     * @return период времени для вывода предупреждений о закрытии смены.
     */
    private int getCurrentTimeToCloseShiftMessage() {
        return privateSettingsHolder.get().getTimeForShiftCloseMessage();
    }

    /**
     * Метод для обновления периода времени для вывода предупреждений о закрытии смены.
     */
    private void setTimeToCloseShiftMessage() {
        final boolean isCanSetTimeToCloseShiftMessage = hasPermission(PermissionDvc.ChangeTimeToCloseShiftMessage);

        timeCloseShiftLayout.setEnabled(isCanSetTimeToCloseShiftMessage);
        timeCloseShiftLayout.setBackgroundResource(isCanSetTimeToCloseShiftMessage ? R.color.white : R.color.gray_inactive);
        timeCloseShiftTextView.setText(String.format(shortMinString, getCurrentTimeToCloseShiftMessage()));
        timeCloseShiftArrowView.setVisibility(isCanSetTimeToCloseShiftMessage ? View.VISIBLE : View.GONE);
    }

    /**
     * Метод для изменения периода времени для вывода предупреждений о закрытии смены.
     */
    private void changeTimeToCloseShiftMessage() {
        if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.ChangeTimeToCloseShiftMessage)) {
            Navigator.navigateToInputDataActivity(getActivity(), ChangeAction.ATTENTION_CLOSE_SHIFT_TIME);
        } else {
            ((SystemBarActivity) getActivity()).makeErrorAccessToast();
        }
    }

    /**
     * Метод для изменения настроек звука.
     */
    private void changeSound() {
        Navigator.navigateToSoundSettingsActivity(getActivity());
    }

    /**
     * Метод для управления списком штрафов.
     */
    private void toFineListManagement() {
        Navigator.navigateToFineListManagementActivity(getActivity());
    }

}
