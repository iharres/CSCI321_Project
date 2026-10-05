package com.legoproject.backend;

public class BrickLinkSale {
    private double unitPrice;
    private int quantity;
    private String sellerCountry;
    private String buyerCountry;
    private String dateOrdered;

    public BrickLinkSale(){
    }

    public double getUnitPrice(){
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice){
        this.unitPrice = unitPrice;
    }

    public int getQuantity(){
        return quantity;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public String getSellerCountry(){
        return sellerCountry;
    }

    public void setSellerCountry(String sellerCountry){
        this.sellerCountry = sellerCountry;
    }

    public String getBuyerCountry(){
        return buyerCountry;
    }

    public void setBuyerCountry(String buyerCounty){
        this.buyerCountry = buyerCountry;
    }

    public String getDateOrdered(){
        return dateOrdered;
    }

    public void setDateOrdered(){
        this.dateOrdered = dateOrdered;
    }




}
