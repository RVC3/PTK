package ru.ppr.core.logic;

import android.support.annotation.NonNull;

import javax.inject.Inject;

/**
 * @author Grigoriy Kashka
 */
public class FioNormalizer {

    private final FioFormatter fioFormatter;

    @Inject
    public FioNormalizer(FioFormatter fioFormatter) {
        this.fioFormatter = fioFormatter;
    }

    /**
     * Вернет нормализованную строку ФИО в виде Фамилия И. О.
     *
     * @param fio
     * @return
     */
    public String getNormalizedFio(@NonNull String fio) {

        //удалим точки
        String newFio = fio.replace(".", "");

        //выделим фамилию
        newFio = newFio.trim();
        int spaceIndex = newFio.indexOf(" ");
        String surname = newFio.substring(0, spaceIndex);
        newFio = newFio.substring(spaceIndex);

        //выделим имя
        newFio = newFio.trim();
        spaceIndex = newFio.indexOf(" ");
        String name = newFio.substring(0, spaceIndex);
        newFio = newFio.substring(spaceIndex);

        //выделим отчество
        String patronymic = newFio.trim();

        newFio = fioFormatter.getFullNameAsSurnameWithInitials(surname, name, patronymic);

        return newFio;
    }
}
