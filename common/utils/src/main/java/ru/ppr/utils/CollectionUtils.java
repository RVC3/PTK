package ru.ppr.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Функции для работы с коллекциями.
 *
 * @author Aleksandr Brazhkin
 */
public class CollectionUtils {

    /**
     * Функция конвертации значения в элемент коллекции.
     *
     * @param <S> Тип исходного значения
     * @param <T> Тип нового значения
     */
    public interface MapFunc<S, T> {
        /**
         * Конвертирует {@code source} в значение типа {@code T}
         *
         * @param source Исходное значение
         * @return Значение типа {@code T}
         */
        T map(S source);
    }

    /**
     * Конвертирует {@link List<S>} в {@link List<T>}
     *
     * @param list    Исходный список
     * @param mapFunc Функция конвертации
     * @param <T>     Тип элемента списка на выходе
     * @param <S>     Тип элемента списка на входе
     * @return Список элементов типа {@code T}
     */
    @Nullable
    public static <T, S> List<T> map(@Nullable List<S> list, @NonNull MapFunc<S, T> mapFunc) {
        if (list == null) {
            return null;
        }
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> newList = new ArrayList<>();
        for (S s : list) {
            newList.add(mapFunc.map(s));
        }
        return newList;
    }

    /**
     * Конвертирует {@code value} в {@link Set<T>}
     *
     * @param value Значение
     * @param <T>   Тип значения
     * @return Множество элементов типа {@code T}
     */
    @Nullable
    public static <T> Set<T> asSet(@Nullable T value) {
        if (value == null) {
            return null;
        }
        return Collections.singleton(value);
    }

    /**
     * Конвертирует {@link Collection<S>} {@link Set<T>}
     *
     * @param collection Исходная коллекция
     * @param mapFunc    Функция конвертации
     * @param <T>        Тип элемента множества на выходе
     * @param <S>        Тип элемента коллеции на входе
     * @return Множество элементов типа {@code T}
     */
    @Nullable
    public static <T, S> Set<T> asSet(@Nullable Collection<S> collection, @NonNull MapFunc<S, T> mapFunc) {
        if (collection == null) {
            return null;
        }
        if (collection.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> set = new HashSet<>();
        for (S s : collection) {
            set.add(mapFunc.map(s));
        }
        return set;
    }

    /**
     * Выполянет INNER JOIN множества и элемента.
     *
     * @param set   Множество
     * @param value Элемент
     * @param <T>   Тип элемента множества на выходе
     * @return Множество элементов типа {@code T}
     */
    @Nullable
    public static <T> Set<T> innerJoin(@Nullable Set<T> set, @Nullable T value) {
        if (set == null) {
            return value == null ? null : Collections.singleton(value);
        }
        if (value == null) {
            return new HashSet<>(set);
        }
        return set.contains(value) ? Collections.singleton(value) : new HashSet<T>();
    }

    /**
     * Выполянет INNER JOIN двух множеств.
     *
     * @param set1 Множество 1
     * @param set2 Множество 2
     * @param <T>  Тип элемента множества на выходе
     * @return Множество элементов типа {@code T}
     */
    @Nullable
    public static <T> Set<T> innerJoin(@Nullable Set<T> set1, @Nullable Set<T> set2) {
        if (set1 == null) {
            return set2 == null ? null : new HashSet<>(set2);
        }
        if (set2 == null) {
            return new HashSet<>(set1);
        }
        Set<T> result = new HashSet<>();
        result.addAll(set1);
        result.retainAll(set2);
        return result;
    }

    /**
     * Выполянет FULL OUTER JOIN множества и элемента.
     *
     * @param set   Множество
     * @param value Элемент
     * @param <T>   Тип элемента множества на выходе
     * @return Множество элементов типа {@code T}
     */
    @Nullable
    public static <T> Set<T> fullOuterJoin(@Nullable Set<T> set, @Nullable T value) {
        if (set == null) {
            return value == null ? null : Collections.singleton(value);
        }
        if (value == null) {
            return new HashSet<>(set);
        }
        Set<T> result = new HashSet<>();
        result.addAll(set);
        result.add(value);
        return result;
    }

    /**
     * Выполянет FULL OUTER JOIN двух множеств.
     *
     * @param set1 Множество 1
     * @param set2 Множество 2
     * @param <T>  Тип элемента множества на выходе
     * @return Множество элементов типа {@code T}
     */
    @Nullable
    public static <T> Set<T> fullOuterJoin(@Nullable Set<T> set1, @Nullable Set<T> set2) {
        if (set1 == null) {
            return set2 == null ? null : new HashSet<>(set2);
        }
        if (set2 == null) {
            return new HashSet<>(set1);
        }
        Set<T> result = new HashSet<>();
        result.addAll(set1);
        result.addAll(set2);
        return result;
    }

    /**
     * Проверяет равенство двух множеств по значению.
     *
     * @param set1 Множество 1
     * @param set2 Множество 2
     * @param <T>  Тип элементов множества
     * @return {@code true} если множества равны, {@code false} - иначе.
     */
    public static <T> boolean equals(@Nullable Set<T> set1, @Nullable Set<T> set2) {
        if (set1 != null) {
            return set1.equals(set2);
        } else {
            return set2 == null;
        }
    }
}
