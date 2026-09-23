package com.legoproject.backend;

import org.springframework.stereotype.Service;

@Service

public class PortfolioService {
    private final Portfolio portfolio;

    public PortfolioService(){
        portfolio = new Portfolio();

        portfolio.addSet(
                new LegoSet(
                        "10294",
                        "Titanic",
                        500,
                        650
                )
        );
    }
    public Portfolio getPortfolio(){
        return portfolio;
    }

    public void addSet(LegoSet set){
        portfolio.addSet(set);
    }
    public void removeSet(String setNumber) {
        portfolio.removeSet(setNumber);
    }

    public void updateSet(String setNumber, LegoSet updatedSet) {
        portfolio.updateSet(setNumber, updatedSet);
    }
}
