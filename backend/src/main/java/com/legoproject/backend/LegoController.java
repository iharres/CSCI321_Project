package com.legoproject.backend;

import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class LegoController {

    private final PortfolioService portfolioService;
    private final BrickLinkService brickLinkService;
    private final PricingService pricingService;


    public LegoController(PortfolioService portfolioService, BrickLinkService brickLinkService, PricingService pricingService) {
        this.portfolioService = portfolioService;
        this.brickLinkService = brickLinkService;
        this.pricingService = pricingService;
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

    @GetMapping("/api/bricklink/price/{setNumber}/median")
    public double getMedianPrice(@PathVariable String setNumber) throws Exception {
        BrickLinkPriceGuide priceGuide = brickLinkService.getPriceGuide(setNumber);
        return pricingService.calculateMedianPrice(priceGuide);
    }
    @GetMapping("/api/bricklink/price/{setNumber}/average")
    public double getAveragePrice(@PathVariable String setNumber) throws Exception {
        BrickLinkPriceGuide priceGuide = brickLinkService.getPriceGuide(setNumber);
        return pricingService.calculateAverageWithoutOutliers(priceGuide);
    }

}


