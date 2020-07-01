package ru.ppr.chit.domain.model.nsi.base;

/**
 * Описывает доменную модель НСИ, у которой есть:
 * - код
 *
 * @author Dmitry Nevolin
 */
public interface NsiModelWithCode<C> {

    C getCode();

    void setCode(C code);

}
