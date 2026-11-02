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
                        .replaceAll("[^a-z0-9 ]", " ")
                        .replaceAll("\\s+", " ");

        String bestCandidate = "";
        int bestScore = Integer.MIN_VALUE;

        java.util.List<String> candidates =
                request.getCandidates();

        for (int i = 0; i < candidates.size(); i++) {

            String candidate =
                    candidates.get(i);

            try {

                String setName =
                        brickLinkService.getSetName(candidate);

                String normalizedName =
                        setName
                                .toLowerCase()
                                .replaceAll("[^a-z0-9 ]", " ")
                                .replaceAll("\\s+", " ")
                                .trim();

                // Candidates near the top of the OCR ranking
                // start with a slightly higher score.
                int score =
                        100 - (i * 5);

                // Very strong match:
                // entire BrickLink name appears in OCR text.
                if (
                        !normalizedName.isEmpty()
                                && normalizedOcr.contains(normalizedName)
                ) {

                    score += 1000;
                }

                // Check individual important words from the set name.
                String[] words =
                        normalizedName.split(" ");

                for (String word : words) {

                    if (
                            word.length() >= 4
                                    && normalizedOcr.contains(word)
                    ) {

                        score += 150;
                    }
                }

                System.out.println(
                        candidate
                                + " | "
                                + setName
                                + " | score = "
                                + score
                );

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
}

