package com.legoproject.backend;

public class AddSetRequest {

    private String setNumber;
    private double purchasePrice;

    public AddSetRequest() {
    }

    public String getSetNumber() {
        return setNumber;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setSetNumber(String setNumber) {
        this.setNumber = setNumber;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}
