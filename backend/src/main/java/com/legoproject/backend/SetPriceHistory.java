package com.legoproject.backend;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "set_price_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "set_number",
                                "condition",
                                "price_date"
                        }
                )
        }
)
public class SetPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "set_number",
            nullable = false
    )
    private String setNumber;

    @Column(
            name = "condition",
            nullable = false
    )
    private String condition;

    @Column(
            name = "price_date",
            nullable = false
    )
    private LocalDate priceDate;

    @Column(
            name = "estimated_value",
            nullable = false
    )
    private double estimatedValue;

    @Column(
            name = "source",
            nullable = false
    )
    private String source;

    @Column(
            name = "saved_at",
            nullable = false
    )
    private LocalDateTime savedAt;


    public SetPriceHistory() {
    }


    public SetPriceHistory(
            String setNumber,
            String condition,
            LocalDate priceDate,
            double estimatedValue,
            String source) {

        this.setNumber = setNumber;
        this.condition = condition;
        this.priceDate = priceDate;
        this.estimatedValue = estimatedValue;
        this.source = source;
        this.savedAt = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public String getSetNumber() {
        return setNumber;
    }

    public String getCondition() {
        return condition;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public double getEstimatedValue() {
        return estimatedValue;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }


    public void setEstimatedValue(
            double estimatedValue) {

        this.estimatedValue =
                estimatedValue;

        this.savedAt =
                LocalDateTime.now();
    }


    public void setSource(
            String source) {

        this.source =
                source;

        this.savedAt =
                LocalDateTime.now();
    }
}