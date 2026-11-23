package com.legoproject.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin
@RestController
public class LegoController {

    private final UserRepository userRepository;
    private final PortfolioService portfolioService;
    private final BrickLinkService brickLinkService;
    private final PricingService pricingService;
    private final PriceHistoryService priceHistoryService;

    public LegoController(
            PortfolioService portfolioService,
            BrickLinkService brickLinkService,
            PricingService pricingService,
            UserRepository userRepository,
            PriceHistoryService priceHistoryService) {

        this.portfolioService = portfolioService;
        this.brickLinkService = brickLinkService;
        this.pricingService = pricingService;
        this.userRepository = userRepository;
        this.priceHistoryService = priceHistoryService;
    }


    // ======================================================
    // GET CURRENT USER
    // ======================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (
                authentication == null || !authentication.isAuthenticated()
        ) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in");
        }

        return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }


    // ======================================================
    // PORTFOLIO
    // ======================================================

    @GetMapping("/api/portfolio")
    public Portfolio getPortfolio(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return portfolioService.getPortfolio(user.getId());
    }


    // ======================================================
    // ADD LEGO SET
    // ======================================================

    @PostMapping("/api/sets")
    public void addSet(
            @RequestBody AddSetRequest request,
            Authentication authentication) throws Exception {

        /*
         * IMPORTANT:
         * The browser no longer tells us which user owns the set.
         *
         * The authenticated session determines the owner.
         */
        User user = getAuthenticatedUser(authentication);


        String setNumber = request.getSetNumber();

        double purchasePrice = request.getPurchasePrice();

        String condition = request.getCondition();

        String name = brickLinkService.getSetName(setNumber);

        BrickLinkPriceGuide priceGuide = brickLinkService.getPriceGuide(setNumber, condition);
        double estimatedValue = pricingService.calculateAverageWithoutOutliers(priceGuide);
        LegoSet set = new LegoSet(setNumber, name, purchasePrice, estimatedValue);


        set.setCondition(condition);


        /*
         * The user ID comes from the server-side
         * authenticated session.
         */
        portfolioService.addSet(set, user.getId());
    }


    // ======================================================
    // DELETE LEGO SET
    // ======================================================

    @DeleteMapping("/api/sets/{id}")
    public void deleteSet(@PathVariable Long id, Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        try {portfolioService.removeSet(id, user.getId());

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "LEGO set not found");
        }
    }


    // ======================================================
    // UPDATE LEGO SET
    // ======================================================

    @PutMapping("/api/sets/{setNumber}")
    public void updateSet(
            @PathVariable String setNumber,
            @RequestBody LegoSet updatedSet,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        try {
            portfolioService.updateSet(setNumber, updatedSet, user.getId());

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "LEGO set not found");
        }
    }


    // ======================================================
    // BRICKLINK PRICE GUIDE
    // ======================================================

    @GetMapping("/api/bricklink/price/{setNumber}")
    public BrickLinkPriceGuide getBrickLinkPrice(
            @PathVariable String setNumber) throws Exception {

        return brickLinkService.getPriceGuide(setNumber, "NEW");
    }


    // ======================================================
    // BRICKLINK MEDIAN PRICE
    // ======================================================

    @GetMapping("/api/bricklink/price/{setNumber}/median")
    public double getMedianPrice(
            @PathVariable String setNumber) throws Exception {

        BrickLinkPriceGuide priceGuide = brickLinkService.getPriceGuide(setNumber, "NEW");

        return pricingService.calculateMedianPrice(priceGuide);
    }


    // ======================================================
    // BRICKLINK AVERAGE PRICE
    // ======================================================

    @GetMapping("/api/bricklink/price/{setNumber}/average")
    public double getAveragePrice(
            @PathVariable String setNumber) throws Exception {

        BrickLinkPriceGuide priceGuide = brickLinkService.getPriceGuide(setNumber, "NEW");
        return pricingService.calculateAverageWithoutOutliers(priceGuide);
    }


    // ======================================================
    // OLD OCR IDENTIFICATION ENDPOINT
    // ======================================================

    @PostMapping("/api/bricklink/identify")
    public String identifySet(
            @RequestBody IdentifySetRequest request) {

        String ocrText = request.getOcrText();

        if (ocrText == null) {
            ocrText = "";
        }


        String normalizedOcr =
                ocrText
                        .toLowerCase()
                        .replaceAll(
                                "[^a-z0-9 ]",
                                " "
                        )
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        String bestCandidate = "";

        int bestScore = Integer.MIN_VALUE;


        java.util.List<String> candidates = request.getCandidates();

        for (int i = 0; i < candidates.size(); i++) {
            String candidate = candidates.get(i);


            try {
                String setName = brickLinkService.getSetName(candidate);

                String normalizedName = setName.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
                int score = 100 - (i * 5);

                if (!normalizedName.isEmpty() && normalizedOcr.contains(normalizedName)) {
                    score += 1000;
                }


                String[] words = normalizedName.split(" ");


                for (String word : words) {
                    if (word.length() >= 4 && normalizedOcr.contains(word)) {
                        score += 150;
                    }
                }

                System.out.println(candidate + " | " + setName + " | score = " + score);

                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = candidate;
                }


            } catch (Exception e) {

                // Not a valid BrickLink set.
            }
        }

        return bestCandidate;
    }

    @GetMapping("/api/sets/{id}/history")
    public java.util.List<PriceHistoryPoint> getSetPriceHistory(@PathVariable Long id, @RequestParam(defaultValue = "6M") String period, Authentication authentication) throws Exception {

        User user = getAuthenticatedUser(authentication);

        LegoSet set = portfolioService.getSetForUser(id, user.getId());

        return priceHistoryService.getPriceHistory(set.getSetNumber(), set.getCondition(), period);
    }
}