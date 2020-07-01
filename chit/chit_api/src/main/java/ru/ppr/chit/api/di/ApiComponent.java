package ru.ppr.chit.api.di;

import javax.inject.Singleton;

import dagger.Component;
import ru.ppr.chit.api.Api;

/**
 * @author Dmitry Nevolin
 */
@Singleton
@Component(modules = {ApiModule.class})
public interface ApiComponent {

    Api api();

}
