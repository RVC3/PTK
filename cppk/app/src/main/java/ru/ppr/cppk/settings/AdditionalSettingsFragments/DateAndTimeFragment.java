package ru.ppr.cppk.settings.AdditionalSettingsFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Фрагмент настроек "Дата и время".
 */
public class DateAndTimeFragment extends FragmentParent {

    private ViewGroup setDateTimeLayout;
    private View setDateTimeArrowView;

    private ViewGroup syncTimeLayout;
    private ImageView syncTimeCheckBox;

    private ViewGroup permissionToExcessLayout;
    private ImageView permissionToExcessCheckbox;

    private Globals globals;
    private OnFragmentInteraction listener;

    private Holder<PrivateSettings> privateSettingsHolder;

    public interface OnFragmentInteraction {
        /**
         * Вызывается при вопросе об установке настройки автоматической синхронизации времени
         */
        void syncTimeDialog();

        void changeSyncTimeEnable();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (OnFragmentInteraction) activity;
        } catch (ClassCastException e) {

            throw new IllegalStateException("Activity " + activity.getClass().getName()
                    + " must implement DateAndTimeFragment#OnFragmentInteraction");
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
        privateSettingsHolder = di().getPrivateSettings();
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.additional_setting_date_and_time_fragment, null);

        setDateTimeLayout = (RelativeLayout) view.findViewById(R.id.adtSettingSetDateTime);
        setDateTimeLayout.setOnClickListener(v -> changeDateTime());
        setDateTimeArrowView = view.findViewById(R.id.additional_setting_set_date_time_arrow);

        syncTimeLayout = (RelativeLayout) view.findViewById(R.id.adtSettingSyncTime);
        syncTimeLayout.setOnClickListener(v -> changeSyncTime());
        syncTimeCheckBox = (ImageView) view.findViewById(R.id.adtSettingSyncTimeCheckBox);

        permissionToExcessLayout = (RelativeLayout) view.findViewById(R.id.adtSettingPermissionToExcess);
        permissionToExcessLayout.setOnClickListener(v -> changeAutoTimeSyncEnabled());
        permissionToExcessCheckbox = (ImageView) view.findViewById(R.id.adtSettingPermissionToExcessCheckbox);

        setTimeSyncEnabled(isTimeSyncEnabled());
        setAutoTimeSyncEnabled(privateSettingsHolder.get().isAutoTimeSyncEnabled());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setDateTime();
        setSyncTime();
        setChangeAutoTimeSyncEnabled();
    }

    /**
     * Метод для обновления даты и времени.
     */
    private void setDateTime() {
        final boolean isCanChangeDateTime = hasPermission(PermissionDvc.ChangeDateTime);

        setDateTimeLayout.setEnabled(isCanChangeDateTime);
        setDateTimeLayout.setBackgroundResource(isCanChangeDateTime ? R.color.white : R.color.gray_inactive);
        setDateTimeArrowView.setVisibility(isCanChangeDateTime ? View.VISIBLE : View.GONE);
    }

    /**
     * Метод для изменения даты и времени.
     */
    public void changeDateTime() {
        if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.ChangeDateTime)) {
            Navigator.navigateToSetTimeActivity(getActivity());
        } else
            ((SystemBarActivity) getActivity()).makeErrorAccessToast();
    }

    /**
     * Метод для обновления синхронизции время при подключении ПТК к АРМ.
     */
    private void setSyncTime() {
        final boolean isCanSyncTime = hasPermission(PermissionDvc.TimeSync);

        syncTimeLayout.setEnabled(isCanSyncTime);
        syncTimeLayout.setBackgroundResource(isCanSyncTime ? R.color.white : R.color.gray_inactive);
    }

    /**
     * Синхронизировать время при подключении ПТК к АРМ
     */
    public void changeSyncTime() {
        if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.TimeSync)) {
            if (isTimeSyncEnabled()) {
                if (listener != null) {
                    listener.changeSyncTimeEnable();
                }
                return;
            }

            if (listener != null) {
                listener.syncTimeDialog();
            }
        } else
            ((SystemBarActivity) getActivity()).makeErrorAccessToast();

    }

    /**
     * Метод для проверки включена ли синхронизация время при подключении ПТК к АРМ.
     *
     * @return результат проверки.
     */
    private boolean isTimeSyncEnabled() {
        return privateSettingsHolder.get().isTimeSyncEnabled();
    }

    /**
     * Метод для обновления отображения разрешения на превышение допустимого периода изменения времени.
     */
    private void setChangeAutoTimeSyncEnabled() {
        final boolean isCanChangeAutoTimeSync = hasPermission(PermissionDvc.ChangeAutoTimeSync);

        permissionToExcessLayout.setEnabled(isCanChangeAutoTimeSync);
        permissionToExcessLayout.setBackgroundResource(isCanChangeAutoTimeSync ? R.color.white : R.color.gray_inactive);
    }

    /**
     * Разрешение на превышение допустимого периода изменения времени
     */
    private void changeAutoTimeSyncEnabled() {
        if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.ChangeAutoTimeSync)) {
            final PrivateSettings privateSettings = new PrivateSettings(privateSettingsHolder.get());
            final boolean isAutoTimeSyncEnabled = privateSettings.isAutoTimeSyncEnabled();
            privateSettings.setAutoTimeSyncEnabled(!isAutoTimeSyncEnabled);
            Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
            privateSettingsHolder.set(privateSettings);

            setAutoTimeSyncEnabled(!isAutoTimeSyncEnabled);
        } else
            ((SystemBarActivity) getActivity()).makeErrorAccessToast();
    }

    /**
     * Метод для отображения состояния синхронизации времени при подключении ПТК к АРМ.
     *
     * @param enabled синхронизация включена.
     */
    public void setTimeSyncEnabled(boolean enabled) {
        if (isDetached()) return;

        if (enabled) {
            syncTimeCheckBox.setVisibility(View.VISIBLE);
        } else {
            syncTimeCheckBox.setVisibility(View.GONE);
        }
    }

    /**
     * Метод для отображения состояния разрешения на превышение допустимого периода изменения времени.
     *
     * @param enabled разрешено превышение допустимого периода изменения времени.
     */
    private void setAutoTimeSyncEnabled(boolean enabled) {
        if (enabled) {
            permissionToExcessCheckbox.setVisibility(View.VISIBLE);
        } else {
            permissionToExcessCheckbox.setVisibility(View.GONE);
        }
    }

}
