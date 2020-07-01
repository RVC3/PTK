package ru.ppr.chit.ui.widget.networkstatus;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.core.ui.mvp.MvpDelegate;

/**
 * Отображает статус сети
 *
 * @author Dmitry Nevolin
 */
public class NetworkStatusAndroidView extends FrameLayout implements NetworkStatusView {

    private MvpDelegate mvpDelegate;
    private NetworkStatusComponent component;
    private NetworkStatusPresenter presenter;

    private ImageView networkStatusImage;
    private int currentImageId = -1;

    public NetworkStatusAndroidView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public NetworkStatusAndroidView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public NetworkStatusAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NetworkStatusAndroidView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.widget_network_status, this);

        if (isInEditMode()) {
            return;
        }

        mvpDelegate = new MvpDelegate(Dagger.appComponent().mvpProcessor(), this);
        component = DaggerNetworkStatusComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .build();
        component.inject(this);

        networkStatusImage = (ImageView) findViewById(R.id.wifi_status_image);
    }

    public void init(MvpDelegate parent, String id) {
        mvpDelegate.init(parent, id);
        presenter = mvpDelegate.getPresenter(component::networkStatusPresenter, NetworkStatusPresenter.class);
        presenter.initialize();
    }

    @Override
    public void setNetworkAvailable(boolean value) {
        int imageId = value ? R.drawable.wifi_enable_status : R.drawable.wifi_disable_status;
        synchronized (this) {
            if (imageId != currentImageId) {
                currentImageId = imageId;
                networkStatusImage.setImageResource(currentImageId);
            }
        }
    }
}
