package ru.ppr.chit.data.mapper.local;

import java.util.List;

/**
 * Маппер сущностей локальной базы между доменным слоем и слоем БД
 *
 * @author Dmitry Nevolin
 */
public interface LocalDbMapper<M, E> {

    /**
     * Маппит сущность БД в доменную модель
     *
     * @param entity сущность БД
     * @return доменную модель
     */
    M entityToModel(E entity);

    /**
     * Маппит доменную модель в сущность БД
     *
     * @param model доменная модель
     * @return сущность БД
     */
    E modelToEntity(M model);

    /**
     * Маппит список сущностей в БД список доменных моделей
     *
     * @param entities список сущностей БД
     * @return список доменных моделей
     */
    List<M> entityListToModelList(List<E> entities);

    /**
     * Маппит список доменных моделей в список сущностей БД
     *
     * @param entities список доменных моделей
     * @return список сущностей БД
     */
    List<E> modelListToEntityList(List<M> entities);

}
