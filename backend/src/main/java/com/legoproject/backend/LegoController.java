package com.legoproject.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@CrossOrigin
@RestController
public class LegoController {
    private final PortfolioService portfolioService;


    public LegoController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/api/portfolio")
    public Portfolio getPortfolio() {
        return portfolioService.getPortfolio();
    }

    @PostMapping("/api/sets")
    public void addSet(@RequestBody LegoSet set) {
        portfolioService.addSet(set);
    }

    @DeleteMapping("/api/sets/{setNumber}")
    public void deleteSet(@PathVariable String setNumber){
        portfolioService.removeSet(setNumber);
    }



}


