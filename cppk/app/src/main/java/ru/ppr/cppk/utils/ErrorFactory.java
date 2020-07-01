package ru.ppr.cppk.utils;

import android.content.Context;

import ru.ppr.cppk.R;
import ru.ppr.cppk.entity.AuthResult;

public class ErrorFactory {

    public static String getAuthError(Context context, AuthResult result) {

        String errorMessageString = null;

        switch (result) {
            case USER_NOT_FOUND:
                errorMessageString = context.getString(R.string.user_not_found);
                break;

            case CARD_NOT_FOUND:
                errorMessageString = context.getString(R.string.card_not_found);
                break;

            case INVALID_PASSWORD:
                errorMessageString = context.getString(R.string.invalid_password);
                break;

            case INVALID_ROLE:
                errorMessageString = context.getString(R.string.invalid_role);
                break;

            case INVALID_ROLE_NEW_CARD:
                errorMessageString = context.getString(R.string.invalid_role_new_card);
                break;

            case ECP_ERROR:
                errorMessageString = context.getString(R.string.incorrect_ecp_number);
                break;

            case INVALID_TIME:
                errorMessageString = context.getString(R.string.invalid_time);
                break;

            case CARD_NOT_ACTIVE:
                errorMessageString = context.getString(R.string.card_not_active);
                break;

            case AuthDasabledForThisRole:
                errorMessageString = context.getString(R.string.authDisabledForThisRole);
                break;

            default:
                errorMessageString = context.getString(R.string.unknown_error);
                break;
        }

        return errorMessageString;
    }
}