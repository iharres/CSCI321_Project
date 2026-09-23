package com.legoproject.backend;

import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/api/sets/{setNumber}")
    public void updateSet(
            @PathVariable String setNumber,
            @RequestBody LegoSet updatedSet) {

        portfolioService.updateSet(setNumber, updatedSet);
    }
}


