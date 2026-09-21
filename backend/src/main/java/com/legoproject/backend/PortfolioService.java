package com.legoproject.backend;

import org.springframework.stereotype.Service;

@Service

public class PortfolioService {

    public Portfolio getPortfolio(){
        Portfolio portfolio = new Portfolio();
        portfolio.addSet(
                new LegoSet(
                        "10294",
                        "Titanic",
                        500,
                        650
                )
        );
        return portfolio;
    }
}
