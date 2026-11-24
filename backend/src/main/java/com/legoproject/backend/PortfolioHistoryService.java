package com.legoproject.backend;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PortfolioHistoryService {

    private final LegoSetRepository legoSetRepository;
    private final PriceHistoryService priceHistoryService;

    public PortfolioHistoryService(
            LegoSetRepository legoSetRepository,
            PriceHistoryService priceHistoryService) {

        this.legoSetRepository = legoSetRepository;
        this.priceHistoryService = priceHistoryService;
    }

    public List<PortfolioHistoryPoint> getPortfolioHistory(
            Long userId,
            String period) throws Exception {

        List<LegoSet> sets =
                legoSetRepository.findByUserId(userId);

        if (sets.isEmpty()) {
            return new ArrayList<>();
        }

        Map<LocalDate, Double> portfolioValues =
                new TreeMap<>();

        Map<LocalDate, Integer> holdingCounts =
                new TreeMap<>();

        for (LegoSet set : sets) {

            List<PriceHistoryPoint> history =
                    priceHistoryService.getPriceHistory(
                            set.getSetNumber(),
                            set.getCondition(),
                            period
                    );

            if (history == null || history.isEmpty()) {
                return new ArrayList<>();
            }

            for (PriceHistoryPoint point : history) {

                portfolioValues.merge(
                        point.getDate(),
                        point.getValue(),
                        Double::sum
                );

                holdingCounts.merge(
                        point.getDate(),
                        1,
                        Integer::sum
                );
            }
        }

        List<PortfolioHistoryPoint> portfolioHistory =
                new ArrayList<>();

        for (Map.Entry<LocalDate, Double> entry :
                portfolioValues.entrySet()) {

            LocalDate date = entry.getKey();

            /*
             * Only include dates where every holding
             * has a known market value.
             */
            if (holdingCounts.getOrDefault(date, 0)
                    == sets.size()) {

                double value =
                        Math.round(entry.getValue() * 100.0)
                                / 100.0;

                portfolioHistory.add(
                        new PortfolioHistoryPoint(
                                date,
                                value
                        )
                );
            }
        }

        return portfolioHistory;
    }
}