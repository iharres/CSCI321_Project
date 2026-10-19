package com.legoproject.backend;

public class AddSetRequest {

    private String setNumber;
    private double purchasePrice;
    private String condition;

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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition){
        this.condition = condition;
    }

}
