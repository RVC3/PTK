package ru.ppr.chit.api.entity;

/**
 * Описывает Файл. Расчитан на передачу небольших файлов, до 80кб
 *
 * @author Dmitry Nevolin
 */
public class FileEntity {

    /**
     * Полное имя файла
     */
    private String name;
    /**
     * Данные
     */
    private byte[] data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
