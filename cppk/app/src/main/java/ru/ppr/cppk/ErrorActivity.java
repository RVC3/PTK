package ru.ppr.cppk;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.androidquery.AQuery;

import ru.ppr.cppk.systembar.SystemBarActivity;

/**
 * Окно вывода сообщения об ошибке чтения билетов сюда нужно передавать type
 * ошибки
 */
public class ErrorActivity extends SystemBarActivity {

    public static final String TYPE_ERROR = "type";
    public static final String LAST_ACTIVITY = "last";

    /**
     * Невалидные данные на крте
     */
    public static final int ERROR_TYPE_BAD_DATA = 0;

    /**
     * Неизвестная ошибка
     */
    public static final int ERROR_TYPE_UNKNOWN = 4;

    /**
     * Билет для аннулирвоания некорректен
     */
    public static final int PD_FOR_REPAIL_UNCORRECT = 5;

    private TextView messageView;
    private View circleImage;
    private int codePreviousActivity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        AQuery aQuery = new AQuery(this);

        messageView = aQuery.id(R.id.error_fragment_message).getTextView();
        circleImage = aQuery.id(R.id.circleImage).getView();
        aQuery.id(R.id.btnAgain).clicked(againClickListener);
        aQuery.id(R.id.ok).clicked(backClickListener);

        Bundle bundle = getIntent().getExtras();
        codePreviousActivity = bundle.getInt(LAST_ACTIVITY, 0);
        int errorCode = bundle.getInt(TYPE_ERROR, ERROR_TYPE_BAD_DATA);

        switch (errorCode) {
            case ERROR_TYPE_BAD_DATA:
                messageView.setText(R.string.error_activity_ticket_not_found);
                circleImage.setVisibility(View.VISIBLE);
                break;

            case ERROR_TYPE_UNKNOWN:
                messageView.setText(R.string.error_activity_read_barcode_fail_title_2);
                circleImage.setVisibility(View.VISIBLE);
                break;

            case PD_FOR_REPAIL_UNCORRECT:
                messageView.setText(R.string.error_activity_repeal_incorrect_pd);
                circleImage.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    private OnClickListener backClickListener = v -> onBackPressed();

    private OnClickListener againClickListener = v -> {
        switch (codePreviousActivity) {
            case GlobalConstants.READ_BARCODE_ACTIVITY:
                break;

            case GlobalConstants.READ_RFID_ACTIVITY:
                break;

            default:
                finish();
                break;
        }

    };

}
