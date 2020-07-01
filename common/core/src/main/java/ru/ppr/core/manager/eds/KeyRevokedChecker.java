package ru.ppr.core.manager.eds;

import android.support.annotation.Nullable;

import java.util.Date;

import javax.inject.Inject;

/**
 * Проверка на отозванность ключа
 *
 * @author Grigoriy Kashka
 */
public class KeyRevokedChecker {

    @Inject
    public KeyRevokedChecker() {
    }

    /**
     * Метод возвращает флаг отозванности ключа ЭЦП.
     *
     * @return {@code true} - отозвана, иначе {@code false}.
     */
    public boolean isRevoked(@Nullable Date dateOfRevocation) {
        return dateOfRevocation != null && dateOfRevocation.getTime() > 0 && dateOfRevocation.before(new Date());
    }
}
