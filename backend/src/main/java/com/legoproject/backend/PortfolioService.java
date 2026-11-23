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


    // ======================================================
    // GET PORTFOLIO
    // ======================================================

    public Portfolio getPortfolio(Long userId) {

        Portfolio portfolio =
                new Portfolio();

        List<LegoSet> sets =
                legoSetRepository.findByUserId(
                        userId
                );

        for (LegoSet set : sets) {

            portfolio.addSet(
                    set
            );
        }

        return portfolio;
    }


    // ======================================================
    // ADD SET
    // ======================================================

    public void addSet(
            LegoSet set,
            Long userId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        set.setUser(
                user
        );

        legoSetRepository.saveAndFlush(
                set
        );
    }


    // ======================================================
    // REMOVE SET
    // ======================================================

    public void removeSet(
            Long id,
            Long userId) {

        LegoSet set =
                legoSetRepository
                        .findByIdAndUserId(
                                id,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "LEGO set not found or does not belong to this user"
                                )
                        );

        legoSetRepository.delete(
                set
        );
    }


    // ======================================================
    // UPDATE SET
    // ======================================================

    public void updateSet(
            String setNumber,
            LegoSet updatedSet,
            Long userId) {

        /*
         * Only search through LEGO sets belonging
         * to the authenticated user.
         */
        List<LegoSet> sets =
                legoSetRepository.findByUserId(
                        userId
                );


        for (LegoSet set : sets) {

            if (
                    set.getSetNumber()
                            .equals(setNumber)
            ) {

                set.setName(
                        updatedSet.getName()
                );

                set.setPurchasePrice(
                        updatedSet.getPurchasePrice()
                );

                set.setCurrentValue(
                        updatedSet.getCurrentValue()
                );

                set.setCondition(
                        updatedSet.getCondition()
                );


                legoSetRepository.save(
                        set
                );

                return;
            }
        }


        /*
         * Nothing belonging to this user matched.
         */
        throw new IllegalArgumentException(
                "LEGO set not found or does not belong to this user"
        );
    }

    public LegoSet getSetForUser(
            Long id,
            Long userId) {

        return legoSetRepository
                .findByIdAndUserId(
                        id,
                        userId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "LEGO set not found or does not belong to this user"
                        )
                );
    }
}