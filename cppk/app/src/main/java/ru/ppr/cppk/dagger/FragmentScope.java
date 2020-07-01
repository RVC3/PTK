package ru.ppr.cppk.dagger;

import android.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Границы жизни объекта - существование экземпляра {@link Fragment}.
 *
 * @author Aleksandr Brazhkin
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface FragmentScope {
}
