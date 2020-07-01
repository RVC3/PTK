package ru.ppr.cppk.legacy;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Александр on 11.01.2016.
 */
public class EdsException extends Exception {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ERROR_SIGN_DATA})
    public @interface EcpErrorCode {
    }

    public static final int ERROR_SIGN_DATA = 1;

    @EcpErrorCode
    private final int code;

    public EdsException(@EcpErrorCode int code) {
        this.code = code;
    }

    @EcpErrorCode
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return "Eds error, code = " + code;
    }
}
