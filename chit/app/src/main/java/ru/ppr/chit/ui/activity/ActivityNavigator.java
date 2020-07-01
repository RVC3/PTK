package ru.ppr.chit.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.ui.activity.main.MainActivity;
import ru.ppr.chit.ui.activity.menu.MenuActivity;
import ru.ppr.chit.ui.activity.passengerlist.PassengerListActivity;
import ru.ppr.chit.ui.activity.readbarcode.ReadBarcodeActivity;
import ru.ppr.chit.ui.activity.readbsc.ReadBscActivity;
import ru.ppr.chit.ui.activity.readbsqrcode.ReadBsQrCodeActivity;
import ru.ppr.chit.ui.activity.root.RootActivity;
import ru.ppr.chit.ui.activity.rootaccess.RootAccessActivity;
import ru.ppr.chit.ui.activity.setdeviceid.SetDeviceIdActivity;
import ru.ppr.chit.ui.activity.setuser.SetUserActivity;
import ru.ppr.chit.ui.activity.splash.SplashActivity;
import ru.ppr.chit.ui.activity.ticketcontrol.TicketControlActivity;
import ru.ppr.chit.ui.activity.welcome.WelcomeActivity;
import ru.ppr.chit.ui.activity.workingstate.WorkingStateActivity;

/**
 * @author Dmitry Nevolin
 */
public class ActivityNavigator {

    private final Activity activity;

    @Inject
    public ActivityNavigator(Activity activity) {
        this.activity = activity;
    }

    private void animForward() {
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.zero_animation);
    }

    private void animBack() {
        activity.overridePendingTransition(R.anim.zero_animation, R.anim.slide_to_right);
    }

    public void navigateBack() {
        activity.finish();
        animBack();
    }

    public void navigateToSetDeviceId() {
        Intent intent = SetDeviceIdActivity.getCallingIntent(activity);
        activity.startActivity(intent);
        animForward();
    }

    public void navigateToSplash(boolean back) {
        Intent intent = SplashActivity.getCallingIntent(activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        if (back) {
            animBack();
        } else {
            animForward();
        }
    }

    public void navigateToWelcome(boolean clearTask) {
        Intent intent = WelcomeActivity.getCallingIntent(activity);
        if (clearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
        animForward();
    }

    public void navigateToMenu() {
        activity.startActivity(MenuActivity.getCallingIntent(activity));
        animForward();
    }

    public void navigateToReadBsc() {
        activity.startActivity(ReadBscActivity.getCallingIntent(activity));
        animForward();
    }

    public void navigateToReadBarcode() {
        activity.startActivity(ReadBarcodeActivity.getCallingIntent(activity));
        animForward();
    }

    public void navigateToFromListTicketControl(long ticketId) {
        activity.startActivity(TicketControlActivity.getFromListCallingIntent(activity, ticketId));
        animForward();
    }

    public void navigateToFromBscTicketControl() {
        activity.startActivity(TicketControlActivity.getFromBscCallingIntent(activity));
        activity.finish();
        animForward();
    }

    public void navigateToFromBarcodeTicketControl() {
        activity.startActivity(TicketControlActivity.getFromBarcodeCallingIntent(activity));
        activity.finish();
        animForward();
    }

    public void navigateToPassengerList() {
        activity.startActivity(PassengerListActivity.getCallingIntent(activity));
        animForward();
    }

    public void navigateToWorkingState() {
        activity.startActivity(WorkingStateActivity.getCallingIntent(activity));
        animForward();
    }

    public void navigateToWifiSettings(int requestCode) {
        activity.startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), requestCode);
        animForward();
    }

    public void navigateToReadBsQrCode(int requestCode) {
        activity.startActivityForResult(ReadBsQrCodeActivity.getCallingIntent(activity), requestCode);
        animForward();
    }

    public void navigateToSetUser() {
        Intent intent = SetUserActivity.getCallingIntent(activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);
        animForward();
    }

    public void navigateToMain(boolean back) {
        Intent intent = MainActivity.getCallingIntent(activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        if (back) {
            animBack();
        } else {
            animForward();
        }
    }

    public void navigateToRootAccess() {
        activity.startActivity(RootAccessActivity.getCallingIntent(activity)
                .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        animForward();
    }

    public void navigateToRoot(boolean backToSplash) {
        activity.startActivity(RootActivity.getCallingIntent(activity, backToSplash));
        animForward();
    }

}
