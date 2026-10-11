package com.legoproject.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.stereotype.Service;

@Service
public class BrickLinkService {

    private final OAuth10aService service;
    private final OAuth1AccessToken accessToken;
    private final ObjectMapper objectMapper;

    public BrickLinkService() {
        // Get BrickLink credentials from environment variables
        String consumerKey = System.getenv("BRICKLINK_CONSUMER_KEY");
        String consumerSecret = System.getenv("BRICKLINK_CONSUMER_SECRET");
        String token = System.getenv("BRICKLINK_TOKEN");
        String tokenSecret = System.getenv("BRICKLINK_TOKEN_SECRET");

        // Set up OAuth authentication for BrickLink
        service = new ServiceBuilder(consumerKey)
                .apiSecret(consumerSecret)
                .build(BrickLinkApi.instance());

        // Create the access token using our BrickLink credentials
        accessToken = new OAuth1AccessToken(token, tokenSecret);

        objectMapper = new ObjectMapper();
    }

    public BrickLinkPriceGuide getPriceGuide(String setNumber) throws Exception {

        String url =
                "https://api.bricklink.com/api/store/v1/items/SET/"
                        + setNumber
                        + "-1/price?guide_type=sold";
        // Create a GET request to BrickLink
        OAuthRequest request = new OAuthRequest(Verb.GET, url);

        // Add our OAuth credentials to the request
        service.signRequest(accessToken, request);

        String response = service.execute(request).getBody();
        System.out.println(response);

        JsonNode data = objectMapper.readTree(response).get("data");

        BrickLinkPriceGuide priceGuide = new BrickLinkPriceGuide();

        // Store the pricing information from BrickLink
        priceGuide.setMinPrice(
                data.get("min_price").asDouble()
        );

        priceGuide.setMaxPrice(
                data.get("max_price").asDouble()
        );

        priceGuide.setAveragePrice(
                data.get("avg_price").asDouble()
        );

        priceGuide.setQuantityAveragePrice(
                data.get("qty_avg_price").asDouble()
        );

        priceGuide.setUnitQuantity(
                data.get("unit_quantity").asInt()
        );

        priceGuide.setTotalQuantity(
                data.get("total_quantity").asInt()
        );

        // Convert each individual BrickLink sale into a BrickLinkSale object
        java.util.List<BrickLinkSale> sales = new java.util.ArrayList<>();

        JsonNode priceDetails = data.get("price_detail");

        if (priceDetails != null && priceDetails.isArray()) {

            for (JsonNode saleData : priceDetails) {

                BrickLinkSale sale = new BrickLinkSale();

                sale.setUnitPrice(
                        saleData.get("unit_price").asDouble()
                );

                sale.setQuantity(
                        saleData.get("quantity").asInt()
                );

                sale.setSellerCountry(
                        saleData.get("seller_country_code").asText()
                );

                sale.setBuyerCountry(
                        saleData.get("buyer_country_code").asText()
                );

                sale.setDateOrdered(
                        saleData.get("date_ordered").asText()
                );

                sales.add(sale);
            }
        }

        priceGuide.setSales(sales);


        return priceGuide;
    }
}
