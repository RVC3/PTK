package ru.ppr.cppk.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import ru.ppr.logger.Logger;

/**
 * Created by Dmitry Nevolin on 08.04.2016.
 */
public class LoggableViewFlipper extends ViewFlipper {

    private String concreteTag = "";

    public LoggableViewFlipper(Context context) {
        super(context);
    }

    public LoggableViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setDisplayedChild(int whichChild) {
        log(buildMessage(getDisplayedChild() + " hide"));
        log(buildMessage(whichChild + " show"));

        super.setDisplayedChild(whichChild);
    }

    public void setConcreteTag(String concreteTag) {
        this.concreteTag = concreteTag;
    }

    private String buildMessage(String body) {
        return concreteTag + "| " + body;
    }

    private void log(String message) {
        Logger.info(getContext().getClass(), message);
    }

}
