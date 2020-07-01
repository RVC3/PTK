package ru.ppr.cppk.settings;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.Sounds.BeepPlayer;
import ru.ppr.cppk.Sounds.BeepRingtone;
import ru.ppr.cppk.Sounds.Ringtone.BeepType;
import ru.ppr.logger.Logger;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.utils.PlaySound;

/**
 * Активити позволяет изменить рингтон, который будет проигрываться при
 * удачном/неудачном считывании ПД
 *
 * @author A.Ushakov
 */
public class BeepChangeActivity extends SystemBarActivity implements OnItemClickListener {

    public static final String BEEP_TYPE = "BeepType";

    private static final String TAG = "BeepChangeActivity";

    private BeepType beepType = null;
    private String selectedBeepName = null;
    private PlaySound playSound = null;
    private String pathToBeep;
    private List<BeepRingtone> beepList = null;
    private ListView beepListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound_beep_dialog);

        // получаем путь до папки с рингтонами
        beepType = (BeepType) getIntent().getSerializableExtra(BEEP_TYPE);
        switch (beepType) {
            case FAIL_BEEP:
                pathToBeep = GlobalConstants.FAIL_BEEP_PATH + "/";
                break;

            case SUCCES_BEEP:
                pathToBeep = GlobalConstants.SUCCESS_BEEP_PATH + "/";
                break;

            default:
                break;
        }

        // получаем текущий установленный рингтон
        selectedBeepName = SharedPreferencesUtils.getBeepFilename(getApplicationContext(), beepType);
        Logger.info(TAG, "Current beep name - " + selectedBeepName);
        // получаем список доступных рингтонов
        String[] beeps = getBeeps(beepType);

        // получаем индекс текущего рингтона
        int selectedBeepIndex = getCurrentBeepIndex(beeps, selectedBeepName);

        // загружаем звуки
        playSound = new PlaySound(getApplicationContext());
        createSound(beeps, pathToBeep);

        // создаем адаптер
        BeepAdapter adapter = new BeepAdapter(getApplicationContext(), beeps);

        // настраиваем view
        AQuery aQuery = new AQuery(this);
        beepListView = aQuery.id(R.id.sound_beep_list_view).itemClicked(this).getListView();
        beepListView.setAdapter(adapter);
        beepListView.setItemChecked(selectedBeepIndex, true);
        aQuery.id(R.id.sound_beep_cancel).clicked(cancelClickListener);
        aQuery.id(R.id.sound_beep_okey).clicked(saveClickListener);
    }

    /**
     * Возвращает индекс текущего рингтона в списке
     *
     * @param beeps       список доступных рингтонов
     * @param currentBeep текущий установленный рингтон
     * @return
     */
    private int getCurrentBeepIndex(@Nullable String[] beeps, @Nullable String currentBeep) {
        int index = 0;

        if (beeps == null || beeps.length == 0 || currentBeep == null) {
            return 0;
        }

        for (String beep : beeps) {
            if (beep.equals(currentBeep))
                break;
            index++;
        }

        return index;
    }

    /**
     * Загружает рингтоны в память
     *
     * @param beeps      список рингтонов
     * @param pathToBeep путь до папки с рингтонами
     */
    private void createSound(String[] beeps, String pathToBeep) {

        beepList = new ArrayList<BeepRingtone>();
        for (String beep : beeps) {
            BeepRingtone beepRingtone = new BeepRingtone(playSound, pathToBeep + beep);
            beepList.add(beepRingtone);
        }
    }

    /**
     * Возвращает список рингтонов для типа @see {@link BeepType}
     *
     * @param beepType
     * @return
     */
    private String[] getBeeps(BeepType beepType) {

        AssetManager assetManager = getAssets();

        String pathToBeep = null;

        switch (beepType) {
            case FAIL_BEEP:
                pathToBeep = GlobalConstants.FAIL_BEEP_PATH;
                break;

            case SUCCES_BEEP:
                pathToBeep = GlobalConstants.SUCCESS_BEEP_PATH;
                break;

            default:
                break;
        }

        if (pathToBeep == null) {
            Logger.info(TAG, "Path to beep is null");
            return null;
        }
        String[] beepFiles = null;
        try {
            beepFiles = assetManager.list(pathToBeep);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return beepFiles;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        selectedBeepName = (String) parent.getItemAtPosition(position);
        Logger.info(TAG, pathToBeep + selectedBeepName);
        beepList.get(position).play();
    }

    /**
     * Сохраняет имя выбранного рингтона в настройки
     *
     * @param beepType
     * @param beepName
     */
    private void saveCurrentBeepName(BeepType beepType, String beepName) {
        if (beepType != null && beepName != null) {
            Logger.info(TAG, "Save beep " + beepName + " to " + beepType.getTypeValue());
            // сохраняем имя рингтона
            SharedPreferencesUtils.setBeppFileName(getApplicationContext(), beepType, beepName);
            Globals globals = (Globals) getApplication();
            // перезагружаем соответствующий рингтон
            BeepPlayer beepPlayer = BeepPlayer.getInstance(globals);
            beepPlayer.loadBeep(beepType);
        } else {
            Logger.info(TAG, "Error while save beep");
        }
    }

    private OnClickListener saveClickListener = v -> {
        saveCurrentBeepName(beepType, selectedBeepName);
        finish();
    };

    private OnClickListener cancelClickListener = v -> finish();

    /**
     * Адаптер для отображения рингтонов
     *
     * @author A.Ushakov
     */
    private class BeepAdapter extends BaseAdapter {

        private final int size;
        private final String[] data;

        public BeepAdapter(Context context, String[] data) {
            this.data = data;
            size = data.length;
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public Object getItem(int position) {
            if (position < 0 || position > size) {
                Logger.info(TAG, "Incorect item index");
                return null;
            }
            return data[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            View view = convertView;

            if (view == null) {
                view = getLayoutInflater().inflate(android.R.layout.simple_list_item_activated_1, null);
                viewHolder = new ViewHolder();
                viewHolder.beepTitle = (TextView) view;
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            String beepName = (String) getItem(position);
            viewHolder.beepTitle.setText(beepName);

            return view;
        }
    }

    static class ViewHolder {
        TextView beepTitle;
    }
}
