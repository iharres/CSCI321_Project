package com.legoproject.backend;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PriceHistoryService {

    private final BrickLinkService brickLinkService;

    public PriceHistoryService(
            BrickLinkService brickLinkService) {

        this.brickLinkService =
                brickLinkService;
    }


    public List<PriceHistoryPoint> getPriceHistory(
            String setNumber,
            String condition,
            String period) throws Exception {

        String normalizedCondition =
                normalizeCondition(condition);


        BrickLinkPriceGuide priceGuide =
                brickLinkService.getPriceGuide(
                        setNumber,
                        normalizedCondition
                );


        List<BrickLinkSale> sales =
                priceGuide.getSales();


        if (sales == null || sales.isEmpty()) {
            return new ArrayList<>();
        }


        LocalDate cutoffDate =
                getCutoffDate(period);


        LocalDate today =
                LocalDate.now();


        /*
         * Group ALL available BrickLink sales by week.
         *
         * We intentionally do not filter by the requested
         * period yet because we may need a sale from before
         * the cutoff as the starting value.
         */
        Map<LocalDate, List<BrickLinkSale>> weeklySales =
                new TreeMap<>();


        for (BrickLinkSale sale : sales) {

            LocalDate saleDate =
                    parseBrickLinkDate(
                            sale.getDateOrdered()
                    );


            if (saleDate == null) {
                continue;
            }


            LocalDate weekStart =
                    saleDate.with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.MONDAY
                            )
                    );


            weeklySales
                    .computeIfAbsent(
                            weekStart,
                            key -> new ArrayList<>()
                    )
                    .add(sale);
        }


        /*
         * Convert each week's real sales into
         * one estimated market value.
         */
        Map<LocalDate, PriceHistoryPoint> actualWeeklyValues =
                new TreeMap<>();


        for (
                Map.Entry<LocalDate, List<BrickLinkSale>> entry
                : weeklySales.entrySet()
        ) {

            List<BrickLinkSale> filteredSales =
                    removeOutliers(
                            entry.getValue()
                    );


            if (filteredSales.isEmpty()) {
                continue;
            }


            double weeklyValue =
                    calculateWeightedAverage(
                            filteredSales
                    );


            actualWeeklyValues.put(
                    entry.getKey(),
                    new PriceHistoryPoint(
                            entry.getKey(),
                            roundToTwoDecimals(
                                    weeklyValue
                            ),
                            filteredSales.size()
                    )
            );
        }


        if (actualWeeklyValues.isEmpty()) {
            return new ArrayList<>();
        }


        LocalDate startWeek =
                cutoffDate.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );


        LocalDate currentWeek =
                today.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );


        /*
         * Find the most recent known market value
         * from before the requested period.
         */
        Double lastKnownValue =
                null;


        for (
                Map.Entry<LocalDate, PriceHistoryPoint> entry
                : actualWeeklyValues.entrySet()
        ) {

            if (
                    entry.getKey().isBefore(startWeek)
                            ||
                            entry.getKey().isEqual(startWeek)
            ) {

                lastKnownValue =
                        entry.getValue().getValue();
            }
        }


        List<PriceHistoryPoint> history =
                new ArrayList<>();


        /*
         * Walk through every week in the requested range.
         *
         * If there was a real sale that week:
         *      use the new market value.
         *
         * If there was no sale:
         *      carry forward the last known value.
         */
        LocalDate week =
                startWeek;


        while (
                !week.isAfter(currentWeek)
        ) {

            PriceHistoryPoint actualPoint =
                    actualWeeklyValues.get(
                            week
                    );


            if (actualPoint != null) {

                lastKnownValue =
                        actualPoint.getValue();


                history.add(
                        actualPoint
                );

            } else if (lastKnownValue != null) {

                /*
                 * sales = 0 means this is a carried-forward
                 * value rather than a new market observation.
                 */
                history.add(
                        new PriceHistoryPoint(
                                week,
                                lastKnownValue,
                                0
                        )
                );
            }


            week =
                    week.plusWeeks(1);
        }


        return history;
    }


    // ======================================================
    // CONDITION
    // ======================================================

    private String normalizeCondition(
            String condition) {

        if (condition == null) {
            return "NEW";
        }


        if (condition.equalsIgnoreCase("N") || condition.equalsIgnoreCase("NEW")) {

            return "NEW";
        }

        return "USED";
    }


    // ======================================================
    // PERIOD
    // ======================================================

    private LocalDate getCutoffDate(String period) {

        LocalDate today = LocalDate.now();


        if (period == null) {return today.minusMonths(6);}


        return switch (period.toUpperCase()) {

            case "1M" ->
                    today.minusMonths(1);

            case "3M" ->
                    today.minusMonths(3);

            case "6M" ->
                    today.minusMonths(6);

            case "12M" ->
                    today.minusMonths(12);

            default ->
                    today.minusMonths(6);
        };
    }


    // ======================================================
    // DATE PARSING
    // ======================================================

    private LocalDate parseBrickLinkDate(String dateOrdered) {

        try {

            if (dateOrdered == null || dateOrdered.length() < 10) {
                return null;
            }

            return LocalDate.parse(dateOrdered.substring(0, 10));

        } catch (Exception e) {
            return null;
        }
    }


    // ======================================================
    // OUTLIER FILTERING
    // ======================================================

    private List<BrickLinkSale> removeOutliers(List<BrickLinkSale> sales) {

        if (sales.size() < 4) {
            return new ArrayList<>(sales);
        }


        List<Double> prices = sales.stream().map(BrickLinkSale::getUnitPrice).sorted().toList();


        double q1 = percentile(prices, 0.25);

        double q3 = percentile(prices, 0.75);


        double iqr = q3 - q1;


        if (iqr == 0) {
            return new ArrayList<>(sales);
        }


        double lowerBound = q1 - (1.5 * iqr);

        double upperBound = q3 + (1.5 * iqr);


        List<BrickLinkSale> filtered = new ArrayList<>();


        for (BrickLinkSale sale : sales) {

            double price = sale.getUnitPrice();

            if (
                    price >= lowerBound && price <= upperBound
            ) {

                filtered.add(sale);
            }
        }


        if (filtered.isEmpty()) {
            return new ArrayList<>(sales);
        }


        return filtered;
    }


    // ======================================================
    // PERCENTILE
    // ======================================================

    private double percentile(List<Double> values, double percentile) {

        if (values.isEmpty()) {
            return 0;
        }


        double index = percentile * (values.size() - 1);


        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);


        if (lowerIndex == upperIndex) {
            return values.get(lowerIndex);
        }


        double fraction = index - lowerIndex;
        return values.get(lowerIndex) + (values.get(upperIndex) - values.get(lowerIndex)) * fraction;
    }


    // ======================================================
    // WEIGHTED AVERAGE
    // ======================================================

    private double calculateWeightedAverage(List<BrickLinkSale> sales) {

        double totalValue = 0;

        int totalQuantity = 0;


        for (BrickLinkSale sale : sales) {
            int quantity = Math.max(sale.getQuantity(), 1);


            totalValue += sale.getUnitPrice() * quantity;
            totalQuantity += quantity;
        }


        if (totalQuantity == 0) {
            return 0;
        }


        return totalValue / totalQuantity;
    }


    // ======================================================
    // ROUNDING
    // ======================================================

    private double roundToTwoDecimals(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}