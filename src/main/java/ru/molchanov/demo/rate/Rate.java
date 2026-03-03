package ru.molchanov.demo.rate;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "rates")
public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyCode;

    private LocalDate rateDate;

    private BigDecimal rateValue;

    private int nominal;

    private Instant updatedAt;
    public Rate() {
    }

    public Long getId() { return id; }
    public String getCurrencyCode() { return currencyCode; }
    public LocalDate getRateDate() { return rateDate; }
    public BigDecimal getRateValue() { return rateValue; }
    public int getNominal() { return nominal; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    public void setRateDate(LocalDate rateDate) {
        this.rateDate = rateDate;
    }

    public void setValue(BigDecimal rateValue) {
        this.rateValue = rateValue;
    }
    public void setNominal(int nominal) {
        this.nominal = nominal;
    }

    public void setUpdatedAt(Instant now) {
        this.updatedAt = now;
    }
}
