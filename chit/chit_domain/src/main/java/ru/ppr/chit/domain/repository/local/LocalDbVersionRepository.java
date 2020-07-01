package ru.ppr.chit.domain.repository.local;

import ru.ppr.chit.domain.model.local.LocalDbVersion;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;

/**
 * @author Aleksandr Brazhkin
 */
public interface LocalDbVersionRepository extends CrudLocalDbRepository<LocalDbVersion, Long> {
}
