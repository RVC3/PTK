package ru.ppr.ikkm.file.state.model;

/**
 * Оператор принетра
 * Created by Артем on 21.01.2016.
 */
public class Operator {
    private Long id;
    private String operatorName;
    private byte operatorCode; //не должен превышать 99

    public Operator() {
    }

    public Operator(String operatorName, byte operatorCode) {
        this.operatorName = operatorName;
        this.operatorCode = operatorCode;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public byte getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(byte operatorCode) {
        if(operatorCode > 99) {
            this.operatorCode = 99;
        } else {
            this.operatorCode = operatorCode;
        }

    }
}
