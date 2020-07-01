package ru.ppr.chit.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * @author Aleksandr Brazhkin
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceScope {


}
