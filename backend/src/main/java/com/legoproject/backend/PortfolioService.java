package com.legoproject.backend;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    private final LegoSetRepository legoSetRepository;
    private final UserRepository userRepository;

    public PortfolioService(
            LegoSetRepository legoSetRepository,
            UserRepository userRepository) {

        this.legoSetRepository = legoSetRepository;
        this.userRepository = userRepository;
    }

    public Portfolio getPortfolio(Long userId) {

        Portfolio portfolio = new Portfolio();

        List<LegoSet> sets = legoSetRepository.findByUserId(userId);

        for (LegoSet set : sets) {
            portfolio.addSet(set);
        }

        return portfolio;
    }

    public void addSet(LegoSet set, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        set.setUser(user);

        legoSetRepository.saveAndFlush(set);
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