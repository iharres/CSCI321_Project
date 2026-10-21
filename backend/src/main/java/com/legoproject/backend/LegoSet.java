package com.legoproject.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LegoSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String setNumber;
    private String name;
    private double purchasePrice;
    private double currentValue;
    private String condition;

    public LegoSet() {
    }

    public LegoSet(String setNumber, String name, double purchasePrice, double currentValue) {
        this.setNumber = setNumber;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.currentValue = currentValue;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSetNumber() {
        return setNumber;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public String getCondition() {
        return condition;
    }

    public double getProfit() {
        return currentValue - purchasePrice;
    }

    public double getReturnProfitPercent() {
        return ((currentValue - purchasePrice) / purchasePrice) * 100;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}



