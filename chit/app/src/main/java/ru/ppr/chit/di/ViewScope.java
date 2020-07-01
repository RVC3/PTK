package ru.ppr.chit.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * @author Dmitry Nevolin
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewScope {


}
