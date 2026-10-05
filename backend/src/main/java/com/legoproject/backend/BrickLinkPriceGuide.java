package com.legoproject.backend;

import java.util.List;

public class BrickLinkPriceGuide {
    private double minPrice;
    private double maxPrice;
    private double averagePrice;
    private double quantityAveragePrice;
    private int unitQuantity;
    private int totalQuantity;
    private List<BrickLinkSale> sales;

    public BrickLinkPriceGuide(){
    }

    public double getMinPrice(){
        return minPrice;
    }

    public void setMinPrice(double minPrice){
        this.minPrice = minPrice;
    }

    public double getMaxPrice(){
        return maxPrice;
    }
    public void setMaxPrice(double maxPrice){
        this.maxPrice = maxPrice;
    }

    public double getAveragePrice(){
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice){
        this.averagePrice = averagePrice;
    }

    public double getQuantityAveragePrice(){
        return quantityAveragePrice;
    }

    public void setQuantityAveragePrice(double quantityAveragePrice){
        this.quantityAveragePrice = quantityAveragePrice;
    }

    public int getUnitQuantity(){
        return unitQuantity;
    }

    public void setUnitQuantity(int unitQuantity){
        this.unitQuantity = unitQuantity;
    }

    public int getTotalQuantity(){
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity){
        this.totalQuantity = totalQuantity;
    }

    public List<BrickLinkSale> getSales(){
        return sales;
    }

    public void setSales(List<BrickLinkSale> sales){
        this.sales = sales;
    }


}
