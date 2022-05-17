package com.test.coding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversionRates implements Serializable {

    private double exchangeRate;
    private String fromCurrencyCode;
    private String fromCurrencyName;
    private String toCurrencyCode;
    private String toCurrencyName;

    public double getExchangeRate() {
        return exchangeRate;
    }

    public String getFromCurrencyCode() {
        return fromCurrencyCode;
    }

    public String getFromCurrencyName() {
        return fromCurrencyName;
    }

    public String getToCurrencyCode() {
        return toCurrencyCode;
    }

    public String getToCurrencyName() {
        return toCurrencyName;
    }

    @Override
    public String toString() {
        return "ConversionRates{" +
                "exchangeRate=" + exchangeRate +
                ", fromCurrencyCode='" + fromCurrencyCode + '\'' +
                ", fromCurrencyName='" + fromCurrencyName + '\'' +
                ", toCurrencyCode='" + toCurrencyCode + '\'' +
                ", toCurrencyName='" + toCurrencyName + '\'' +
                '}';
    }
}
