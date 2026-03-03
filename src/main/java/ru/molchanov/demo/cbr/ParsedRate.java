package ru.molchanov.demo.cbr;

import java.math.BigDecimal;


public class ParsedRate {

    private final String code;
    private final int nominal;
    private final BigDecimal value;

    public ParsedRate(String code, int nominal, BigDecimal value) {
        this.code = code;
        this.nominal = nominal;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public int getNominal() {
        return nominal;
    }

    public BigDecimal getValue() {
        return value;
    }
}