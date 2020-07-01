package ru.ppr.cppk.ui.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.core.ui.helper.FontInjector;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.cppk.model.SaleType;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.widget.LoggableViewFlipper;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.logger.Logger;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class FragmentWorkWithPosTerminal extends FragmentParent implements FragmentOnBackPressed, OnCancelBTOperationDialogClickListener {

    public interface OnInteractionListener {
        void onActionSuccess(@Nullable SaleType saleType);

        void onActionFail(@Nullable SaleType saleType);

        void onStartNewSellProcess();

        void onCancelSellProcess();
    }

    private enum OperationType {
        SALE,
        CANCEL
    }

    public static final String TAG = Logger.makeLogTag(FragmentWorkWithPosTerminal.class);

    private static final String EXTRA_PARAM = "EXTRA_PARAM";
    private static final String SALE_TYPE = "SALE_TYPE";
    private static final String OPERATION_TYPE = "OPERATION_TYPE";

    private static final int SALE_TYPE_USELESS = -1;

    private boolean allowPressBack;
    private OnInteractionListener onInteractionListener;
    private SaleType saleType;
    private OperationType operationType;

    private Holder<PrivateSettings> privateSettingsHolder;

    private Screen screen;

    public static Fragment newInstance(@NonNull SaleType saleType, String price) {
        Fragment fragment = new FragmentWorkWithPosTerminal();
        Bundle bundle = new Bundle();

        bundle.putInt(SALE_TYPE, saleType.ordinal());
        bundle.putInt(OPERATION_TYPE, OperationType.SALE.ordinal());
        bundle.putString(EXTRA_PARAM, price);

        fragment.setArguments(bundle);

        return fragment;
    }

    public static Fragment newInstance(int transactionId) {
        Fragment fragment = new FragmentWorkWithPosTerminal();
        Bundle bundle = new Bundle();

        bundle.putInt(SALE_TYPE, SALE_TYPE_USELESS);
        bundle.putInt(OPERATION_TYPE, OperationType.CANCEL.ordinal());
        bundle.putString(EXTRA_PARAM, String.valueOf(transactionId));

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnInteractionListener)
            onInteractionListener = (OnInteractionListener) activity;
        else
            throw new IllegalStateException(activity.toString() + " must implement " + OnInteractionListener.class.getName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View screen = inflater.inflate(R.layout.fragment_work_with_pos_terminal, container, false);

        screen.setFocusableInTouchMode(true);
        screen.requestFocus();
        screen.setOnKeyListener((v, keyCode, event) -> !allowPressBack && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BACKSLASH));

        this.screen = new Screen(screen);
        this.screen.showPosTerminalConnecting();

        Bundle bundle = getArguments();

        if (bundle != null) {
            int saleType = bundle.getInt(SALE_TYPE, Integer.MIN_VALUE);
            int operationType = bundle.getInt(OPERATION_TYPE, Integer.MIN_VALUE);
            BigDecimal param = new BigDecimal(bundle.getString(EXTRA_PARAM, BigDecimal.ZERO.toPlainString()));

            if (saleType == Integer.MIN_VALUE || operationType == Integer.MIN_VALUE)
                if (onInteractionListener != null)
                    onInteractionListener.onActionFail(null);

            if (saleType != SALE_TYPE_USELESS)
                this.saleType = saleType == SaleType.PRINT.ordinal() ? SaleType.PRINT : SaleType.SMART_CARD;

            this.operationType = operationType == OperationType.SALE.ordinal() ? OperationType.SALE : OperationType.CANCEL;

            if (this.operationType == OperationType.SALE) {
                Logger.info(TAG, "onCreateView() - Operation - SALE, price = " + param.toPlainString());

                Globals.getInstance().getPosManager().sale(param, new PosManager.AbstractFinancialTransactionListener() {
                    @Override
                    public void onTickBeforeTimeout(long value) {
                        getActivity().runOnUiThread(() -> {
                            FragmentWorkWithPosTerminal.this.screen.customUpdateTimer(String.valueOf(value / 1000));
                        });
                    }

                    @Override
                    public void onConnectionTimeout() {
                        Logger.info(TAG, "onConnectionTimeout");

                        getActivity().runOnUiThread(() -> {
                            FragmentWorkWithPosTerminal.this.screen.showPosTerminalNotConnected();
                        });
                    }

                    @Override
                    public void onConnected() {
                        Logger.info(TAG, "onConnected");

                        getActivity().runOnUiThread(() -> {
                            FragmentWorkWithPosTerminal.this.screen.showPosTerminalConnectedSale();
                        });
                    }

                    @Override
                    public void onResult(@NonNull PosOperationResult<FinancialTransactionResult> operationResult) {
                        FinancialTransactionResult result = operationResult.getTransactionResult();
                        Logger.info(TAG, "onResult, has result: " + String.valueOf(result != null));

                        getActivity().runOnUiThread(() -> {
                            if (result != null && result.isApproved())
                                printSlipSaleFirst(result.getReceipt(), false);
                            else
                                showOperationRejectedScreen(result);
                        });
                    }
                });
            } else {
                Logger.info(TAG, "onCreateView() - Operation - CANCEL, transactionId = " + param.intValue());

                Globals.getInstance().getPosManager().cancelTransaction(param.intValue(), new PosManager.AbstractFinancialTransactionListener() {

                    @Override
                    public void onTickBeforeTimeout(long value) {
                        getActivity().runOnUiThread(() -> {
                            FragmentWorkWithPosTerminal.this.screen.customUpdateTimer(String.valueOf(value / 1000));
                        });
                    }

                    @Override
                    public void onConnectionTimeout() {
                        Logger.info(TAG, "onConnectionTimeout");

                        getActivity().runOnUiThread(() -> {
                            FragmentWorkWithPosTerminal.this.screen.showPosTerminalNotConnected();
                        });
                    }

                    @Override
                    public void onConnected() {
                        Logger.info(TAG, "onConnected");

                        getActivity().runOnUiThread(() -> {
                            FragmentWorkWithPosTerminal.this.screen.showPosTerminalConnectedCancel();
                        });
                    }

                    @Override
                    public void onResult(@NonNull PosOperationResult<FinancialTransactionResult> operationResult) {
                        FinancialTransactionResult result = operationResult.getTransactionResult();
                        Logger.info(TAG, "onResult, has result: " + String.valueOf(result != null));

                        getActivity().runOnUiThread(() -> {
                            if (result != null && result.isApproved())
                                printSlipCancelFirst(result.getReceipt(), false);
                            else
                                showOperationRejectedScreen(result);
                        });
                    }
                });
            }
        } else {
            if (onInteractionListener != null)
                onInteractionListener.onActionFail(saleType);
        }

        return screen;
    }

    @Override
    public void onDetach() {
        onInteractionListener = null;

        super.onDetach();
    }

    private void printSlipSaleFirst(final List<String> receipt, final boolean removeTopWriting) {
        allowPressBack = false;
        getActivity().runOnUiThread(() -> {
            if (removeTopWriting)
                screen.showPosTerminalPrintingSlipSaleFirstWithoutTopWriting();
            else
                screen.showPosTerminalPrintingSlipSaleFirstWithTopWriting();
        });

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                    params.tplParams.slipLines = receipt;

                    return params;
                }))
                .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   screen.showPosTerminalSuccessSlipSaleFirst(receipt);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                                       Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());

                                       Single.create(subscriber -> {
                                           try {
                                               Thread.sleep(200);
                                           } catch (InterruptedException ex) {
                                               ex.printStackTrace();
                                           }

                                           subscriber.onSuccess(Object.class);
                                       })
                                               .subscribeOn(SchedulersCPPK.background())
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .subscribe(o -> {
                                                   screen.showPosTerminalFailSlipSaleFirst(receipt);
                                               });
                                   } else {
                                       screen.showPosTerminalFailSlipSaleFirst(receipt);
                                   }
                               }

                               @Override
                               public void onNext(Object aVoid) {
                               }
                           }
                );
    }

    private void printSlipSaleSecond(final List<String> receipt) {
        allowPressBack = false;
        getActivity().runOnUiThread(screen::showPosTerminalPrintingSlipSaleSecond);

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                    params.tplParams.slipLines = receipt;

                    return params;
                }))
                .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   screen.showPosTerminalSuccessSlipSaleSecond(receipt);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                                       Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());

                                       Single.create(subscriber -> {
                                           try {
                                               Thread.sleep(200);
                                           } catch (InterruptedException ex) {
                                               ex.printStackTrace();
                                           }

                                           subscriber.onSuccess(Object.class);
                                       })
                                               .subscribeOn(SchedulersCPPK.background())
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .subscribe(o -> {
                                                   screen.showPosTerminalFailSlipSaleSecond(receipt);
                                               });
                                   } else {
                                       screen.showPosTerminalFailSlipSaleSecond(receipt);
                                   }
                               }

                               @Override
                               public void onNext(Object aVoid) {
                               }
                           }
                );
    }

    private void printSlipCancelFirst(final List<String> receipt, final boolean removeTopWriting) {
        allowPressBack = false;
        getActivity().runOnUiThread(() -> {
            if (removeTopWriting)
                screen.showPosTerminalPrintingSlipCancelFirstWithoutTopWriting();
            else
                screen.showPosTerminalPrintingSlipCancelFirstWithTopWriting();
        });

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                    params.tplParams.slipLines = receipt;

                    return params;
                }))
                .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   screen.showPosTerminalSuccessSlipCancelFirst(receipt);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                                       Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());

                                       Single.create(subscriber -> {
                                           try {
                                               Thread.sleep(200);
                                           } catch (InterruptedException ex) {
                                               ex.printStackTrace();
                                           }

                                           subscriber.onSuccess(Object.class);
                                       })
                                               .subscribeOn(SchedulersCPPK.background())
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .subscribe(o -> {
                                                   screen.showPosTerminalFailSlipCancelFirst(receipt);
                                               });
                                   } else {
                                       screen.showPosTerminalFailSlipCancelFirst(receipt);
                                   }
                               }

                               @Override
                               public void onNext(Object object) {
                               }
                           }
                );
    }

    private void printSlipOperationRejectedFirst(final FinancialTransactionResult financialTransactionResult) {
        allowPressBack = false;
        getActivity().runOnUiThread(() -> screen.showPosTerminalPrintingSlipOperationRejectedFirst(financialTransactionResult));

        final List<String> receipt = financialTransactionResult.getReceipt();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                    params.tplParams.slipLines = receipt;

                    return params;
                }))
                .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   screen.showPosTerminalSuccessSlipOperationRejectedFirst(financialTransactionResult);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                                       Navigator.navigateToActivityTicketTapeIsNotSet(getActivity());

                                       Single.create(subscriber -> {
                                           try {
                                               Thread.sleep(200);
                                           } catch (InterruptedException ex) {
                                               ex.printStackTrace();
                                           }

                                           subscriber.onSuccess(Object.class);
                                       })
                                               .subscribeOn(SchedulersCPPK.background())
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .subscribe(o -> {
                                                   screen.showPosTerminalFailSlipOperationRejectedFirst(financialTransactionResult);
                                               });
                                   } else {
                                       screen.showPosTerminalFailSlipOperationRejectedFirst(financialTransactionResult);
                                   }
                               }

                               @Override
                               public void onNext(Object aVoid) {
                               }
                           }
                );
    }

    void showCancel() {
        CppkDialogFragment.getInstance(null,
                getString(R.string.dialog_cancel_message),
                getString(R.string.dialog_cancel_ok),
                getString(R.string.dialog_cancel_nope),
                CppkDialogFragment.CppkDialogButtonStyle.HORIZONTAL)
                .show(getActivity().getFragmentManager(), null);
    }

    void showCancelOk() {
        getActivity().runOnUiThread(() -> {
            operationType = OperationType.CANCEL;

            screen.showPosTerminalConnecting();
        });


        Globals.getInstance().getPosManager().cancelLastTransaction(new PosManager.AbstractFinancialTransactionListener() {

            @Override
            public void onTickBeforeTimeout(long value) {
                getActivity().runOnUiThread(() -> {
                    FragmentWorkWithPosTerminal.this.screen.customUpdateTimer(String.valueOf(value / 1000));
                });
            }

            @Override
            public void onConnectionTimeout() {
                getActivity().runOnUiThread(() -> {
                    FragmentWorkWithPosTerminal.this.screen.showPosTerminalNotConnected();
                });
            }

            @Override
            public void onConnected() {
                getActivity().runOnUiThread(() -> {
                    FragmentWorkWithPosTerminal.this.screen.showPosTerminalConnectedCancel();
                });
            }

            @Override
            public void onResult(@NonNull PosOperationResult<FinancialTransactionResult> operationResult) {
                getActivity().runOnUiThread(() -> {
                    FinancialTransactionResult result = operationResult.getTransactionResult();
                    if (result != null && result.isApproved())
                        printSlipCancelFirst(result.getReceipt(), false);
                    else
                        showOperationRejectedScreen(result);
                });

            }
        });
    }

    private void showOperationRejectedScreen(@Nullable FinancialTransactionResult financialTransactionResult) {
        if (financialTransactionResult != null) {
            List<String> receipt = financialTransactionResult.getReceipt();
            //раз нам нечего печатать, то ничего и не печатаем...
            if (receipt == null || receipt.isEmpty())
                getActivity().runOnUiThread(() -> screen.showPosTerminalOperationRejectedNoPrint(financialTransactionResult));
            else
                printSlipOperationRejectedFirst(financialTransactionResult);
        } else
            getActivity().runOnUiThread(() -> screen.showPosTerminalOperationRejectedNoPrint(null));
    }

    private void showToastTerminalIsBusy() {
        Globals.getInstance().getToaster().showToast(R.string.terminal_is_busy);
    }

    @Override
    public void onOk(DialogFragment dialog) {
        showCancelOk();

        dialog.dismiss();
    }

    @Override
    public void onNope(DialogFragment dialog) {
        dialog.dismiss();
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

        void showPosTerminalConnecting() {
            custom_pos_terminal_connecting_timer.setText(String.valueOf(Globals.getInstance().getPosManager().getConnectionTimeout() / 1000));
            Logger.info(TAG, "showPosTerminalConnecting() - " + custom_pos_terminal_connecting_timer.getText());
            layout.setDisplayedChild(CUSTOM_POS_TERMINAL_CONNECTING);
        }

        void showPosTerminalConnectedSale() {

            writings_2_w_top.setText(R.string.terminal_connected_sale);
            FontInjector.injectFont(writings_2_w_top, FontInjector.ROBOTO_BOLD);
            writings_2_w_bottom.setText(R.string.terminal_see_terminal);

            Logger.info(TAG, "showPosTerminalConnectedSale() - " + writings_2_w_top.getText() + " - " + writings_2_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2);
        }

        void showPosTerminalConnectedCancel() {
            writings_2_w_top.setText(R.string.terminal_connected_cancel);
            FontInjector.injectFont(writings_2_w_top, FontInjector.ROBOTO_BOLD);
            writings_2_w_bottom.setText(R.string.terminal_see_terminal);

            Logger.info(TAG, "showPosTerminalConnecting() - " + writings_2_w_top.getText() + " - " + writings_2_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2);
        }

        void showPosTerminalNotConnected() {
            writings_2_button_w_top.setText(R.string.terminal_no_connection);
            writings_2_button_w_bottom.setText(R.string.terminal_reboot);
            writings_2_button_b.setText(R.string.dialog_cancel);
            writings_2_button_b.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalNotConnected() - нажали кнопку: " + writings_2_button_b.getText());
                Logger.info(TAG, "getPosManager().forceCloseConnection() from showPosTerminalNotConnected()");

                if (onInteractionListener != null)
                    onInteractionListener.onActionFail(saleType);
            });

            Logger.info(TAG, "showPosTerminalNotConnected() - " + writings_2_button_w_top.getText() + " - " + writings_2_button_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2_BUTTON);
        }

        void customUpdateTimer(String value) {
            Logger.info(TAG, "customUpdateTimer()");

            if (layout.getDisplayedChild() == CUSTOM_POS_TERMINAL_CONNECTING)
                custom_pos_terminal_connecting_timer.setText(value);
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

        void showPosTerminalPrintingSlipCancelFirstWithTopWriting() {
            writings_2_w_top.setText(R.string.terminal_cancel_success);
            FontInjector.injectFont(writings_2_w_top, FontInjector.ROBOTO_REGULAR);
            writings_2_w_bottom.setText(R.string.terminal_printing_slip);

            Logger.info(TAG, "showPosTerminalPrintingSlipCancelFirstWithTopWriting() - " + writings_2_w_top.getText() + " - " + writings_2_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2);
        }

        void showPosTerminalPrintingSlipCancelFirstWithoutTopWriting() {
            writing_w.setText(R.string.terminal_printing_slip);

            Logger.info(TAG, "showPosTerminalPrintingSlipCancelFirstWithoutTopWriting() - " + writing_w.getText());

            layout.setDisplayedChild(WRITING);
        }

        void showPosTerminalSuccessSlipSaleFirst(final List<String> receipt) {
            writings_2_buttons_3_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_dont_know));

            writings_2_buttons_3_long_w_top.setText(R.string.terminal_is_slip_n_printed);
            writings_2_buttons_3_long_w_bottom.setText(R.string.terminal_cut_slip);

            Logger.info(TAG, "showPosTerminalSuccessSlipSaleFirst() - " + writings_2_buttons_3_long_w_top.getText() + " - " + writings_2_buttons_3_long_w_bottom.getText());

            writings_2_buttons_3_long_b_top.setText(R.string.terminal_cancel);
            writings_2_buttons_3_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleFirst() - нажали на кнопку: " + writings_2_buttons_3_long_b_top.getText());
                if (!Globals.getInstance().getPosManager().isReady())
                    showToastTerminalIsBusy();
                else
                    showCancel();
            });
            writings_2_buttons_3_long_b_mid.setText(R.string.terminal_retry);
            writings_2_buttons_3_long_b_mid.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleFirst() - нажали на кнопку: " + writings_2_buttons_3_long_b_mid.getText());
                printSlipSaleFirst(receipt, true);
            });
            writings_2_buttons_3_long_b_bottom.setText(R.string.terminal_next);
            writings_2_buttons_3_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleFirst() - нажали на кнопку: " + writings_2_buttons_3_long_b_bottom.getText());
                printSlipSaleSecond(receipt);
            });

            layout.setDisplayedChild(WRITINGS_2_BUTTONS_3_LONG);
        }

        void showPosTerminalFailSlipSaleFirst(final List<String> receipt) {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.terminal_printing_slip_failed);

            Logger.info(TAG, "showPosTerminalFailSlipSaleFirst() - " + writing_buttons_2_long_w.getText());

            writing_buttons_2_long_b_top.setText(R.string.terminal_cancel);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipSaleFirst() - нажали на кнопку: " + writing_buttons_2_long_b_top.getText());
                if (!Globals.getInstance().getPosManager().isReady())
                    showToastTerminalIsBusy();
                else
                    showCancel();
            });
            writing_buttons_2_long_b_bottom.setText(R.string.terminal_retry);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipSaleFirst() - нажали на кнопку: " + writing_buttons_2_long_b_bottom.getText());
                printSlipSaleFirst(receipt, true);
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showPosTerminalSuccessSlipSaleSecond(final List<String> receipt) {
            writings_2_buttons_3_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_dont_know));

            writings_2_buttons_3_long_w_top.setText(R.string.terminal_is_slip_n_printed);
            writings_2_buttons_3_long_w_bottom.setText(R.string.terminal_cut_slip);

            Logger.info(TAG, "showPosTerminalSuccessSlipSaleSecond() - " + writings_2_buttons_3_long_w_top.getText() + " - " + writings_2_buttons_3_long_w_bottom.getText());

            writings_2_buttons_3_long_b_top.setText(R.string.terminal_cancel);
            writings_2_buttons_3_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleSecond() - нажали на кнопку: " + writings_2_buttons_3_long_b_top.getText());
                if (!Globals.getInstance().getPosManager().isReady())
                    showToastTerminalIsBusy();
                else
                    showCancel();
            });
            writings_2_buttons_3_long_b_mid.setText(R.string.terminal_retry);
            writings_2_buttons_3_long_b_mid.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleSecond() - нажали на кнопку: " + writings_2_buttons_3_long_b_mid.getText());
                printSlipSaleSecond(receipt);
            });
            writings_2_buttons_3_long_b_bottom.setText(R.string.terminal_next);
            writings_2_buttons_3_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipSaleSecond() - нажали на кнопку: " + writings_2_buttons_3_long_b_bottom.getText());
                if (onInteractionListener != null)
                    onInteractionListener.onActionSuccess(saleType);
            });

            layout.setDisplayedChild(WRITINGS_2_BUTTONS_3_LONG);
        }

        void showPosTerminalFailSlipSaleSecond(final List<String> receipt) {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.terminal_printing_slip_failed);

            Logger.info(TAG, "showPosTerminalFailSlipSaleSecond() - " + writing_buttons_2_long_w.getText());

            writing_buttons_2_long_b_top.setText(R.string.terminal_cancel);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipSaleSecond() - нажали на кнопку: " + writing_buttons_2_long_b_top.getText());
                if (!Globals.getInstance().getPosManager().isReady())
                    showToastTerminalIsBusy();
                else
                    showCancel();
            });
            writing_buttons_2_long_b_bottom.setText(R.string.terminal_retry);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipSaleSecond() - нажали на кнопку: " + writing_buttons_2_long_b_bottom.getText());
                printSlipSaleSecond(receipt);
            });

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showPosTerminalSuccessSlipCancelFirst(final List<String> receipt) {
            writings_2_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_dont_know));

            writings_2_buttons_2_long_w_top.setText(R.string.terminal_is_slip_n_printed);
            writings_2_buttons_2_long_w_bottom.setText(R.string.terminal_cut_slip);

            Logger.info(TAG, "showPosTerminalSuccessSlipCancelFirst() - " + writings_2_buttons_2_long_w_top.getText() + " - " + writings_2_buttons_2_long_w_bottom.getText());

            writings_2_buttons_2_long_b_top.setText(R.string.terminal_retry);
            writings_2_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipCancelFirst() - нажали на кнопку: " + writings_2_buttons_2_long_b_top.getText());
                printSlipCancelFirst(receipt, true);
            });
            writings_2_buttons_2_long_b_bottom.setText(R.string.sale_pd);
            writings_2_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipCancelFirst() - нажали на кнопку: " + writings_2_buttons_2_long_b_bottom.getText());
                if (onInteractionListener != null) {
                    onInteractionListener.onStartNewSellProcess();
                }
            });

            allowPressBack = true;

            layout.setDisplayedChild(WRITINGS_2_BUTTONS_2_LONG);
        }

        void showPosTerminalFailSlipCancelFirst(final List<String> receipt) {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.terminal_printing_slip_failed);

            Logger.info(TAG, "showPosTerminalFailSlipCancelFirst() - " + writing_buttons_2_long_w.getText());

            writing_buttons_2_long_b_top.setText(R.string.terminal_retry);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipCancelFirst() - нажали на кнопку: " + writing_buttons_2_long_b_top.getText());
                printSlipCancelFirst(receipt, true);
            });
            writing_buttons_2_long_b_bottom.setText(R.string.sale_pd);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipCancelFirst() - нажали на кнопку: " + writing_buttons_2_long_b_bottom.getText());
                if (onInteractionListener != null) {
                    onInteractionListener.onStartNewSellProcess();
                }
            });

            allowPressBack = true;

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

        void showPosTerminalOperationRejectedNoPrint(@Nullable final FinancialTransactionResult financialTransactionResult) {

            writing_button_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            if ((financialTransactionResult == null) || (financialTransactionResult.getBankResponse() == null))
                writing_button_w.setText(R.string.terminal_operation_rejected_reason_unknown);
            else
                writing_button_w.setText(getString(R.string.terminal_operation_rejected, financialTransactionResult.getBankResponse()));

            Logger.info(TAG, "showPosTerminalOperationRejectedNoPrint() - " + writing_button_w.getText());

            writing_button_b.setText(R.string.terminal_next);
            writing_button_b.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalOperationRejectedNoPrint() - нажали на кнопку: " + writing_button_b.getText());
                if (onInteractionListener != null) {
                    onInteractionListener.onCancelSellProcess();
                }
            });

            allowPressBack = true;

            layout.setDisplayedChild(WRITING_BUTTON);
        }


        void showPosTerminalPrintingSlipOperationRejectedFirst(final FinancialTransactionResult financialTransactionResult) {
            writings_2_w_top.setText(getString(R.string.terminal_operation_rejected, financialTransactionResult.getBankResponse()));
            FontInjector.injectFont(writings_2_w_top, FontInjector.ROBOTO_BOLD);
            writings_2_w_bottom.setText(R.string.terminal_printing_slip);

            Logger.info(TAG, "showPosTerminalPrintingSlipOperationRejectedFirst() - " + writings_2_w_top.getText() + " - " + writings_2_w_bottom.getText());

            layout.setDisplayedChild(WRITINGS_2);
        }

        void showPosTerminalSuccessSlipOperationRejectedFirst(final FinancialTransactionResult financialTransactionResult) {
            writings_2_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_dont_know));

            writings_2_buttons_2_long_w_top.setText(R.string.terminal_is_slip_n_printed);
            writings_2_buttons_2_long_w_bottom.setText(R.string.terminal_cut_slip);

            Logger.info(TAG, "showPosTerminalSuccessSlipOperationRejectedFirst() - " + writings_2_buttons_2_long_w_top.getText() + " - " + writings_2_buttons_2_long_w_bottom.getText());

            writings_2_buttons_2_long_b_top.setText(R.string.terminal_retry);
            writings_2_buttons_2_long_b_top.setOnClickListener(v -> printSlipOperationRejectedFirst(financialTransactionResult));
            writings_2_buttons_2_long_b_bottom.setText(R.string.terminal_next);
            writings_2_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalSuccessSlipOperationRejectedFirst() - нажали на кнопку: " + writings_2_buttons_2_long_b_bottom.getText());
                if (onInteractionListener != null) {
                    onInteractionListener.onCancelSellProcess();
                }
            });

            allowPressBack = true;

            layout.setDisplayedChild(WRITINGS_2_BUTTONS_2_LONG);
        }

        void showPosTerminalFailSlipOperationRejectedFirst(final FinancialTransactionResult financialTransactionResult) {
            writing_buttons_2_long_icon.setImageDrawable(getResources().getDrawable(R.drawable.icon_failed));

            writing_buttons_2_long_w.setText(R.string.terminal_printing_slip_failed);

            Logger.info(TAG, "showPosTerminalFailSlipOperationRejectedFirst() - " + writing_buttons_2_long_w.getText());

            writing_buttons_2_long_b_top.setText(R.string.terminal_retry);
            writing_buttons_2_long_b_top.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipOperationRejectedFirst() - нажали на кнопку: " + writing_buttons_2_long_b_top.getText());
                printSlipOperationRejectedFirst(financialTransactionResult);
            });
            writing_buttons_2_long_b_bottom.setText(R.string.terminal_next);
            writing_buttons_2_long_b_bottom.setOnClickListener(v -> {
                Logger.info(TAG, "showPosTerminalFailSlipOperationRejectedFirst() - нажали на кнопку: " + writing_buttons_2_long_b_bottom.getText());
                if (onInteractionListener != null) {
                    onInteractionListener.onCancelSellProcess();
                }
            });

            allowPressBack = true;

            layout.setDisplayedChild(WRITING_BUTTONS_2_LONG);
        }

    }

    @Override
    public boolean onBackPress() {
        if (allowPressBack) {
            onInteractionListener.onCancelSellProcess();
        }
        return true;
    }

}
