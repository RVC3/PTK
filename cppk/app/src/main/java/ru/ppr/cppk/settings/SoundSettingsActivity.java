package ru.ppr.cppk.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.Sounds.Ringtone.BeepType;
import ru.ppr.cppk.systembar.SystemBarActivity;

public class SoundSettingsActivity extends SystemBarActivity {

    private Globals g;

    private ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_sound);
        g = (Globals) getApplication();

        viewHolder = new ViewHolder();

        viewHolder.adtSettingSoundOnReadErrorCheckbox = (ImageView) findViewById(R.id.adtSettingSoundOnReadErrorCheckbox);
        viewHolder.adtSettingSoundOnReadSuccessCheckbox = (ImageView) findViewById(R.id.adtSettingSoundOnReadSuccessCheckbox);

        setOnReadSuccess(SharedPreferencesUtils.isSoundOnReadBskSuccesEnable(g));
        setOnReadError(SharedPreferencesUtils.isSoundReadBskErrorEnabled(g));

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adtSettingSoundOnReadError:
                playSoundOnReadError();
                break;

            case R.id.adtSettingSoundOnReadSuccess:
                playSoundOnReadSuccess();
                break;

            case R.id.adtSettingSoundSetFailSound:
                startChangeBeepActivity(BeepType.FAIL_BEEP);
                break;

            case R.id.adtSettingSoundSetSuccessSoundr:
                startChangeBeepActivity(BeepType.SUCCES_BEEP);
                break;

            default:
                break;
        }

    }

    private void playSoundOnReadSuccess() {
        boolean playSoundOnReadBSKSucces = SharedPreferencesUtils.isSoundOnReadBskSuccesEnable(g);
        SharedPreferencesUtils.setSoundReadBskSuccesEnable(g, !playSoundOnReadBSKSucces);
        setOnReadSuccess(!playSoundOnReadBSKSucces);
    }

    private void playSoundOnReadError() {
        boolean playSoundOnReadBSKError = SharedPreferencesUtils.isSoundReadBskErrorEnabled(g);
        SharedPreferencesUtils.setSoundReadBskErrorEnable(g, !playSoundOnReadBSKError);
        setOnReadError(!playSoundOnReadBSKError);
    }

    private void setOnReadSuccess(boolean visibility) {
        if (visibility) {
            viewHolder.adtSettingSoundOnReadSuccessCheckbox.setVisibility(View.VISIBLE);
        } else {
            viewHolder.adtSettingSoundOnReadSuccessCheckbox.setVisibility(View.GONE);
        }
    }

    private void setOnReadError(boolean visibility) {
        if (visibility) {
            viewHolder.adtSettingSoundOnReadErrorCheckbox.setVisibility(View.VISIBLE);
        } else {
            viewHolder.adtSettingSoundOnReadErrorCheckbox.setVisibility(View.GONE);
        }
    }

    private void startChangeBeepActivity(BeepType beepType) {
        Intent intent = new Intent(this, BeepChangeActivity.class);
        intent.putExtra(BeepChangeActivity.BEEP_TYPE, beepType);
        startActivity(intent);
    }

    class ViewHolder {
        ImageView adtSettingSoundOnReadErrorCheckbox;
        ImageView adtSettingSoundOnReadSuccessCheckbox;
    }
}
