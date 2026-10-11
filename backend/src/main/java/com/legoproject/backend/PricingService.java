package com.legoproject.backend;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PricingService {

    //calculating median price based off of sales
    public double calculateMedianPrice(BrickLinkPriceGuide priceGuide) {

        List<BrickLinkSale> sales = priceGuide.getSales();

        List<Double> prices = new ArrayList<>();

        for(BrickLinkSale sale : sales){
            prices.add(sale.getUnitPrice());
        }

        int middle = prices.size() / 2;

        Collections.sort(prices);

        if(prices.size() % 2 == 0){
            return (prices.get(middle - 1) + prices.get(middle)) / 2;
        }

        return prices.get(middle);
    }

    // Calculate the average BrickLink sale price after removing outliers
    public double calculateAverageWithoutOutliers(BrickLinkPriceGuide priceGuide) {

        List<BrickLinkSale> sales = priceGuide.getSales();

        List<Double> prices = new ArrayList<>();

        // Get the price from each sale
        for (BrickLinkSale sale : sales) {
            prices.add(sale.getUnitPrice());
        }

        // Sort prices from lowest to highest
        Collections.sort(prices);

        // Find Q1 and Q3
        double q1IndexDouble = prices.size() * 0.25;
        int q1Index = (int) Math.floor(q1IndexDouble);

        double q3IndexDouble = prices.size() * 0.75;
        int q3Index = (int) Math.floor(q3IndexDouble);

        double q1 = prices.get(q1Index);
        double q3 = prices.get(q3Index);

        // Calculate the interquartile range
        double iqr = q3 - q1;

        // Calculate the outlier boundaries
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        // Keep only prices that are not outliers
        List<Double> filteredPrices = new ArrayList<>();

        for (double price : prices) {
            if (price <= upperBound && price >= lowerBound) {
                filteredPrices.add(price);
            }
        }

        // Calculate the average of the remaining prices
        double total = 0;

        for (double price : filteredPrices) {
            total += price;
        }

        return total / filteredPrices.size();
    }
}


