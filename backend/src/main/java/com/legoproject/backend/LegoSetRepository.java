package com.legoproject.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegoSetRepository extends JpaRepository<LegoSet, Long> {
    List<LegoSet> findByUserId(Long userId);

}