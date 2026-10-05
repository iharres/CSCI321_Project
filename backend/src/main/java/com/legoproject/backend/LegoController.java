package com.legoproject.backend;

import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class LegoController {

    private final PortfolioService portfolioService;
    private final BrickLinkService brickLinkService;

    public LegoController(PortfolioService portfolioService, BrickLinkService brickLinkService) {
        this.portfolioService = portfolioService;
        this.brickLinkService = brickLinkService;
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
    public void deleteSet(@PathVariable String setNumber) {
        portfolioService.removeSet(setNumber);
    }

    @PutMapping("/api/sets/{setNumber}")
    public void updateSet(@PathVariable String setNumber, @RequestBody LegoSet updatedSet) {
        portfolioService.updateSet(setNumber, updatedSet);
    }

    @GetMapping("/api/bricklink/price/{setNumber}")
    public BrickLinkPriceGuide getBrickLinkPrice(@PathVariable String setNumber) throws Exception {
        return brickLinkService.getPriceGuide(setNumber);
    }
}


