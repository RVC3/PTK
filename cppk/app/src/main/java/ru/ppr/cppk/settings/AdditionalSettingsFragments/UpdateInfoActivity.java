package ru.ppr.cppk.settings.AdditionalSettingsFragments;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ru.ppr.cppk.R;
import ru.ppr.cppk.db.nsi.NsiDbOperations;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.systembar.SystemBarActivity;

/**
 * Класс выводит информацию о версиях: ПО, НСИ, Базы безопасности, Стоп-листов,
 * Пакетов открытых ключей.
 */
public class UpdateInfoActivity extends SystemBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_database_fragment);

        TextView textView = (TextView) findViewById(R.id.current_version_po);
        String version = null;
        try {
            version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            version = "unknown";
        }
        textView.setText(version);

        int nsiVersion = Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId();

        // utc время
        String securityVersion = getSecurityDaoSession().getSecurityDataVersionDao().getSecurityVersion();
        String smartCardStopListVersion = getSecurityDaoSession().getSecurityStopListVersionDao().getSmartCardStoplistItemVersion();
        String ticketStopListItemVersion = getSecurityDaoSession().getSecurityStopListVersionDao().getTicketStopListItemVersion();
        String ticketWhitelistItemVersion = getSecurityDaoSession().getSecurityStopListVersionDao().getTicketWhiteListItemVersion();

        // локальное время
        String openKeysPackageVersion = NsiDbOperations.getOpenKeysPackageVersionString(getApplicationContext());

        textView = (TextView) findViewById(R.id.current_version_nsi);
        textView.setText("" + nsiVersion);

        textView = (TextView) findViewById(R.id.current_version_pok);
        textView.setText(openKeysPackageVersion);

        textView = (TextView) findViewById(R.id.current_version_security);
        textView.setText(getLocalTimeString(securityVersion));

        textView = (TextView) findViewById(R.id.current_version_stoplist);
        textView.setText(getLocalTimeString(smartCardStopListVersion));

        textView = (TextView) findViewById(R.id.current_version_stoplist_pd);
        textView.setText(getLocalTimeString(ticketStopListItemVersion));

        textView = (TextView) findViewById(R.id.current_version_whitelist_pd);
        textView.setText(getLocalTimeString(ticketWhitelistItemVersion));

    }

    private String getLocalTimeString(String sUtcDate) {
        String out = getResources().getString(R.string.getVersionError);
        if (sUtcDate == null)
            return out;
        try {
            if (sUtcDate.indexOf(".") == -1)
                sUtcDate = sUtcDate + ".000";
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            utc.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date((utc.parse(sUtcDate)).getTime());
            SimpleDateFormat local = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            out = local.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return out;
    }

}
