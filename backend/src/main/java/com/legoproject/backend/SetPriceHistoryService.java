package com.legoproject.backend;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SetPriceHistoryService {

    private final SetPriceHistoryRepository repository;


    public SetPriceHistoryService(
            SetPriceHistoryRepository repository) {

        this.repository =
                repository;
    }


    // ======================================================
    // SAVE OR UPDATE HISTORICAL PRICE
    // ======================================================

    public void savePrice(
            String setNumber,
            String condition,
            LocalDate date,
            double value,
            String source) {

        SetPriceHistory history =
                repository
                        .findBySetNumberAndConditionAndPriceDate(
                                setNumber,
                                condition,
                                date
                        )
                        .orElseGet(() ->
                                new SetPriceHistory(
                                        setNumber,
                                        condition,
                                        date,
                                        value,
                                        source
                                )
                        );


        history.setEstimatedValue(
                value
        );

        history.setSource(
                source
        );


        repository.save(
                history
        );
    }


    // ======================================================
    // READ SAVED HISTORY
    // ======================================================

    public List<SetPriceHistory> getHistory(
            String setNumber,
            String condition) {

        return repository
                .findBySetNumberAndConditionOrderByPriceDateAsc(
                        setNumber,
                        condition
                );
    }
}