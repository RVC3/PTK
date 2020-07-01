package ru.ppr.chit.data.repository.nsi;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.CredentialDocumentTypeMapper;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.data.repository.nsi.base.BaseCvdNsiDbRepository;
import ru.ppr.chit.domain.model.nsi.CredentialDocumentType;
import ru.ppr.chit.domain.repository.nsi.CredentialDocumentTypeRepository;
import ru.ppr.chit.nsidb.entity.CredentialDocumentTypeEntity;

/**
 * @author Aleksandr Brazhkin
 */
public class CredentialDocumentTypeRepositoryImpl extends BaseCvdNsiDbRepository<CredentialDocumentType, CredentialDocumentTypeEntity, Long> implements CredentialDocumentTypeRepository {

    @Inject
    CredentialDocumentTypeRepositoryImpl(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Override
    protected AbstractDao<CredentialDocumentTypeEntity, Void> dao() {
        return daoSession().getCredentialDocumentTypeEntityDao();
    }

    @Override
    protected NsiDbMapper<CredentialDocumentType, CredentialDocumentTypeEntity> mapper() {
        return CredentialDocumentTypeMapper.INSTANCE;
    }

}