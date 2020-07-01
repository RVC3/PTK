package ru.ppr.chit.api.mapper;

import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Aleksandr Brazhkin
 */
@Qualifier
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@interface UtcDate {
}
