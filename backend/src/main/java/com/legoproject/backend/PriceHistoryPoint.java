package com.legoproject.backend;

import java.time.LocalDate;

public class PriceHistoryPoint {

    private final LocalDate date;
    private final double value;
    private final int sales;

    public PriceHistoryPoint(
            LocalDate date,
            double value,
            int sales) {

        this.date = date;
        this.value = value;
        this.sales = sales;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public int getSales() {
        return sales;
    }
}