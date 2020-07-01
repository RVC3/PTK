package ru.ppr.chit.data;

import dagger.Binds;
import dagger.Module;
import ru.ppr.chit.data.repository.local.AppPropertiesRepositoryImpl;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;

/**
 * @author Aleksandr Brazhkin
 */
@Module
public abstract class DataSingletonModule {

    @Binds
    public abstract AppPropertiesRepository appPropertiesRepository(AppPropertiesRepositoryImpl appPropertiesRepository);
}
