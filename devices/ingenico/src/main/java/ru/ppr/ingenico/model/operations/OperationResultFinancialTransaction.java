package ru.ppr.ingenico.model.operations;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.logger.Logger;
import ru.ppr.utils.DateFormatOperations;
import ru.ppr.ingenico.model.requests.Request;
import ru.ppr.ipos.model.FinancialTransactionResult;

/**
 * Created by Dmitry Nevolin on 28.01.2016.
 */
public abstract class OperationResultFinancialTransaction extends Operation<FinancialTransactionResult> {

    private static final String TAG = Logger.makeLogTag(OperationResultFinancialTransaction.class);

//    public class Result implements Operation.Result {
//
//        @Nullable
//        @Override
//        public FinancialTransactionResult get() {
//            FinancialTransactionResult financialTransactionResult = new FinancialTransactionResult();
//
//            for (Request.Body requestBody : getRequestBodies()) {
//                if (requestBody.getType() == Request.SET_TAGS) {
//                    financialTransactionResult = Request.Utils.SET_TAGS.getFinancialTransactionResult(requestBody);
//
//                    if(financialTransactionResult != null) {
//                        info(Request.SET_TAGS.name(), "id: " + String.valueOf(financialTransactionResult.getId()));
//                        info(Request.SET_TAGS.name(), "invoice number: " + String.valueOf(financialTransactionResult.getInvoiceNumber()));
//                        info(Request.SET_TAGS.name(), "terminal id: " + financialTransactionResult.getTerminalId());
//                        info(Request.SET_TAGS.name(), "timestamp: " + DateFormatOperations.getDateddMMyyyyHHmmss(financialTransactionResult.getTimeStamp()));
//                        info(Request.SET_TAGS.name(), "rrn: " + financialTransactionResult.getRRN());
//                        info(Request.SET_TAGS.name(), "application name: " + financialTransactionResult.getApplicationName());
//                        info(Request.SET_TAGS.name(), "issuer name: " + financialTransactionResult.getIssuerName());
//                        info(Request.SET_TAGS.name(), "card pan: " + financialTransactionResult.getCardPAN());
//                        info(Request.SET_TAGS.name(), "amount: " + String.valueOf(financialTransactionResult.getAmount()));
//                        info(Request.SET_TAGS.name(), "merchant id: " + financialTransactionResult.getMerchantId());
//                        info(Request.SET_TAGS.name(), "authorization id: " + financialTransactionResult.getAuthorizationId());
//                        info(Request.SET_TAGS.name(), "merchant id: " + financialTransactionResult.getMerchantId());
//                        info(Request.SET_TAGS.name(), "currency code: " + financialTransactionResult.getCurrencyCode());
//                    }
//
//                    break;
//                }
//            }
//
//            if (financialTransactionResult != null) {
//                List<String> receipt = new ArrayList<String>();
//
//                for (Request.Body requestBody : getRequestBodies()) {
//                    if (requestBody.getType() == Request.STORE_RC) {
//                        int code = Request.Utils.STORE_RC.parse(requestBody);
//
//                        financialTransactionResult.setApproved(Request.Utils.STORE_RC.getApproved(code));
//                        financialTransactionResult.setBankResponseCode(code);
//                        financialTransactionResult.setBankResponse(Request.Utils.STORE_RC.getMessage(code));
//
//                        info(Request.STORE_RC.name(), "is approved: " + String.valueOf(financialTransactionResult.isApproved()));
//                        info(Request.STORE_RC.name(), "bank response code: " + String.valueOf(financialTransactionResult.getBankResponseCode()));
//                        info(Request.STORE_RC.name(), "bank response message: " + financialTransactionResult.getBankResponse());
//                    } else if (requestBody.getType() == Request.PRINT) {
//                        String line = Request.Utils.PRINT.parse(requestBody);
//
//                        receipt.add(line);
//
//                        info(Request.PRINT.name(), line);
//                    }
//                }
//
//                if (!receipt.isEmpty()) {
//                    receipt.add("                           ");
//                    receipt.add("                           ");
//                    financialTransactionResult.setReceipt(receipt);
//                }
//            }
//
//            return financialTransactionResult;
//        }
//
//    }

    protected static void info(String requestBodyName, String message) {
        Logger.info(TAG, requestBodyName + "|" + message);
    }

    OperationResultFinancialTransaction(int klass, int code) {
        super(klass, code);
    }

    @Override
    public FinancialTransactionResult getResult() {
        FinancialTransactionResult financialTransactionResult = new FinancialTransactionResult();

        for (Request.Body requestBody : getRequestBodies()) {
            if (requestBody.getType() == Request.SET_TAGS) {
                financialTransactionResult = Request.Utils.SET_TAGS.getFinancialTransactionResult(requestBody);

                if(financialTransactionResult != null) {
                    info(Request.SET_TAGS.name(), "id: " + String.valueOf(financialTransactionResult.getId()));
                    info(Request.SET_TAGS.name(), "invoice number: " + String.valueOf(financialTransactionResult.getInvoiceNumber()));
                    info(Request.SET_TAGS.name(), "terminal id: " + financialTransactionResult.getTerminalId());
                    info(Request.SET_TAGS.name(), "timestamp: " + DateFormatOperations.getDateddMMyyyyHHmmss(financialTransactionResult.getTimeStamp()));
                    info(Request.SET_TAGS.name(), "rrn: " + financialTransactionResult.getRRN());
                    info(Request.SET_TAGS.name(), "application name: " + financialTransactionResult.getApplicationName());
                    info(Request.SET_TAGS.name(), "issuer name: " + financialTransactionResult.getIssuerName());
                    info(Request.SET_TAGS.name(), "card pan: " + financialTransactionResult.getCardPAN());
                    info(Request.SET_TAGS.name(), "amount: " + String.valueOf(financialTransactionResult.getAmount()));
                    info(Request.SET_TAGS.name(), "merchant id: " + financialTransactionResult.getMerchantId());
                    info(Request.SET_TAGS.name(), "authorization id: " + financialTransactionResult.getAuthorizationId());
                    info(Request.SET_TAGS.name(), "merchant id: " + financialTransactionResult.getMerchantId());
                    info(Request.SET_TAGS.name(), "currency code: " + financialTransactionResult.getCurrencyCode());
                }

                break;
            }
        }

        if (financialTransactionResult != null) {
            List<String> receipt = new ArrayList<>();


            for (int i = 0; i < getRequestBodies().size(); i++) {
                getRequestBodies().get(i);
            }

            for (Request.Body requestBody : getRequestBodies()) {
                if (requestBody.getType() == Request.STORE_RC) {
                    int code = Request.Utils.STORE_RC.parse(requestBody);

                    financialTransactionResult.setApproved(Request.Utils.STORE_RC.getApproved(code));
                    financialTransactionResult.setBankResponseCode(code);
                    financialTransactionResult.setBankResponse(Request.Utils.STORE_RC.getMessage(code));

                    info(Request.STORE_RC.name(), "is approved: " + String.valueOf(financialTransactionResult.isApproved()));
                    info(Request.STORE_RC.name(), "bank response code: " + String.valueOf(financialTransactionResult.getBankResponseCode()));
                    info(Request.STORE_RC.name(), "bank response message: " + financialTransactionResult.getBankResponse());
                } else if (requestBody.getType() == Request.PRINT) {
                    String line = Request.Utils.PRINT.parse(requestBody);

                    receipt.add(line);

                    info(Request.PRINT.name(), line);
                }
            }

            if (!receipt.isEmpty()) {
                receipt.add("                           ");
                receipt.add("                           ");
                financialTransactionResult.setReceipt(receipt);
            }
        }

        return financialTransactionResult;
    }

}
