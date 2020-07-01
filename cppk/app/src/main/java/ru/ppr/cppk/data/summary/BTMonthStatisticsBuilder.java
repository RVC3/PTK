package ru.ppr.cppk.data.summary;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.local.BankTransactionDao;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.entity.event.base34.TerminalDay;

/**
 * Билдер статистики за месяц БТ.
 *
 * @author Dmitry Nevolin
 */
public class BTMonthStatisticsBuilder {

    private final String terminalNumber;
    private final int monthNumber;
    private final Transactions transactions;
    private final Cancels cancels;
    private final TransactionsWithoutSale transactionsWithoutSale;
    private final TransactionsWithoutSaleAndCancellation transactionsWithoutSaleAndCancellation;
    private final CompletedTransactions completedTransactions;

    private BTMonthStatisticsBuilder(Builder builder) {
        terminalNumber = builder.terminalNumber;
        monthNumber = builder.monthNumber;
        transactions = builder.transactions;
        cancels = builder.cancels;
        transactionsWithoutSale = builder.transactionsWithoutSale;
        transactionsWithoutSaleAndCancellation = builder.transactionsWithoutSaleAndCancellation;
        completedTransactions = builder.completedTransactions;
    }

    public String getTerminalNumber() {
        return terminalNumber;
    }

    public int getMonthNumber() {
        return monthNumber;
    }

    @NonNull
    public Transactions getTransactions() {
        return transactions;
    }

    @NonNull
    public Cancels getCancels() {
        return cancels;
    }

    @NonNull
    public TransactionsWithoutSale getTransactionsWithoutSale() {
        return transactionsWithoutSale;
    }

    @NonNull

    public TransactionsWithoutSaleAndCancellation getTransactionsWithoutSaleAndCancellation() {
        return transactionsWithoutSaleAndCancellation;
    }

    @NonNull
    public CompletedTransactions getCompletedTransactions() {
        return completedTransactions;
    }

    @NonNull
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String monthId;
        private boolean buildForLastMonth;

        private String terminalNumber;
        private int monthNumber;
        private Transactions transactions;
        private Cancels cancels;
        private TransactionsWithoutSale transactionsWithoutSale;
        private TransactionsWithoutSaleAndCancellation transactionsWithoutSaleAndCancellation;
        private CompletedTransactions completedTransactions;

        private Builder() {
        }

        @NonNull
        public Builder setMonthId(String monthId) {
            this.monthId = monthId;

            return this;
        }

        @NonNull
        public Builder setBuildForLastMonth(boolean buildForLastMonth) {
            this.buildForLastMonth = buildForLastMonth;

            return this;
        }

        @NonNull
        public BTMonthStatisticsBuilder build() {
            LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();

            MonthEvent monthEvent;

            if (buildForLastMonth) {
                monthEvent = localDaoSession.getMonthEventDao().getLastMonthEvent();
            } else {
                monthEvent = localDaoSession.getMonthEventDao().getLastMonthByMonthId(monthId);
            }

            if (monthEvent != null) {
                BankTransactionDao bankTransactionDao = localDaoSession.getBankTransactionDao();

                List<BankTransactionEvent> transactionsList = bankTransactionDao.getSuccessfulTransactions(monthEvent.getMonthId());
                List<BankTransactionEvent> cancelsList = bankTransactionDao.getSuccessfulCancels(monthEvent.getMonthId());
                List<BankTransactionEvent> transactionsWithoutSaleList = bankTransactionDao.getSuccessfulTransactionsWithoutSale(monthEvent.getMonthId());
                List<BankTransactionEvent> transactionsWithoutSaleAndCancellationList = bankTransactionDao.getSuccessfulTransactionsWithoutSaleAndCancellation(monthEvent.getMonthId());
                List<BankTransactionEvent> completedTransactionsList = bankTransactionDao.getSuccessfulCompletedTransactions(monthEvent.getMonthId());

                TerminalDay lastDay = localDaoSession.getTerminalDayDao().getLastTerminalDayInMonth(monthEvent.getId());

                terminalNumber = lastDay == null ? "0" : lastDay.getTerminalNumber();
                monthNumber = monthEvent.getMonthNumber();
                transactions = new Transactions(transactionsList.size(), getTotal(transactionsList));
                cancels = new Cancels(cancelsList.size(), getTotal(cancelsList));
                transactionsWithoutSale = new TransactionsWithoutSale(transactionsWithoutSaleList.size(), getTotal(transactionsWithoutSaleList));
                transactionsWithoutSaleAndCancellation = new TransactionsWithoutSaleAndCancellation(transactionsWithoutSaleAndCancellationList.size(), getTotal(transactionsWithoutSaleAndCancellationList));
                completedTransactions = new CompletedTransactions(completedTransactionsList.size(), getTotal(completedTransactionsList));
            } else {
                // в этом случае открытого месяца еще не было, по идее такой ситуации быть не должно
                throw new IllegalStateException("monthEvent is null");
            }

            return new BTMonthStatisticsBuilder(this);
        }

        private static BigDecimal getTotal(List<BankTransactionEvent> transactions) {
            BigDecimal total = BigDecimal.ZERO;

            for (BankTransactionEvent bankTransactionEvent : transactions)
                total = total.add(bankTransactionEvent.getTotal());

            return total;
        }

    }

    private static class MonthReportPoint {

        private final int quantity;
        private final BigDecimal total;

        MonthReportPoint(int quantity, BigDecimal total) {
            this.quantity = quantity;
            this.total = total;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getTotal() {
            return total;
        }

    }

    /**
     * Все успешные транзакции оплаты
     */
    public static class Transactions extends MonthReportPoint {
        private Transactions(int quantity, BigDecimal total) {
            super(quantity, total);
        }
    }

    /**
     * Все успешные транзакции отмены
     */
    public static class Cancels extends MonthReportPoint {
        private Cancels(int quantity, BigDecimal total) {
            super(quantity, total);
        }
    }

    /**
     * Транзакции не прикреплённые к событию продажи ПД + события продажи со статусом checkPrinted
     */
    public static class TransactionsWithoutSale extends MonthReportPoint {
        private TransactionsWithoutSale(int quantity, BigDecimal total) {
            super(quantity, total);
        }
    }

    /**
     * Транзакции не прикреплённые к событию продажи ПД + события продажи со статусом checkPrinted, но без успешно аннулированных
     */
    public static class TransactionsWithoutSaleAndCancellation extends MonthReportPoint {
        private TransactionsWithoutSaleAndCancellation(int quantity, BigDecimal total) {
            super(quantity, total);
        }
    }

    /**
     * Все успешные и целиком завершенные транзакции оплаты
     */
    public static class CompletedTransactions extends MonthReportPoint {
        private CompletedTransactions(int quantity, BigDecimal total) {
            super(quantity, total);
        }
    }

}
