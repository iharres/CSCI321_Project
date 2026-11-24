package com.legoproject.backend;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarketValueRefreshService {

    private final LegoSetRepository legoSetRepository;
    private final BrickLinkService brickLinkService;
    private final PricingService pricingService;

    private final Map<String, Double> dailyValues =
            new HashMap<>();

    private LocalDate cacheDate = LocalDate.now();

    public MarketValueRefreshService(
            LegoSetRepository legoSetRepository,
            BrickLinkService brickLinkService,
            PricingService pricingService) {

        this.legoSetRepository = legoSetRepository;
        this.brickLinkService = brickLinkService;
        this.pricingService = pricingService;
    }

    // ======================================================
    // REFRESH PORTFOLIO
    // ======================================================

    public void refreshPortfolioValues(Long userId) {

        List<LegoSet> sets =
                legoSetRepository.findByUserId(userId);

        for (LegoSet set : sets) {

            Double latestValue = getLatestValue(
                    set.getSetNumber(),
                    normalizeCondition(set.getCondition())
            );

            /*
             * If BrickLink is unavailable, preserve
             * the current value stored in PostgreSQL.
             */
            if (latestValue == null) {
                continue;
            }

            if (Double.compare(
                    set.getCurrentValue(),
                    latestValue) != 0) {

                set.setCurrentValue(latestValue);

                legoSetRepository.save(set);
            }
        }
    }

    // ======================================================
    // GET LATEST MARKET VALUE
    // ======================================================

    private synchronized Double getLatestValue(
            String setNumber,
            String condition) {

        LocalDate today = LocalDate.now();

        if (!today.equals(cacheDate)) {

            dailyValues.clear();

            cacheDate = today;
        }

        String key = setNumber + ":" + condition;

        /*
         * Avoid repeatedly requesting the same set
         * from BrickLink during the same day.
         */
        if (dailyValues.containsKey(key)) {

            return dailyValues.get(key);
        }

        try {

            BrickLinkPriceGuide priceGuide =
                    brickLinkService.getPriceGuide(
                            setNumber,
                            condition
                    );

            double latestValue =
                    pricingService
                            .calculateAverageWithoutOutliers(
                                    priceGuide
                            );

            if (!Double.isFinite(latestValue)
                    || latestValue <= 0) {

                return null;
            }

            latestValue =
                    Math.round(latestValue * 100.0)
                            / 100.0;

            dailyValues.put(key, latestValue);

            return latestValue;

        } catch (Exception e) {

            System.out.println(
                    "Unable to refresh set "
                            + setNumber
                            + ". Keeping saved market value."
            );

            return null;
        }
    }

    // ======================================================
    // NORMALIZE CONDITION
    // ======================================================

    private String normalizeCondition(String condition) {

        if (condition == null
                || condition.equalsIgnoreCase("NEW")
                || condition.equalsIgnoreCase("N")) {

            return "NEW";
        }

        return "USED";
    }
}