package com.legoproject.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

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

}


