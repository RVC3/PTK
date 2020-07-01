package ru.ppr.chit.ui.widget.clock;

import java.util.Date;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface ClockView extends MvpView {

    void setDate(Date date);

}
