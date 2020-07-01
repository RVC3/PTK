package ru.ppr.database.references;

/**
 * Класс, хранящий информацию о свзях таблиц  (ссылках)
 *
 * @author m.sidorov
 */
public class ReferenceInfo {

    public enum ReferencesType {NO_ACTION, RESTRICT, SET_NULL, CASCADE}

    // Имя мастер таблицы (на которую ссылаются)
    private final String masterTable;
    // Имя поля таблицы мастера (PrimaryKey)
    private final String masterField;
    // Имя ссылочной таблицы (которая ссылается на мастер)
    private final String referenceTable;
    // Имя поля ссылки из ссылочной таблицы
    private final String referenceField;
    // Тип связи
    private final ReferencesType referencesType;

    public String getMasterTable(){
        return masterTable;
    }

    public String getMasterField(){
        return masterField;
    }

    public String getReferenceTable(){
        return referenceTable;
    }

    public String getReferenceField(){
        return referenceField;
    }

    public ReferencesType getReferencesType(){
        return referencesType;
    }

    public ReferenceInfo(String masterTable, String masterField, String referenceTable, String referenceField, ReferencesType referencesType){
        this.masterTable = masterTable;
        this.masterField = masterField;
        this.referenceTable = referenceTable;
        this.referenceField = referenceField;
        this.referencesType = referencesType;
    }

}
