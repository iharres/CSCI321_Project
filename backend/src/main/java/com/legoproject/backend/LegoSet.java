package com.legoproject.backend;

public class LegoSet {

    private String setNumber;
    private String name;
    private double purchasePrice;
    private double currentValue;

    public LegoSet(){

    }
    public LegoSet(String setNumber, String name, double purchasePrice, double currentValue){
        this.setNumber = setNumber;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.currentValue = currentValue;
    }

    public String getName(){
        return name;
    }

    public String getSetNumber(){
        return setNumber;
    }

    public double getPurchasePrice(){
        return purchasePrice;
    }

    public double getCurrentValue(){
        return currentValue;
    }

    public double getProfit(){
        return currentValue - purchasePrice;
    }

    public double getReturnProfitPercent(){
        return ((currentValue - purchasePrice) / purchasePrice) * 100;

    }

    public void setCurrentValue(double currentValue){
        this.currentValue = currentValue;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setPurchasePrice(double purchasePrice){
        this.purchasePrice = purchasePrice;
    }

}



