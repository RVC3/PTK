package ru.ppr.cppk.dagger;

import android.app.Activity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Границы жизни объекта - существование экземпляра {@link Activity}.
 *
 * @author Aleksandr Brazhkin
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityScope {
}
