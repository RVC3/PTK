package ru.ppr.core.exceptions;

/**
 * @author m.sidorov
 *
 * Exception c сообщением, которое можно показывать в UI
 */
public class UserException extends Exception implements UserThrowable {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause){
        super(message, cause);
    }

    public static Throwable wrap(Throwable t){
        return wrap(t, "Системная ошибка");
    }

    public static Throwable wrap(Throwable t, String defaultMessage){
        if (t instanceof UserThrowable){
            return t;
        } else {
            return new UserException(defaultMessage, t);
        }
    }

}
