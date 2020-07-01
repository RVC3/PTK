package ru.ppr.chit.nsidb.entity.base;

/**
 * Описывает сущность НСИ, у которой есть:
 * - код
 *
 * @author Dmitry Nevolin
 */
public interface NsiEntityWithCode<C> {

    String Property = "Code";

    C getCode();

    void setCode(C code);

}
