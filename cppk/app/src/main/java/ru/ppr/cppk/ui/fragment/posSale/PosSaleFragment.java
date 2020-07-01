package ru.ppr.cppk.ui.fragment.posSale;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;

import ru.ppr.core.ui.helper.FontInjector;
import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.cppk.ui.widget.LoggableViewFlipper;
import ru.ppr.logger.Logger;

/**
 * Экран оплаты банковской картой.
 *
 * @author Aleksandr Brazhkin
 */
public class PosSaleFragment extends LegacyMvpFragment implements PosSaleView, FragmentOnBackPressed {

    private static final String TAG = Logger.makeLogTag(PosSaleFragment.class);
    public static final String FRAGMENT_TAG = PosSaleFragment.class.getSimpleName();

    public static PosSaleFragment newInstance() {
        return new PosSaleFragment();
    }

    /**
     * Di
     */
    private final PosSaleDi di = new PosSaleDi(di());
    private InteractionListener mInteractionListener;
    private Screen screen;
    //region Other
    private PosSalePresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pos_sale, container, false);
        screen = new Screen(view);
        return view;
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(PosSalePresenter::new, PosSalePresenter.class);
    }

    public void initialize(BigDecimal amount) {
        presenter.bindInteractionListener(posSaleInteractionListener);
        presenter.initialize(amount, di.uiThread(),
                di.posManager(),
                di.localDaoSession(),
                di.printerManager(),
                Dagger.appComponent().ticketTapeChecker()
        );
    }

    @Override
    public void showConnectingState(long timeout) {
        screen.showPosTerminalConnecting(timeout);
    }

    @Override
    public void showConnectionTimeoutState() {
        screen.showPosTerminalNotConnected();
    }

    @Override
    public void showConnectedState() {
        screen.showPosTerminalConnectedSale();
    }

    @Override
    public void showPrintingFirstSlip(boolean repeat) {
        if (repeat) {
            screen.showPosTerminalPrintingSlipSaleFirstWithoutTopWriting();
        } else {
            screen.showPosTerminalPrintingSlipSaleFirstWithTopWriting();
        }
    }

    @Override
    public void showPrintFirstSlipFailState() {
        screen.showPosTerminalFailSlipSaleFirst();
    }

    @Override
    public void showPrintFirstSlipSuccessState() {
        screen.showPosTerminalSuccessSlipSaleFirst();
    }

    @Override
    public void showPrintingSecondSlip() {
        screen.showPosTerminalPrintingSlipSaleSecond();
    }

    @Override
    public void showPrintSecondSlipFailState() {
        screen.showPosTerminalFailSlipSaleSecond();
    }

    @Override
    public void showPrintSecondSlipSuccessState() {
        screen.showPosTerminalSuccessSlipSaleSecond();
    }

    @Override
    public void showOperationRejectedWithoutSlip(String bankResponse) {
        screen.showPosTerminalOperationRejectedNoPrint(bankResponse);
    }

    @Override
    public void showPrintingRejectSlipState(String bankResponse) {
        screen.showPosTerminalPrintingSlipOperationRejectedFirst(bankResponse);
    }

    @Override
    public void showPrintRejectSlipFailState() {
        screen.showPosTerminalFailSlipOperationRejectedFirst();
    }

    @Override
    public void showPrintRejectSlipSuccessState() {
        screen.showPosTerminalSuccessSlipOperationRejectedFirst();
    }

    @Override
    public void showCancelConfirmationDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.dialog_cancel_message),
                getString(R.string.dialog_cancel_ok),
                getString(R.string.dialog_cancel_nope),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            presenter.onCancelTransactionConfirmClicked();
        });
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

    private class Screen {

        private static final int CUSTOM_POS_TERMINAL_CONNECTING = 0;
        private static final int WRITING = 1;
        private static final int WRITING_BUTTON = 2;
        private static final int WRITING_BUTTONS_2_LONG = 3;
        private static final int WRITINGS_2 = 4;
        private static final int WRITINGS_2_BUTTON = 5;
        private static final int WRITINGS_2_BUTTONS_2_LONG = 6;
        private static final int WRITINGS_2_BUTTONS_3_LONG = 7;

        LoggableViewFlipper layout;

        TextView writing_w;

        ImageView writing_button_icon;
        TextView writing_button_w;
        Button writing_button_b;

        ImageView writing_buttons_2_long_icon;
        TextView writing_buttons_2_long_w;
        Button writing_buttons_2_long_b_top;
        Button writing_buttons_2_long_b_bottom;

        TextView writings_2_w_top;
        TextView writings_2_w_bottom;

        TextView writings_2_button_w_top;
        TextView writings_2_button_w_bottom;
        Button writings_2_button_b;

        ImageView writings_2_buttons_2_long_icon;
        TextView writings_2_buttons_2_long_w_top;
        TextView writings_2_buttons_2_long_w_bottom;
        Button writings_2_buttons_2_long_b_top;
        Button writings_2_buttons_2_long_b_bottom;

        ImageView writings_2_buttons_3_long_icon;
        TextView writings_2_buttons_3_long_w_top;
        TextView writings_2_buttons_3_long_w_bottom;
        Button writings_2_buttons_3_long_b_top;
        Button writings_2_buttons_3_long_b_mid;
        Button writings_2_buttons_3_long_b_bottom;

        TextView custom_pos_terminal_connecting_timer;

        Screen(@NonNull View screen) {
            layout = (LoggableViewFlipper) screen.findViewById(R.id.layout);
            layout.setConcreteTag(TAG);

            writing_w = (TextView) screen.findViewById(R.id.writing_w);

            writing_button_icon = (ImageView) screen.findViewById(R.id.writing_button_icon);
            writing_button_w = (TextView) screen.findViewById(R.id.writing_button_w);
            writing_button_b = (Button) screen.findViewById(R.id.writing_button_b);

            writing_buttons_2_long_icon = (ImageView) screen.findViewById(R.id.writing_buttons_2_long_icon);
            writing_buttons_2_long_w = (TextView) screen.findViewById(R.id.writing_buttons_2_long_w);
            writing_buttons_2_long_b_top = (Button) screen.findViewById(R.id.writing_buttons_2_long_b_top);
            writing_buttons_2_long_b_bottom = (Button) screen.findViewById(R.id.writing_buttons_2_long_b_bottom);

            writings_2_w_top = (TextView) screen.findViewById(R.id.writings_2_w_top);
            writings_2_w_bottom = (TextView) screen.findViewById(R.id.writings_2_w_bottom);

            writings_2_button_w_top = (TextView) screen.findViewById(R.id.writings_2_button_w_top);
            writings_2_button_w_bottom = (TextView) screen.findViewById(R.id.writings_2_button_w_bottom);
            writings_2_button_b = (Button) screen.findViewById(R.id.writings_2_button_b);

            writings_2_buttons_2_long_icon = (ImageView) screen.findViewById(R.id.writings_2_buttons_2_long_icon);
            writings_2_buttons_2_long_w_top = (TextView) screen.findViewById(R.id.writings_2_buttons_2_long_w_top);
            writings_2_buttons_2_long_w_bottom = (TextView) screen.findViewById(R.id.writings_2_buttons_2_long_w_bottom);
            writings_2_buttons_2_long_b_top = (Button) screen.findViewById(R.id.writings_2_buttons_2_long_b_top);
            writings_2_buttons_2_long_b_bottom = (Button) screen.findViewById(R.id.writings_2_buttons_2_long_b_bottom);

            writings_2_buttons_3_long_icon = (ImageView) screen.findViewById(R.id.writings_2_buttons_3_long_icon);
            writings_2_buttons_3_long_w_top = (TextView) screen.findViewById(R.id.writings_2_buttons_3_long_w_top);
            writings_2_buttons_3_long_w_bottom = (TextView) screen.findViewById(R.id.writings_2_buttons_3_long_w_bottom);
            writings_2_buttons_3_long_b_top = (Button) screen.findViewById(R.id.writings_2_buttons_3_long_b_top);
            writings_2_buttons_3_long_b_mid = (Button) screen.findViewById(R.id.writings_2_buttons_3_long_b_mid);
            writings_2_buttons_3_long_b_bottom = (Button) screen.findViewById(R.id.writings_2_buttons_3_long_b_bottom);

            custom_pos_terminal_connecting_timer = (TextView) screen.findViewById(R.id.custom_pos_terminal_connecting_timer);
        }

        void showPosTerminalConnecting(long timeout) {
            Logger.info(TAG, "showPosTerminalConnecting, timeout = " + timeout);
            custom_pos_terminal_connecting_timer.setText(String.valueOf(timeout / 1000));
            layout.setDisplayedChild(CUSTOM_POS_TERMINAL_CONNECTING);
        }

        void showPosTerminalConnectedSale() {

            writings_2_w_top.setText(R.string.terminal_connected_sale);
            FontInjector.injectFont(writings_2_w_top, FontInjector.ROBOTO_BOLD);
            writings_2_w_bottom.setText(R.string.terminal_see_terminal);

            Logger.info(TAG, "showPosTerminalConnectedSale() - " + writings_2_w_top.getText() + " - " + writings_2_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2);
        }

        void showPosTerminalNotConnected() {
            writings_2_button_w_top.setText(R.string.terminal_no_connection);
            writings_2_button_w_bottom.setText(R.string.terminal_reboot);
            writings_2_button_b.setText(R.string.dialog_cancel);
            writings_2_button_b.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalNotConnected() - нажали кнопку: " + writings_2_button_b.getText());
                presenter.onNotConnectedCancelOperationClicked();
            });

            Logger.info(TAG, "showPosTerminalNotConnected() - " + writings_2_button_w_top.getText() + " - " + writings_2_button_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2_BUTTON);
        }

        void showPosTerminalPrintingSlipSaleFirstWithTopWriting() {
            writings_2_w_top.setText(R.string.terminal_sale_success);
            FontInjector.injectFont(writings_2_w_top, FontInjector.ROBOTO_REGULAR);
            writings_2_w_bottom.setText(R.string.terminal_printing_slip);

            Logger.info(TAG, "showPosTerminalPrintingSlipSaleFirstWithTopWriting() - " + writings_2_w_top.getText() + " - " + writings_2_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2);
        }

        void showPosTerminalPrintingSlipSaleFirstWithoutTopWriting() {
            writing_w.setText(R.string.terminal_printing_slip);

            Logger.info(TAG, "showPosTerminalPrintingSlipSaleFirstWithoutTopWriting() - " + writing_w.getText());

            layout.setDisplayedChild(WRITING);
        }

        void showPosTerminalPrintingSlipSaleSecond() {
            writing_w.setText(R.string.terminal_printing_slip);

            Logger.info(TAG, "showPosTerminalPrintingSlipSaleSecond() - " + writing_w.getText());

            layout.setDisplayedChild(WRITING);
        }

        void showPosTerminalSuccessSlipSaleFirst() {
            writings_2_buttons_3_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_dont_know));

            writings_2_buttons_3_long_w_top.setText(R.string.terminal_is_slip_n_printed);
            writings_2_buttons_3_long_w_bottom.setText(R.string.terminal_cut_slip);

            Logger.info(TAG, "showPosTerminalSuccessSlipSaleFirst() - " + writings_2_buttons_3_long_w_top.getText() + " - " + writings_2_buttons_3_long_w_bottom.getText());

            writings_2_buttons_3_long_b_top.setText(R.string.terminal_cancel);
            writings_2_buttons_3_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleFirst() - нажали на кнопку: " + writings_2_buttons_3_long_b_top.getText());
                presenter.onCancelTransactionClicked();
            });
            writings_2_buttons_3_long_b_mid.setText(R.string.terminal_retry);
            writings_2_buttons_3_long_b_mid.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleFirst() - нажали на кнопку: " + writings_2_buttons_3_long_b_mid.getText());
                presenter.onRepeatPrintFirstSlipClicked();
            });
            writings_2_buttons_3_long_b_bottom.setText(R.string.terminal_next);
            writings_2_buttons_3_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleFirst() - нажали на кнопку: " + writings_2_buttons_3_long_b_bottom.getText());
                presenter.onAfterFirstSlipContinueBtnClicked();
            });

            layout.setDisplayedChild(WRITINGS_2_BUTTONS_3_LONG);
        }

        void showPosTerminalFailSlipSaleFirst() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.terminal_printing_slip_failed);

            Logger.info(TAG, "showPosTerminalFailSlipSaleFirst() - " + writing_buttons_2_long_w.getText());

            writing_buttons_2_long_b_top.setText(R.string.terminal_cancel);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipSaleFirst() - нажали на кнопку: " + writing_buttons_2_long_b_top.getText());
                presenter.onCancelTransactionClicked();
            });
            writing_buttons_2_long_b_bottom.setText(R.string.terminal_retry);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipSaleFirst() - нажали на кнопку: " + writing_buttons_2_long_b_bottom.getText());
                presenter.onRepeatPrintFirstSlipClicked();
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showPosTerminalSuccessSlipSaleSecond() {
            writings_2_buttons_3_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_dont_know));

            writings_2_buttons_3_long_w_top.setText(R.string.terminal_is_slip_n_printed);
            writings_2_buttons_3_long_w_bottom.setText(R.string.terminal_cut_slip);

            Logger.info(TAG, "showPosTerminalSuccessSlipSaleSecond() - " + writings_2_buttons_3_long_w_top.getText() + " - " + writings_2_buttons_3_long_w_bottom.getText());

            writings_2_buttons_3_long_b_top.setText(R.string.terminal_cancel);
            writings_2_buttons_3_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleSecond() - нажали на кнопку: " + writings_2_buttons_3_long_b_top.getText());
                presenter.onCancelTransactionClicked();
            });
            writings_2_buttons_3_long_b_mid.setText(R.string.terminal_retry);
            writings_2_buttons_3_long_b_mid.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleSecond() - нажали на кнопку: " + writings_2_buttons_3_long_b_mid.getText());
                presenter.onRepeatPrintSecondSlipClicked();
            });
            writings_2_buttons_3_long_b_bottom.setText(R.string.terminal_next);
            writings_2_buttons_3_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleSecond() - нажали на кнопку: " + writings_2_buttons_3_long_b_bottom.getText());
                presenter.onAfterSecondSlipContinueBtnClicked();
            });

            layout.setDisplayedChild(WRITINGS_2_BUTTONS_3_LONG);
        }

        void showPosTerminalFailSlipSaleSecond() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.terminal_printing_slip_failed);

            Logger.info(TAG, "showPosTerminalFailSlipSaleSecond() - " + writing_buttons_2_long_w.getText());

            writing_buttons_2_long_b_top.setText(R.string.terminal_cancel);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipSaleSecond() - нажали на кнопку: " + writing_buttons_2_long_b_top.getText());
                presenter.onCancelTransactionClicked();
            });
            writing_buttons_2_long_b_bottom.setText(R.string.terminal_retry);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipSaleSecond() - нажали на кнопку: " + writing_buttons_2_long_b_bottom.getText());
                presenter.onRepeatPrintSecondSlipClicked();
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showPosTerminalOperationRejectedNoPrint(@Nullable String bankResponse) {

            writing_button_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            if (TextUtils.isEmpty(bankResponse))
                writing_button_w.setText(R.string.terminal_operation_rejected_reason_unknown);
            else
                writing_button_w.setText(getString(R.string.terminal_operation_rejected, bankResponse));

            Logger.info(TAG, "showPosTerminalOperationRejectedNoPrint() - " + writing_button_w.getText());

            writing_button_b.setText(R.string.terminal_next);
            writing_button_b.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalOperationRejectedNoPrint() - нажали на кнопку: " + writing_button_b.getText());
                presenter.onNoRejectSlipCancelOperationClicked();
            });

            layout.setDisplayedChild(WRITING_BUTTON);
        }


        void showPosTerminalPrintingSlipOperationRejectedFirst(String bankResponse) {
            writings_2_w_top.setText(getString(R.string.terminal_operation_rejected, bankResponse));
            FontInjector.injectFont(writings_2_w_top, FontInjector.ROBOTO_BOLD);
            writings_2_w_bottom.setText(R.string.terminal_printing_slip);

            Logger.info(TAG, "showPosTerminalPrintingSlipOperationRejectedFirst() - " + writings_2_w_top.getText() + " - " + writings_2_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2);
        }

        void showPosTerminalSuccessSlipOperationRejectedFirst() {
            writings_2_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_dont_know));

            writings_2_buttons_2_long_w_top.setText(R.string.terminal_is_slip_n_printed);
            writings_2_buttons_2_long_w_bottom.setText(R.string.terminal_cut_slip);

            Logger.info(TAG, "showPosTerminalSuccessSlipOperationRejectedFirst() - " + writings_2_buttons_2_long_w_top.getText() + " - " + writings_2_buttons_2_long_w_bottom.getText());

            writings_2_buttons_2_long_b_top.setText(R.string.terminal_retry);
            writings_2_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipOperationRejectedFirst() - нажали на кнопку: " + writings_2_buttons_2_long_b_top.getText());
                presenter.onRepeatPrintRejectSlipClicked();
            });
            writings_2_buttons_2_long_b_bottom.setText(R.string.terminal_next);
            writings_2_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipOperationRejectedFirst() - нажали на кнопку: " + writings_2_buttons_2_long_b_bottom.getText());
                presenter.onAfterRejectSlipContinueBtnClicked();
            });

            layout.setDisplayedChild(WRITINGS_2_BUTTONS_2_LONG);
        }

        void showPosTerminalFailSlipOperationRejectedFirst() {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.terminal_printing_slip_failed);

            Logger.info(TAG, "showPosTerminalFailSlipOperationRejectedFirst() - " + writing_buttons_2_long_w.getText());

            writing_buttons_2_long_b_top.setText(R.string.terminal_retry);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipOperationRejectedFirst() - нажали на кнопку: " + writing_buttons_2_long_b_top.getText());
                presenter.onRepeatPrintRejectSlipClicked();
            });
            writing_buttons_2_long_b_bottom.setText(R.string.terminal_next);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipOperationRejectedFirst() - нажали на кнопку: " + writing_buttons_2_long_b_bottom.getText());
                presenter.onAfterRejectSlipContinueBtnClicked();
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

    }

    private PosSalePresenter.InteractionListener posSaleInteractionListener = new PosSalePresenter.InteractionListener() {

        @Override
        public void navigateToTicketTapeIsNotSet() {
            Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());
        }

        @Override
        public void onOperationFailed(long bankTransactionEventId) {
            mInteractionListener.onOperationFailed(bankTransactionEventId);
        }

        @Override
        public void onOperationCompleted(long bankTransactionEventId) {
            mInteractionListener.onOperationCompleted(bankTransactionEventId);
        }

        @Override
        public void onCancelTransactionRequired(long bankTransactionEventId) {
            mInteractionListener.onCancelTransactionRequired(bankTransactionEventId);
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {

        void onOperationFailed(long bankTransactionEventId);

        void onOperationCompleted(long bankTransactionEventId);

        void onCancelTransactionRequired(long bankTransactionEventId);
    }
}
