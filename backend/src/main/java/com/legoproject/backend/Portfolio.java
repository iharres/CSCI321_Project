package com.legoproject.backend;
import java.util.ArrayList;
import java.util.List;

public class Portfolio {

    private List<LegoSet> sets;

    public Portfolio(){
        sets = new ArrayList<>();
    }

    public void addSet(LegoSet set){
        sets.add(set);
    }

    public double getTotalCost(){
        double total = 0;
        for(LegoSet set : sets){
            total = total + set.getPurchasePrice();
        }
        return total;
    }

    public double getCurrentValue(){
        double total = 0;
        for(LegoSet set : sets){
            total = total + set.getCurrentValue();
        }
        return total;
    }

    public double getProfit(){
        double totalCost = getTotalCost();
        double totalValue = getCurrentValue();
        return totalValue - totalCost;
    }

    public List<LegoSet> getSets(){
        return sets;
    }
}
