package ru.ppr.chit.domain.model.nsi.base;

/**
 * Описывает доменную модель НСИ, у которой есть:
 * - идентификатор версии НСИ
 *
 * @author Dmitry Nevolin
 */
public interface NsiModelWithVersionId {

    int getVersionId();

    void setVersionId(int versionId);

}
