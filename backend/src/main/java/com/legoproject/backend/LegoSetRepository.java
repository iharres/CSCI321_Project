package com.legoproject.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LegoSetRepository
        extends JpaRepository<LegoSet, Long> {

    List<LegoSet> findByUserId(Long userId);

    Optional<LegoSet> findByIdAndUserId(
            Long id,
            Long userId
    );
}