package com.legoproject.backend;

import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class LegoController {

    private final PortfolioService portfolioService;
    private final BrickLinkService brickLinkService;
    private final PricingService pricingService;

    public LegoController(
            PortfolioService portfolioService,
            BrickLinkService brickLinkService,
            PricingService pricingService) {

        this.portfolioService = portfolioService;
        this.brickLinkService = brickLinkService;
        this.pricingService = pricingService;
    }

    @GetMapping("/api/portfolio")
    public Portfolio getPortfolio(@RequestParam Long userId) {
        return portfolioService.getPortfolio(userId);
    }

    @PostMapping("/api/sets")
    public void addSet(
            @RequestBody AddSetRequest request,
            @RequestParam Long userId) throws Exception {

        String setNumber = request.getSetNumber();
        double purchasePrice = request.getPurchasePrice();
        String condition = request.getCondition();

        String name = brickLinkService.getSetName(setNumber);

        BrickLinkPriceGuide priceGuide =
                brickLinkService.getPriceGuide(setNumber, condition);

        double estimatedValue =
                pricingService.calculateAverageWithoutOutliers(priceGuide);

        LegoSet set = new LegoSet(
                setNumber,
                name,
                purchasePrice,
                estimatedValue
        );

        set.setCondition(condition);

        portfolioService.addSet(set, userId);
    }

    @DeleteMapping("/api/sets/{id}")
    public void deleteSet(@PathVariable Long id) {
        portfolioService.removeSet(id);
    }

    @PutMapping("/api/sets/{setNumber}")
    public void updateSet(
            @PathVariable String setNumber,
            @RequestBody LegoSet updatedSet) {

        portfolioService.updateSet(setNumber, updatedSet);
    }

    @GetMapping("/api/bricklink/price/{setNumber}")
    public BrickLinkPriceGuide getBrickLinkPrice(
            @PathVariable String setNumber) throws Exception {

        return brickLinkService.getPriceGuide(setNumber, "NEW");
    }

    @GetMapping("/api/bricklink/price/{setNumber}/median")
    public double getMedianPrice(
            @PathVariable String setNumber) throws Exception {

        BrickLinkPriceGuide priceGuide =
                brickLinkService.getPriceGuide(setNumber, "NEW");

        return pricingService.calculateMedianPrice(priceGuide);
    }

    @GetMapping("/api/bricklink/price/{setNumber}/average")
    public double getAveragePrice(
            @PathVariable String setNumber) throws Exception {

        BrickLinkPriceGuide priceGuide =
                brickLinkService.getPriceGuide(setNumber, "NEW");

        return pricingService.calculateAverageWithoutOutliers(priceGuide);
    }
}

