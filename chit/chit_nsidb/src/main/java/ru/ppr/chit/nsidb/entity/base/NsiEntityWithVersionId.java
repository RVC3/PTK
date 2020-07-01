package ru.ppr.chit.nsidb.entity.base;

/**
 * Описывает сущность НСИ, у которой есть:
 * - идентификатор версии НСИ
 *
 * @author Dmitry Nevolin
 */
public interface NsiEntityWithVersionId {

    String Property = "VersionId";

    int getVersionId();

    void setVersionId(int versionId);

}
