package ru.ppr.chit.data.mapper.nsi;

import java.util.List;

/**
 * @author Dmitry Nevolin
 */
public interface NsiDbMapper<M, E> {

    M entityToModel(E entity);

    E modelToEntity(M model);

    List<M> entityListToModelList(List<E> entities);

    List<M> modelListToEntityList(List<E> models);

}
