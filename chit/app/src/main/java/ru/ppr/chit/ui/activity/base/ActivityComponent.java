package ru.ppr.chit.ui.activity.base;

import android.app.Activity;

import ru.ppr.chit.ui.activity.ActivityNavigator;

/**
 * @author Aleksandr Brazhkin
 */
public interface ActivityComponent {

    Activity activity();

    ActivityNavigator activityNavigator();

}
