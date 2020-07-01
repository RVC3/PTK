package ru.ppr.cppk.localdb.repository;

import ru.ppr.cppk.localdb.model.LocalDbVersion;
import ru.ppr.cppk.localdb.repository.base.CrudLocalDbRepository;

/**
 * @author Aleksandr Brazhkin
 */
public interface LocalDbVersionRepository extends CrudLocalDbRepository<LocalDbVersion, Long> {
    int getCurrentVersion();
}
