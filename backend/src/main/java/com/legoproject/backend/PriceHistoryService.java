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

    private final SetPriceHistoryService
            setPriceHistoryService;


    public PriceHistoryService(
            BrickLinkService brickLinkService,
            SetPriceHistoryService setPriceHistoryService) {

        this.brickLinkService = brickLinkService;

        this.setPriceHistoryService = setPriceHistoryService;
    }


    public List<PriceHistoryPoint> getPriceHistory(
            String setNumber,
            String condition,
            String period) {

        String normalizedCondition =
                normalizeCondition(
                        condition
                );


        /*
         * First try to update our PostgreSQL cache
         * using the newest BrickLink sales.
         *
         * If BrickLink is unavailable, we simply
         * continue using whatever is already saved.
         */
        try {

            updateCacheFromBrickLink(
                    setNumber,
                    normalizedCondition
            );

        } catch (Exception e) {

            System.out.println(
                    "BrickLink unavailable for "
                            + setNumber
                            + ". Using cached price history."
            );
        }


        /*
         * Whether BrickLink worked or not,
         * the graph now reads from PostgreSQL.
         */
        return buildHistoryFromCache(
                setNumber,
                normalizedCondition,
                period
        );
    }


    // ======================================================
    // UPDATE DATABASE CACHE FROM BRICKLINK
    // ======================================================

    private void updateCacheFromBrickLink(
            String setNumber,
            String condition) throws Exception {

        BrickLinkPriceGuide priceGuide =
                brickLinkService.getPriceGuide(
                        setNumber,
                        condition
                );


        List<BrickLinkSale> sales =
                priceGuide.getSales();


        if (
                sales == null
                        ||
                        sales.isEmpty()
        ) {

            return;
        }


        Map<LocalDate, List<BrickLinkSale>>
                weeklySales =
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
                            TemporalAdjusters
                                    .previousOrSame(
                                            DayOfWeek.MONDAY
                                    )
                    );


            weeklySales
                    .computeIfAbsent(
                            weekStart,
                            key ->
                                    new ArrayList<>()
                    )
                    .add(
                            sale
                    );
        }


        for (
                Map.Entry<
                        LocalDate,
                        List<BrickLinkSale>
                        > entry :
                weeklySales.entrySet()
        ) {

            List<BrickLinkSale> filteredSales =
                    removeOutliers(
                            entry.getValue()
                    );


            if (filteredSales.isEmpty()) {
                continue;
            }


            double value =
                    calculateWeightedAverage(
                            filteredSales
                    );


            value =
                    roundToTwoDecimals(
                            value
                    );


            /*
             * This is a REAL weekly observation
             * derived from BrickLink sales.
             */
            setPriceHistoryService.savePrice(
                    setNumber,
                    condition,
                    entry.getKey(),
                    value,
                    "BRICKLINK_SALES"
            );
        }
    }


    // ======================================================
    // BUILD GRAPH FROM DATABASE
    // ======================================================

    private List<PriceHistoryPoint>
    buildHistoryFromCache(
            String setNumber,
            String condition,
            String period) {

        List<SetPriceHistory> savedHistory =
                setPriceHistoryService
                        .getHistory(
                                setNumber,
                                condition
                        );


        if (
                savedHistory == null
                        ||
                        savedHistory.isEmpty()
        ) {

            return new ArrayList<>();
        }


        Map<LocalDate, Double> actualValues =
                new TreeMap<>();


        for (
                SetPriceHistory saved :
                savedHistory
        ) {

            actualValues.put(
                    saved.getPriceDate(),
                    saved.getEstimatedValue()
            );
        }


        LocalDate cutoffDate =
                getCutoffDate(
                        period
                );


        LocalDate startWeek =
                cutoffDate.with(
                        TemporalAdjusters
                                .previousOrSame(
                                        DayOfWeek.MONDAY
                                )
                );


        LocalDate currentWeek =
                LocalDate.now()
                        .with(
                                TemporalAdjusters
                                        .previousOrSame(
                                                DayOfWeek.MONDAY
                                        )
                        );


        /*
         * Find the latest real observation
         * that happened before the requested range.
         *
         * This lets 1M display a flat line even if
         * the last actual sale occurred earlier.
         */
        Double lastKnownValue =
                null;


        for (
                Map.Entry<LocalDate, Double> entry :
                actualValues.entrySet()
        ) {

            if (
                    entry.getKey()
                            .isBefore(
                                    startWeek
                            )
                            ||
                            entry.getKey()
                                    .isEqual(
                                            startWeek
                                    )
            ) {

                lastKnownValue =
                        entry.getValue();
            }
        }


        List<PriceHistoryPoint> history =
                new ArrayList<>();


        LocalDate week =
                startWeek;


        while (
                !week.isAfter(
                        currentWeek
                )
        ) {

            Double actualValue =
                    actualValues.get(
                            week
                    );


            if (actualValue != null) {

                lastKnownValue =
                        actualValue;


                history.add(
                        new PriceHistoryPoint(
                                week,
                                actualValue,
                                1
                        )
                );

            } else if (
                    lastKnownValue != null
            ) {

                /*
                 * sales = 0 means no new market
                 * observation existed this week.
                 *
                 * We are carrying forward the
                 * previous known value.
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
                    week.plusWeeks(
                            1
                    );
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


        if (
                condition.equalsIgnoreCase(
                        "N"
                )
                        ||
                        condition.equalsIgnoreCase(
                                "NEW"
                        )
        ) {

            return "NEW";
        }


        return "USED";
    }


    // ======================================================
    // PERIOD
    // ======================================================

    private LocalDate getCutoffDate(
            String period) {

        LocalDate today =
                LocalDate.now();


        if (period == null) {

            return today.minusMonths(
                    6
            );
        }


        return switch (
                period.toUpperCase()
                ) {

            case "1M" ->
                    today.minusMonths(
                            1
                    );

            case "3M" ->
                    today.minusMonths(
                            3
                    );

            case "6M" ->
                    today.minusMonths(
                            6
                    );

            case "12M" ->
                    today.minusMonths(
                            12
                    );

            default ->
                    today.minusMonths(
                            6
                    );
        };
    }


    // ======================================================
    // DATE PARSING
    // ======================================================

    private LocalDate parseBrickLinkDate(
            String dateOrdered) {

        try {

            if (
                    dateOrdered == null
                            ||
                            dateOrdered.length() < 10
            ) {

                return null;
            }


            return LocalDate.parse(
                    dateOrdered.substring(
                            0,
                            10
                    )
            );


        } catch (Exception e) {

            return null;
        }
    }


    // ======================================================
    // OUTLIER FILTERING
    // ======================================================

    private List<BrickLinkSale>
    removeOutliers(
            List<BrickLinkSale> sales) {

        if (sales.size() < 4) {

            return new ArrayList<>(
                    sales
            );
        }


        List<Double> prices =
                sales.stream()
                        .map(
                                BrickLinkSale::getUnitPrice
                        )
                        .sorted()
                        .toList();


        double q1 =
                percentile(
                        prices,
                        0.25
                );


        double q3 = percentile(prices, 0.75);

        double iqr = q3 - q1;

        if (iqr == 0) {
            return new ArrayList<>(sales);
        }


        double lowerBound = q1 - (1.5 * iqr);


        double upperBound = q3 + (1.5 * iqr);


        List<BrickLinkSale> filtered =
                new ArrayList<>();


        for (BrickLinkSale sale : sales) {

            double price = sale.getUnitPrice();

            if (price >= lowerBound && price <= upperBound) {
                filtered.add(sale);
            }
        }


        if (filtered.isEmpty()) {

            return new ArrayList<>(
                    sales
            );
        }


        return filtered;
    }


    // ======================================================
    // PERCENTILE
    // ======================================================

    private double percentile(
            List<Double> values,
            double percentile) {

        if (values.isEmpty()) {

            return 0;
        }


        double index =
                percentile
                        *
                        (values.size() - 1);


        int lowerIndex =
                (int)
                        Math.floor(
                                index
                        );


        int upperIndex =
                (int)
                        Math.ceil(
                                index
                        );


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

    private double roundToTwoDecimals(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}