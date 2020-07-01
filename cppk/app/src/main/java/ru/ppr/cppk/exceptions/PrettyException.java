package ru.ppr.cppk.exceptions;

import ru.ppr.core.exceptions.UserException;

/**
 * Created by Александр on 25.12.2015.
 *
 * Exception c сообщением, которое можно показывать в UI
 * Устаревший класс, вместо него лучше использовать общий класс UserException
 */
@Deprecated
public class PrettyException extends UserException {

    public PrettyException(String detailMessage) {
        super(detailMessage);
    }

}
