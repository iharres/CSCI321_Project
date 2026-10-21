package com.legoproject.backend;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    private final LegoSetRepository legoSetRepository;

    public PortfolioService(LegoSetRepository legoSetRepository) {
        this.legoSetRepository = legoSetRepository;
    }

    public Portfolio getPortfolio() {

        Portfolio portfolio = new Portfolio();

        List<LegoSet> sets = legoSetRepository.findAll();

        for (LegoSet set : sets) {
            portfolio.addSet(set);
        }

        return portfolio;
    }

    public void addSet(LegoSet set) {
        LegoSet savedSet = legoSetRepository.saveAndFlush(set);

        System.out.println("Saved LEGO set with database ID: " + savedSet.getId());
    }
    public void removeSet(Long id) {
        legoSetRepository.deleteById(id);
    }

    public void updateSet(String setNumber, LegoSet updatedSet) {

        List<LegoSet> sets = legoSetRepository.findAll();

        for (LegoSet set : sets) {

            if (set.getSetNumber().equals(setNumber)) {

                set.setName(updatedSet.getName());
                set.setPurchasePrice(updatedSet.getPurchasePrice());
                set.setCurrentValue(updatedSet.getCurrentValue());
                set.setCondition(updatedSet.getCondition());

                legoSetRepository.save(set);
                return;
            }
        }
    }
}