package ru.ppr.cppk.ui.activity.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.EnterPinActivity;
import ru.ppr.cppk.PosBindingActivity;
import ru.ppr.cppk.PrinterBindingActivity;
import ru.ppr.cppk.R;
import ru.ppr.cppk.RootAccessRequestActivity;
import ru.ppr.cppk.SetUserIdActivity;
import ru.ppr.cppk.SplashActivity;
import ru.ppr.cppk.WelcomeActivity;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.debug.Debug;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.AuthCard;
import ru.ppr.cppk.model.BluetoothDevice;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.model.ExtraPaymentParams;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.model.PdSaleSuccessParams;
import ru.ppr.cppk.repeal.DeletePdActivity;
import ru.ppr.cppk.repeal.RepealActivity;
import ru.ppr.cppk.repeal.RepealBSCReadErrorActivity;
import ru.ppr.cppk.repeal.RepealFinishActivity;
import ru.ppr.cppk.repeal.RepealFromHistoryActivity;
import ru.ppr.cppk.sell.SellPdSuccessActivity;
import ru.ppr.cppk.settings.AccountingTicketTapeEndActivity;
import ru.ppr.cppk.settings.AccountingTicketTapeStartActivity;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.UpdateInfoActivity;
import ru.ppr.cppk.settings.MobileCashSettingsActivity;
import ru.ppr.cppk.settings.ReportsActivity;
import ru.ppr.cppk.settings.SetTimeActivity;
import ru.ppr.cppk.settings.SoundSettingsActivity;
import ru.ppr.cppk.settings.UserInfoActivity;
import ru.ppr.cppk.settings.inputs.InputDataActivity;
import ru.ppr.cppk.ui.activity.ActivityTicketTapeIsNotSet;
import ru.ppr.cppk.ui.activity.ArmConnectedStateActivity;
import ru.ppr.cppk.ui.activity.BluetoothDeviceSearchActivity;
import ru.ppr.cppk.ui.activity.CalculateDeliveryActivity;
import ru.ppr.cppk.ui.activity.CalculateDeliveryFixedCostActivity;
import ru.ppr.cppk.ui.activity.CloseMonthActivity;
import ru.ppr.cppk.ui.activity.DevicesActivity;
import ru.ppr.cppk.ui.activity.EnterDayCodeActivity;
import ru.ppr.cppk.ui.activity.LockScreenActivity;
import ru.ppr.cppk.ui.activity.OpenShiftActivity;
import ru.ppr.cppk.ui.activity.ResultBarcodeActivity;
import ru.ppr.cppk.ui.activity.RfidResultActivity;
import ru.ppr.cppk.ui.activity.SettingsInformationActivity;
import ru.ppr.cppk.ui.activity.SettingsPosTerminalActivity;
import ru.ppr.cppk.ui.activity.closeTerminalDay.CloseTerminalDayActivity;
import ru.ppr.cppk.ui.activity.closeshift.CloseShiftActivity;
import ru.ppr.cppk.ui.activity.controlreadbarcode.ControlReadBarcodeActivity;
import ru.ppr.cppk.ui.activity.controlreadbsc.ControlReadBscActivity;
import ru.ppr.cppk.ui.activity.controlreadbsc.model.ControlReadBscParams;
import ru.ppr.cppk.ui.activity.decrementtrip.DecrementTripActivity;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripParams;
import ru.ppr.cppk.ui.activity.enterETicketData.EnterETicketDataActivity;
import ru.ppr.cppk.ui.activity.extraPayment.ExtraPaymentActivity;
import ru.ppr.cppk.ui.activity.fineListManagement.FineListManagementActivity;
import ru.ppr.cppk.ui.activity.fineSale.FineSaleActivity;
import ru.ppr.cppk.ui.activity.mainScreen.MainScreenActivity;
import ru.ppr.cppk.ui.activity.pdSale.PdSaleActivity;
import ru.ppr.cppk.ui.activity.pdrepeal.PdRepealActivity;
import ru.ppr.cppk.ui.activity.pdrepeal.PdRepealParams;
import ru.ppr.cppk.ui.activity.readpdfortransfer.ReadPdForTransferActivity;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.activity.repealreadbsc.RepealReadBscActivity;
import ru.ppr.cppk.ui.activity.resultBarcodeCoupon.ResultBarcodeCouponActivity;
import ru.ppr.cppk.ui.activity.root.ofdsettings.OfdSettingsActivity;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionActivity;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.cppk.ui.activity.selectTransferStations.SelectTransferStationsActivity;
import ru.ppr.cppk.ui.activity.selectionActivity.BindingStationSelectionActivity;
import ru.ppr.cppk.ui.activity.selectionActivity.ProductionSectionSelectionActivity;
import ru.ppr.cppk.ui.activity.selectionActivity.StationSelectionActivity;
import ru.ppr.cppk.ui.activity.senddocstoofd.SendDocsToOfdActivity;
import ru.ppr.cppk.ui.activity.serviceticketcontrol.ServiceTicketControlActivity;
import ru.ppr.cppk.ui.activity.settingsPrinter.SettingsPrinterActivity;
import ru.ppr.cppk.ui.activity.transfersale.TransferSaleActivity;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleParams;
import ru.ppr.cppk.ui.activity.transfersalestart.TransferSaleStartActivity;

/**
 * @author Aleksandr Brazhkin
 */
public class Navigator {

    public static void navigateToPdSaleActivity(Activity activity, PdSaleParams pdSaleParams) {
        activity.startActivity(PdSaleActivity.getCallingIntent(activity, pdSaleParams));
    }

    public static void navigateToSelectExemptionActivity(int requestCode, Activity activity, Fragment fragment, SelectExemptionParams selectExemptionParams) {
        Intent intent = SelectExemptionActivity.getCallingIntent(activity, selectExemptionParams);
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static void navigateToEnterETicketDataActivity(int requestCode, Activity activity, Fragment fragment, ETicketDataParams eTicketDataParams) {
        Intent intent = EnterETicketDataActivity.getCallingIntent(activity, eTicketDataParams);
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static void navigateToSellPdSuccessActivity(Activity activity, PdSaleSuccessParams pdSaleSuccessParams) {
        activity.startActivity(SellPdSuccessActivity.getCallingIntent(activity, pdSaleSuccessParams));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToCloseShiftActivity(Context context, boolean silentMode, boolean autoCloseMode) {
        context.startActivity(CloseShiftActivity.getCallingIntent(context, silentMode, autoCloseMode));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToSendDocsToOfdActivity(Context context, boolean backToWelcomeActivity) {
        context.startActivity(SendDocsToOfdActivity.getCallingIntent(context, backToWelcomeActivity));
    }

    public static void navigateToCloseMonthActivity(Context context, boolean silentMode) {
        context.startActivity(CloseMonthActivity.getCallingIntent(context, silentMode));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToFineSaleActivity(Activity activity) {
        activity.startActivity(FineSaleActivity.getCallingIntent(activity));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToFineListManagementActivity(@NonNull Activity activity) {
        activity.startActivity(FineListManagementActivity.getCallingIntent(activity));
    }

    /**
     * Выполняет запуск экрана авторизации.
     *
     * @param context       Конекст
     * @param emergencyMode {@code true}, для перехода в аварийный режим, {@code false} иначе
     */
    public static void navigateToSplashActivity(Context context, boolean emergencyMode) {
        final Intent intent = SplashActivity.getCallingIntent(context, emergencyMode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToEnterPinActivity(Context context, AuthCard authCard) {
        context.startActivity(EnterPinActivity.getCallingIntent(context, authCard));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToWelcomeActivity(Activity activity, boolean startOpenShift) {

        Intent intent = WelcomeActivity.getCallingIntent(activity, startOpenShift);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToAccountingTicketTapeStartActivity(Activity activity, boolean breakingMode) {
        navigateToAccountingTicketTapeStartActivity(activity, null, -1, breakingMode);
    }

    public static void navigateToAccountingTicketTapeStartActivity(Activity activity, Fragment fragment, int requestCode, boolean breakingMode) {
        if (fragment != null) {
            fragment.startActivityForResult(AccountingTicketTapeStartActivity.getCallingIntent(activity, breakingMode), requestCode);
        } else {
            activity.startActivityForResult(AccountingTicketTapeStartActivity.getCallingIntent(activity, breakingMode), requestCode);
        }
        //activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToAccountingTicketTapeEndActivity(Activity activity) {
        activity.startActivity(AccountingTicketTapeEndActivity.getCallingIntent(activity));
        // activity.overridePendingTransition(R.anim.no_animation,
        // R.anim.no_animation);
    }

    public static void navigateToEnterDayCodeActivity(Activity activity) {
        activity.startActivity(EnterDayCodeActivity.getCallingIntent(activity));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToOpenShiftActivity(Activity activity) {
        activity.startActivity(OpenShiftActivity.getCallingIntent(activity));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToDeletePdActivityForResult(Activity activity, int requestCode, String uidCardForDelete, int pdNumber) {
        activity.startActivityForResult(DeletePdActivity.getCallingIntent(activity, uidCardForDelete, pdNumber), requestCode);
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToRepealActivity(Activity activity) {
        if (Di.INSTANCE.getPrivateSettings().get().isSaleEnabled()) {
            activity.startActivity(RepealActivity.getCallingIntent(activity));
        } else {
            if (activity != null) {
                Di.INSTANCE.getApp().getToaster().showToast(R.string.action_is_not_avaliable);
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
    }

    public static void navigateToPdRepealActivity(Activity activity, PdRepealParams pdRepealParams) {
        activity.startActivity(PdRepealActivity.getCallingIntent(activity, pdRepealParams));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToRepealFinishActivity(Activity activity, List<PD> pdList, long id) {
        activity.startActivity(RepealFinishActivity.getCallingIntent(activity, pdList, id));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToRepealFromHistoryActivity(Activity activity) {
        activity.startActivity(RepealFromHistoryActivity.getCallingIntent(activity));
        // activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToExtraPaymentActivity(Activity activity, ExtraPaymentParams extraPaymentParams) {
        activity.startActivity(ExtraPaymentActivity.getCallingIntent(activity, extraPaymentParams));
        //activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToActivityTicketTapeIsNotSet(Activity activity) {
        navigateToTicketTapeIsNotSetActivity(activity, null, -1);
    }

    public static void navigateToTicketTapeIsNotSetActivity(Activity activity, Fragment fragment, int requestCode) {
        if (fragment != null) {
            fragment.startActivityForResult(ActivityTicketTapeIsNotSet.getCallingIntent(activity), requestCode);
        } else {
            activity.startActivityForResult(ActivityTicketTapeIsNotSet.getCallingIntent(activity), requestCode);
        }
    }

    public static void navigateToReportsActivity(Activity activity) {
        activity.startActivity(ReportsActivity.getCallingIntent(activity));
    }

    /**
     * Выполняет запуск экрана меню с 4-мя синими кнопками.
     *
     * @param context Контекст
     */
    public static void navigateToMenuActivity(Context context) {
        final Intent intent = MainScreenActivity.getCallingIntent(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void navigateToBluetoothDeviceSearchActivity(Activity activity, Fragment fragment, int requestCode, BluetoothDevice currentDevice) {
        if (fragment != null) {
            fragment.startActivityForResult(BluetoothDeviceSearchActivity.getCallingIntent(activity, currentDevice), requestCode);
        } else {
            activity.startActivityForResult(BluetoothDeviceSearchActivity.getCallingIntent(activity, currentDevice), requestCode);
        }
        //activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToDevicesActivity(Activity activity) {
        activity.startActivity(DevicesActivity.getCallingIntent(activity));
    }

    public static void navigateToSettingsPosTerminalActivity(Activity activity) {
        activity.startActivity(SettingsPosTerminalActivity.getCallingIntent(activity));
    }

    public static void navigateToDevicesInformationActivity(Activity activity) {
        activity.startActivity(SettingsInformationActivity.getCallingIntent(activity));
    }

    public static void navigateToSettingsPrinterActivity(Activity activity, Fragment fragment, int requestCode) {
        if (fragment != null) {
            fragment.startActivityForResult(SettingsPrinterActivity.getCallingIntent(activity), requestCode);
        } else {
            activity.startActivityForResult(SettingsPrinterActivity.getCallingIntent(activity), requestCode);
        }
        //activity.overridePendingTransition(R.anim.no_animation, R.anim.no_animation);
    }

    public static void navigateToDebugActivity(Activity activity, boolean startSplashOnBack) {
        activity.startActivity(Debug.getCallingIntent(activity, startSplashOnBack));
    }

    public static void navigateToRootAccessRequestActivity(Activity activity) {
        activity.startActivity(RootAccessRequestActivity.getCallingIntent(activity).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    public static void navigateToPosBindingActivity(Activity activity) {
        activity.startActivity(PosBindingActivity.getCallingIntent(activity));
    }

    public static void navigateToPrinterBindingActivity(Activity activity) {
        activity.startActivity(PrinterBindingActivity.getCallingIntent(activity));
    }

    public static void navigateToSetUserIdActivity(Activity activity) {
        activity.startActivity(SetUserIdActivity.getCallingIntent(activity));
    }

    public static void navigateToArmConnectedStateActivity(Context context, boolean flagKill) {
        Intent callingIntent = ArmConnectedStateActivity.getCallingIntent(context, flagKill);
        callingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callingIntent);
    }

    public static void navigateToCalculateDeliveryFixedCostActivity(Activity activity, BigDecimal costPD) {
        activity.startActivity(CalculateDeliveryFixedCostActivity.getCallingIntent(activity, costPD));
    }

    public static void navigateToCalculateDeliveryActivity(Activity activity) {
        activity.startActivity(CalculateDeliveryActivity.getCallingIntent(activity));
    }

    public static void navigateToCloseTerminalDayActivity(Activity activity) {
        activity.startActivity(CloseTerminalDayActivity.getCallingIntent(activity));
    }

    public static void navigateToLockScreenActivity(Activity activity) {
        Intent callingIntent = LockScreenActivity.getCallingIntent(activity);
        activity.startActivity(callingIntent);
    }

    /**
     * Переход на Activity выбора станции работы ПТК
     *
     * @param activity
     * @param fragment
     * @param requestCode
     */
    public static void navigateToStationSelectionActivity(Activity activity, Fragment fragment, int requestCode) {
        Intent callingIntent = StationSelectionActivity.getCallingIntent(activity);
        if (fragment != null) {
            fragment.startActivityForResult(callingIntent, requestCode);
        } else {
            activity.startActivityForResult(callingIntent, requestCode);
        }
    }

    /**
     * Переход на Activity выбора станции привязки ПТК
     *
     * @param activity
     * @param fragment
     * @param requestCode
     */
    public static void navigateToBindingStationSelectionActivity(Activity activity, Fragment fragment, int requestCode) {
        Intent callingIntent = BindingStationSelectionActivity.getCallingIntent(activity);
        if (fragment != null) {
            fragment.startActivityForResult(callingIntent, requestCode);
        } else {
            activity.startActivityForResult(callingIntent, requestCode);
        }
    }

    /**
     * Переход на Activity выбора участка работы ПТК
     *
     * @param activity
     * @param fragment
     * @param requestCode
     */
    public static void navigateToProductionSectionSelectionActivity(Activity activity, Fragment fragment, int requestCode) {
        Intent callingIntent = ProductionSectionSelectionActivity.getCallingIntent(activity);
        if (fragment != null) {
            fragment.startActivityForResult(callingIntent, requestCode);
        } else {
            activity.startActivityForResult(callingIntent, requestCode);
        }
    }

    /**
     * Переход на Activity настройки режима работы мобильной кассы ПТК
     *
     * @param activity
     */
    public static void navigateToMobileCashSettingsActivity(Activity activity) {
        Intent callingIntent = MobileCashSettingsActivity.getCallingIntent(activity);
        activity.startActivity(callingIntent);
    }

    public static void navigateToSetTimeActivity(@NonNull final Activity activity) {
        final Intent intent = new Intent(activity, SetTimeActivity.class);
        activity.startActivity(intent);
    }

    public static void navigateToInputDataActivity(@NonNull final Activity activity, @NonNull final InputDataActivity.ChangeAction changeAction) {
        activity.startActivity(InputDataActivity.getNewIntent(activity, changeAction));
    }

    public static void navigateToSoundSettingsActivity(@NonNull final Activity activity) {
        final Intent intent = new Intent(activity, SoundSettingsActivity.class);
        activity.startActivity(intent);
    }

    public static void navigateToUserInfoActivity(@NonNull final Activity activity) {
        final Intent intent = new Intent(activity, UserInfoActivity.class);
        activity.startActivity(intent);
    }

    public static void navigateToChangeWiFiSettings(@NonNull final Activity activity) {
        activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public static void navigateToUpdateInfoActivity(@NonNull final Activity activity) {
        final Intent intent = new Intent(activity, UpdateInfoActivity.class);
        activity.startActivity(intent);
    }

    public static void navigateToResultBarcodeActivity(@NonNull Activity activity,
                                                       @Nullable ArrayList<PD> pdList,
                                                       @Nullable ReadForTransferParams readForTransferParams) {
        activity.startActivity(ResultBarcodeActivity.getCallingIntent(activity, pdList, readForTransferParams)
                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    public static void navigateToRfidResultActivity(@NonNull Activity activity,
                                                    @Nullable ArrayList<PD> pdList,
                                                    @Nullable BscInformation bscInformation,
                                                    @Nullable ReadForTransferParams readForTransferParams) {
        Intent intent = RfidResultActivity.getCallingIntent(activity, pdList, bscInformation, readForTransferParams);
        activity.startActivity(intent);
    }

    public static void navigateToResultBarcodeCouponActivity(@NonNull Activity activity, long couponReadEventId) {
        Intent intent = ResultBarcodeCouponActivity.getCallingIntent(activity, couponReadEventId);
        activity.startActivity(intent);
    }

    public static void navigateToControlReadBscActivity(@NonNull Activity activity, @NonNull ControlReadBscParams params) {
        activity.startActivity(ControlReadBscActivity.getCallingIntent(activity, params));
    }

    public static void navigateToControlReadBarcodeActivity(@NonNull Activity activity, @Nullable ReadForTransferParams readForTransferParams) {
        activity.startActivity(ControlReadBarcodeActivity.getCallingIntent(activity, readForTransferParams)
                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    public static void navigateToRepealReadBscActivity(@NonNull Activity activity) {
        Intent intent = RepealReadBscActivity.getCallingIntent(activity);
        activity.startActivity(intent);
    }

    public static void navigateToServiceTicketControlActivity(@NonNull Activity activity) {
        Intent intent = ServiceTicketControlActivity.getCallingIntent(activity);
        activity.startActivity(intent);
    }

    public static void navigateToRepealBscReadErrorActivity(@NonNull Activity activity) {
        Intent intent = RepealBSCReadErrorActivity.getCallingIntent(activity);
        activity.startActivity(intent);
    }

    public static void navigateToSelectTransferStationsActivity(@NonNull Activity activity) {
        Intent intent = SelectTransferStationsActivity.getCallingIntent(activity);
        activity.startActivity(intent);
    }

    public static void navigateToReadPdForTransferActivity(@NonNull Activity activity, @NonNull ReadForTransferParams params) {
        Intent intent = ReadPdForTransferActivity.getCallingIntent(activity, params);
        activity.startActivity(intent);
    }

    public static void navigateToDecrementTripActivity(@NonNull Activity activity,
                                                       @Nullable Fragment fragment,
                                                       DecrementTripParams decrementTripParams,
                                                       int requestCode) {
        Intent intent = DecrementTripActivity.getCallingIntent(activity, decrementTripParams);
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static void navigateToTransferSaleStartActivity(@NonNull Activity activity) {
        activity.startActivity(TransferSaleStartActivity.getCallingIntent(activity));
    }

    public static void navigateToTransferSaleActivity(@NonNull Activity activity,
                                                      @NonNull TransferSaleParams transferSaleParams) {
        activity.startActivity(TransferSaleActivity.getCallingIntent(activity, transferSaleParams));
    }

    public static void navigateToOfdSettingsActivity(@NonNull Activity activity) {
        Intent intent = OfdSettingsActivity.getCallingIntent(activity);
        activity.startActivity(intent);
    }

}
