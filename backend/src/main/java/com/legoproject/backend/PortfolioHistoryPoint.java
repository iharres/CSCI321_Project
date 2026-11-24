package com.legoproject.backend;

import java.time.LocalDate;

public class PortfolioHistoryPoint {

    private final LocalDate date;
    private final double value;

    public PortfolioHistoryPoint(
            LocalDate date,
            double value) {

        this.date = date;
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }
}