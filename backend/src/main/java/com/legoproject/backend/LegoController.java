package com.legoproject.backend;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class LegoController {

    private final UserRepository userRepository;
    private final PortfolioService portfolioService;
    private final BrickLinkService brickLinkService;
    private final PricingService pricingService;
    private final PriceHistoryService priceHistoryService;
    private final PortfolioHistoryService portfolioHistoryService;
    private final MarketValueRefreshService marketValueRefreshService;


    public LegoController(
            PortfolioService portfolioService,
            BrickLinkService brickLinkService,
            PricingService pricingService,
            UserRepository userRepository,
            PriceHistoryService priceHistoryService,
            PortfolioHistoryService portfolioHistoryService,
            MarketValueRefreshService marketValueRefreshService) {

        this.portfolioService = portfolioService;
        this.brickLinkService = brickLinkService;
        this.pricingService = pricingService;
        this.userRepository = userRepository;
        this.priceHistoryService = priceHistoryService;
        this.portfolioHistoryService = portfolioHistoryService;
        this.marketValueRefreshService = marketValueRefreshService;
    }


    // ======================================================
    // AUTHENTICATION
    // ======================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "You must be logged in"
            );
        }

        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );
    }


    // ======================================================
    // GET PORTFOLIO
    // ======================================================

    @GetMapping("/api/portfolio")
    public Portfolio getPortfolio(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        marketValueRefreshService.refreshPortfolioValues(
                user.getId()
        );

        return portfolioService.getPortfolio(
                user.getId()
        );
    }


    // ======================================================
    // ADD LEGO SET
    // ======================================================

    @PostMapping("/api/sets")
    public void addSet(
            @RequestBody AddSetRequest request,
            Authentication authentication) throws Exception {

        User user = getAuthenticatedUser(authentication);

        String setNumber = request.getSetNumber();
        double purchasePrice = request.getPurchasePrice();
        String condition = request.getCondition();

        String name =
                brickLinkService.getSetName(setNumber);

        BrickLinkPriceGuide priceGuide =
                brickLinkService.getPriceGuide(
                        setNumber,
                        condition
                );

        double estimatedValue =
                pricingService.calculateAverageWithoutOutliers(
                        priceGuide
                );

        LegoSet set = new LegoSet(
                setNumber,
                name,
                purchasePrice,
                estimatedValue
        );

        set.setCondition(condition);

        portfolioService.addSet(
                set,
                user.getId()
        );
    }


    // ======================================================
    // DELETE LEGO SET
    // ======================================================

    @DeleteMapping("/api/sets/{id}")
    public void deleteSet(
            @PathVariable("id") Long id,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        try {

            portfolioService.removeSet(
                    id,
                    user.getId()
            );

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "LEGO set not found"
            );
        }
    }


    // ======================================================
    // UPDATE LEGO SET
    // ======================================================

    @PutMapping("/api/sets/{setNumber}")
    public void updateSet(
            @PathVariable("setNumber") String setNumber,
            @RequestBody LegoSet updatedSet,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        try {

            portfolioService.updateSet(
                    setNumber,
                    updatedSet,
                    user.getId()
            );

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "LEGO set not found"
            );
        }
    }


    // ======================================================
    // INDIVIDUAL SET PRICE HISTORY
    // ======================================================

    @GetMapping("/api/sets/{id}/history")
    public List<PriceHistoryPoint> getSetPriceHistory(
            @PathVariable("id") Long id,
            @RequestParam(
                    name = "period",
                    defaultValue = "6M"
            ) String period,
            Authentication authentication) throws Exception {

        User user = getAuthenticatedUser(authentication);

        LegoSet set;

        try {

            set = portfolioService.getSetForUser(
                    id,
                    user.getId()
            );

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "LEGO set not found"
            );
        }

        return priceHistoryService.getPriceHistory(
                set.getSetNumber(),
                set.getCondition(),
                period
        );
    }


    // ======================================================
    // OVERALL PORTFOLIO PRICE HISTORY
    // ======================================================

    @GetMapping("/api/portfolio/history")
    public List<PortfolioHistoryPoint> getPortfolioHistory(
            @RequestParam(
                    name = "period",
                    defaultValue = "6M"
            ) String period,
            Authentication authentication) throws Exception {

        User user = getAuthenticatedUser(authentication);

        return portfolioHistoryService.getPortfolioHistory(
                user.getId(),
                period
        );
    }


    // ======================================================
    // BRICKLINK PRICE GUIDE
    // ======================================================

    @GetMapping("/api/bricklink/price/{setNumber}")
    public BrickLinkPriceGuide getBrickLinkPrice(
            @PathVariable("setNumber") String setNumber,
            @RequestParam(
                    name = "condition",
                    defaultValue = "NEW"
            ) String condition) throws Exception {

        return brickLinkService.getPriceGuide(
                setNumber,
                condition
        );
    }


    // ======================================================
    // BRICKLINK MEDIAN PRICE
    // ======================================================

    @GetMapping("/api/bricklink/price/{setNumber}/median")
    public double getMedianPrice(
            @PathVariable("setNumber") String setNumber,
            @RequestParam(
                    name = "condition",
                    defaultValue = "NEW"
            ) String condition) throws Exception {

        BrickLinkPriceGuide priceGuide =
                brickLinkService.getPriceGuide(
                        setNumber,
                        condition
                );

        return pricingService.calculateMedianPrice(
                priceGuide
        );
    }


    // ======================================================
    // BRICKLINK FILTERED AVERAGE PRICE
    // ======================================================

    @GetMapping("/api/bricklink/price/{setNumber}/average")
    public double getAveragePrice(
            @PathVariable("setNumber") String setNumber,
            @RequestParam(
                    name = "condition",
                    defaultValue = "NEW"
            ) String condition) throws Exception {

        BrickLinkPriceGuide priceGuide =
                brickLinkService.getPriceGuide(
                        setNumber,
                        condition
                );

        return pricingService.calculateAverageWithoutOutliers(
                priceGuide
        );
    }
}