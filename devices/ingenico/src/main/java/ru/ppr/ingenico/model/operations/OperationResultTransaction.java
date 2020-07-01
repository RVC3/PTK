package ru.ppr.ingenico.model.operations;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.logger.Logger;
import ru.ppr.utils.DateFormatOperations;
import ru.ppr.ingenico.model.requests.Request;
import ru.ppr.ipos.model.TransactionResult;

/**
 * Created by Dmitry Nevolin on 28.01.2016.
 */
public abstract class OperationResultTransaction extends Operation<TransactionResult> {

    private static final String TAG = Logger.makeLogTag(OperationResultFinancialTransaction.class);

    protected static void info(String requestBodyName, String message) {
        Logger.info(TAG, requestBodyName + "|" + message);
    }

    OperationResultTransaction(int klass, int code) {
        super(klass, code);
    }

    @Override
    public TransactionResult getResult() {
        TransactionResult transactionResult = new TransactionResult();

        for (Request.Body requestBody : getRequestBodies()) {
            if (requestBody.getType() == Request.SET_TAGS) {
                transactionResult = Request.Utils.SET_TAGS.getTransactionResult(requestBody);

                if (transactionResult != null) {
                    info(Request.SET_TAGS.name(), "id: " + String.valueOf(transactionResult.getId()));
                    info(Request.SET_TAGS.name(), "invoice number: " + String.valueOf(transactionResult.getInvoiceNumber()));
                    info(Request.SET_TAGS.name(), "terminal id: " + transactionResult.getTerminalId());
                    info(Request.SET_TAGS.name(), "timestamp: " + DateFormatOperations.getDateddMMyyyyHHmmss(transactionResult.getTimeStamp()));
                }

                break;
            }
        }

        if (transactionResult != null) {
            List<String> receipt = new ArrayList<>();

            for (Request.Body requestBody : getRequestBodies()) {
                if (requestBody.getType() == Request.STORE_RC) {
                    int code = Request.Utils.STORE_RC.parse(requestBody);

                    transactionResult.setApproved(Request.Utils.STORE_RC.getApproved(code));
                    transactionResult.setBankResponseCode(code);
                    transactionResult.setBankResponse(Request.Utils.STORE_RC.getMessage(code));

                    info(Request.STORE_RC.name(), "is approved: " + String.valueOf(transactionResult.isApproved()));
                    info(Request.STORE_RC.name(), "bank response code: " + String.valueOf(transactionResult.getBankResponseCode()));
                    info(Request.STORE_RC.name(), "bank response message: " + transactionResult.getBankResponse());
                } else if (requestBody.getType() == Request.PRINT) {
                    String line = Request.Utils.PRINT.parse(requestBody);

                    receipt.add(line);

                    info(Request.PRINT.name(), line);
                }
            }

            if (!receipt.isEmpty()) {
                receipt.add("                           ");
                receipt.add("                           ");
                transactionResult.setReceipt(receipt);
            }
        }

        return transactionResult;
    }

}
