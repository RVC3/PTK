package ru.ppr.core.logic;

import android.text.TextUtils;

import java.util.Locale;

import javax.inject.Inject;

/**
 * Склеивает Имя Фамилию Отчетсво в формат с которым работает ЦОД
 *
 * @author Grigoriy Kashka
 */
public class FioFormatter {

    @Inject
    public FioFormatter() {
    }

    /**
     * Склеивает Фамлиию Имя и Отчество в "Фамилия И. О.", также обрезает пробелы слева и справа Фамилии, Имени и Отчества
     *
     * @param surname
     * @param name
     * @param patronymic
     * @return
     */
    public String getFullNameAsSurnameWithInitials(String surname, String name, String patronymic) {
        String resSurname = "";
        String resName = "";
        String resPatronymic = "";
        if (!TextUtils.isEmpty(surname)) {
            String surnameTrim = surname.trim();
            if (surname.length() > 0)
                resSurname = surnameTrim.toUpperCase(Locale.getDefault()).substring(0, 1) + surnameTrim.substring(1, surnameTrim.length());
        }
        if (!TextUtils.isEmpty(name)) {
            String nameTrim = name.trim();
            if (nameTrim.length() > 0)
                resName = nameTrim.toUpperCase(Locale.getDefault()).substring(0, 1);
        }
        if (!TextUtils.isEmpty(patronymic)) {
            String patronymicTrim = patronymic.trim();
            if (patronymicTrim.length() > 0)
                resPatronymic = patronymicTrim.toUpperCase(Locale.getDefault()).substring(0, 1);
        }
        //по словам Александра Корчака в случае если нет какойто составляющей ФИО не нужно выводить ее и пробел и точку от нее
        //http://agile.srvdev.ru/browse/CPPKPP-33946
        String surnameAndName = resSurname + (TextUtils.isEmpty(resName) ? "" : (TextUtils.isEmpty(resSurname) ? "" : " ") + resName + "."); //склеим сперва Фаимилию и И.
        String patronymicAndDot = (TextUtils.isEmpty(resPatronymic) ? "" : resPatronymic + "."); //приклеим к отчеству точку
        return surnameAndName + (!TextUtils.isEmpty(surnameAndName) && !TextUtils.isEmpty(patronymicAndDot) ? " " : "") + patronymicAndDot;
    }
}
