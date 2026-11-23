package com.legoproject.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SetPriceHistoryRepository
        extends JpaRepository<SetPriceHistory, Long> {

    Optional<SetPriceHistory>
    findBySetNumberAndConditionAndPriceDate(
            String setNumber,
            String condition,
            LocalDate priceDate
    );


    List<SetPriceHistory>
    findBySetNumberAndConditionOrderByPriceDateAsc(
            String setNumber,
            String condition
    );
}