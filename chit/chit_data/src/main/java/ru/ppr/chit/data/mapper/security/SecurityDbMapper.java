package ru.ppr.chit.data.mapper.security;

import java.util.List;

/**
 * @author Dmitry Nevolin
 */
public interface SecurityDbMapper<M, E> {

    M entityToModel(E entity);

    E modelToEntity(M model);

    List<M> entityListToModelList(List<E> entities);

    List<E> modelListToEntityList(List<M> entities);

}
